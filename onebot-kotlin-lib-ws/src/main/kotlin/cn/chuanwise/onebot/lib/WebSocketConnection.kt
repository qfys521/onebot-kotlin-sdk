/*
 * Copyright 2024 Chuanwise and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.chuanwise.onebot.lib

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.websocket.CloseReason
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CancellationException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

interface WebSocketConnectionConfiguration {
    val maxConnectAttempts: Int?
    val host: String
    val port: Int
    val path: String
    val accessToken: String?
    val reconnectInterval: Duration

    // require final implementations checking!
    val heartbeatInterval: Duration?
}

abstract class WebSocketConnection(
    configuration: WebSocketConnectionConfiguration,
    private val job: Job,
    override val coroutineContext: CoroutineContext,
    receivingLoop: WebSocketReceivingLoop,
    logger: KLogger,
) : WebSocketLikeConnection {

    private val lock = ReentrantReadWriteLock()
    private val condition = lock.writeLock().newCondition()

    enum class State {
        INITIALIZED,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        WAITING,
    }

    private var stateWithoutLock: State = State.INITIALIZED
    protected val state: State
        get() = lock.read { stateWithoutLock }

    override val isConnected: Boolean
        get() = state == State.CONNECTED

    private var sessionWithoutLock: WebSocketSession? = null
    protected val session: WebSocketSession?
        get() = lock.read { sessionWithoutLock }

    private val client = HttpClient {
        install(WebSockets)
    }

    private val connectJob = client.launch(job) {
        val attempts = IncreasingInts(configuration.maxConnectAttempts)
        for (attempt in attempts) {
            // change state
            lock.read {
                when (stateWithoutLock) {
                    State.INITIALIZED, State.WAITING -> lock.upgrade {
                        stateWithoutLock = State.CONNECTING
                    }

                    State.DISCONNECTED -> return@launch
                    else -> throw IllegalStateException("Unexpected state: $stateWithoutLock")
                }
            }

            client.webSocket(
                host = configuration.host,
                port = configuration.port,
                path = configuration.path,
                request = {
                    if (!configuration.accessToken.isNullOrEmpty()) {
                        header("Authorization", BEARER_WITH_SPACE + configuration.accessToken)
                    }
                }
            ) {
                lock.read {
                    when (stateWithoutLock) {
                        State.CONNECTING -> lock.upgrade {
                            sessionWithoutLock = this
                            stateWithoutLock = State.CONNECTED
                            condition.signalAll()
                        }

                        State.DISCONNECTED -> {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Connection closed."))
                            return@webSocket
                        }

                        else -> throw IllegalStateException("Unexpected state: $stateWithoutLock")
                    }
                }

                try {
                    receivingLoop.receive(this, ::onReceive)
                } catch (_: CancellationException) {
                    // receiving cancelled, caused by closing
                    if (state != State.DISCONNECTED) {
                        throw IllegalStateException(
                            "Unexpected state: $state " +
                                    "when java.util.concurrent.CancellationException thrown in receiving loop."
                        )
                    }
                } catch (throwable: Throwable) {
                    logger.error(throwable) { "Exception occurred in session" }
                } finally {
                    lock.read {
                        when (stateWithoutLock) {
                            State.CONNECTED -> lock.upgrade {
                                sessionWithoutLock = null
                                stateWithoutLock = if (attempts.hasNext(attempt)) State.WAITING else State.DISCONNECTED
                                condition.signalAll()
                            }

                            State.DISCONNECTED -> {
                                // ignored
                            }

                            else -> throw IllegalStateException("Unexpected state: $stateWithoutLock")
                        }
                    }
                }
            }

            if (state == State.WAITING) {
                delay(configuration.reconnectInterval)
            }
        }
    }

    protected abstract suspend fun onReceive(node: JsonNode)

    override suspend fun disconnect(reason: CloseReason) {
        val currentSession = session
        if (currentSession === null) {
            throw IllegalStateException("Connection is not established.")
        }

        currentSession.close(reason)
    }

    override fun await(): WebSocketConnection {
        lock.write {
            condition.await()
        }
        return this
    }

    override fun close() {
        lock.read {
            when (stateWithoutLock) {
                State.DISCONNECTED -> throw IllegalStateException("Connection already closed.")
                State.CONNECTED -> runBlocking {
                    disconnect(CloseReason(CloseReason.Codes.NORMAL, "Connection closed."))
                }
                State.INITIALIZED, State.CONNECTING, State.WAITING -> Unit
            }
            lock.upgrade {
                stateWithoutLock = State.DISCONNECTED
                sessionWithoutLock = null
                client.close()
                job.cancel("Connection closed.")
            }
        }
    }
}

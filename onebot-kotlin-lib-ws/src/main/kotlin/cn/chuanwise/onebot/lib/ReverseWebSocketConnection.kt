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
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

interface ReverseWebSocketConnectionConfiguration {
    val host: String
    val port: Int
    val path: String
    val accessToken: String?

    // require final implementations checking!
    val heartbeatInterval: Duration?
}

abstract class ReverseWebSocketConnection(
    configuration: ReverseWebSocketConnectionConfiguration,
    private val job: Job,
    override val coroutineContext: CoroutineContext,
    receivingLoop: WebSocketReceivingLoop,
    logger: KLogger,
) : WebSocketLikeConnection {

    private val lock = ReentrantReadWriteLock()
    private val condition = lock.writeLock().newCondition()

    enum class State {
        WAITING,
        CONNECTED,
        CLOSED,
    }

    private var stateWithoutLock: State = State.WAITING
    protected val state: State
        get() = lock.read { stateWithoutLock }

    private var sessionWithoutLock: WebSocketSession? = null
    protected val session: WebSocketSession?
        get() = lock.read { sessionWithoutLock }

    override val isConnected: Boolean
        get() = state == State.CONNECTED

    private val server = embeddedServer(
        factory = Netty,
        host = configuration.host,
        port = configuration.port,
    ) {
        module(receivingLoop, logger, configuration)
    }.start()

    private fun Application.module(
        receivingLoop: WebSocketReceivingLoop,
        logger: KLogger,
        configuration: ReverseWebSocketConnectionConfiguration
    ) {
        install(WebSockets)

        // authorization
        intercept(ApplicationCallPipeline.Plugins) {
            val header = call.request.headers[AUTHORIZATION]
            val query = call.request.queryParameters[ACCESS_TOKEN]
            val authReceipt = auth(configuration.accessToken, logger, header, query, call.request.origin.remoteAddress)
            when (authReceipt) {
                AuthReceipt.SUCCESS -> lock.read {
                    when (stateWithoutLock) {
                        State.WAITING -> when (stateWithoutLock) {
                            State.WAITING -> return@intercept
                            State.CLOSED -> {
                                call.respond(HttpStatusCode.ResetContent, "Connection closed.")
                                return@intercept
                            }

                            State.CONNECTED -> {
                                call.respond(HttpStatusCode.ResetContent, "Connection already established.")
                                return@intercept
                            }
                        }

                        State.CLOSED -> {
                            call.respond(HttpStatusCode.ResetContent, "Connection closed.")
                            return@intercept
                        }

                        else -> throw IllegalStateException("Unexpected state: $stateWithoutLock")
                    }
                }
                AuthReceipt.FORMAT_ERROR, AuthReceipt.REQUIRED, AuthReceipt.TOKEN_ERROR -> {
                    val message = when (authReceipt) {
                        AuthReceipt.FORMAT_ERROR -> "Access token format error."
                        AuthReceipt.REQUIRED -> "Access token required."
                        AuthReceipt.TOKEN_ERROR -> "Access token error."
                        else -> throw IllegalStateException("Unexpected auth receipt: $authReceipt")
                    }
                    call.respond(HttpStatusCode.Unauthorized, message)
                    return@intercept
                }
            }
        }

        routing {
            webSocket(configuration.path) {
                lock.read {
                    when (stateWithoutLock) {
                        State.WAITING -> lock.upgrade {
                            sessionWithoutLock = this
                            stateWithoutLock = State.CONNECTED
                            condition.signalAll()
                        }

                        State.CLOSED -> {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Connection closed."))
                            return@webSocket
                        }

                        State.CONNECTED -> {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Connection already established."))
                            return@webSocket
                        }
                    }
                }

                try {
                    receivingLoop.receive(this, ::onReceive)
                } catch (_: CancellationException) {
                    // receiving cancelled, caused by closing
                    if (state != State.CLOSED) {
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
                            // if state is CONNECTING, it's because of error.
                            State.CONNECTED -> lock.upgrade {
                                sessionWithoutLock = null
                                stateWithoutLock = State.WAITING
                                condition.signalAll()
                            }

                            State.CLOSED -> lock.upgrade {
                                sessionWithoutLock = null
                                stateWithoutLock = State.CLOSED
                                condition.signalAll()
                            }

                            else -> throw IllegalStateException("Unexpected state: $stateWithoutLock")
                        }
                    }
                }
            }
        }
    }

    protected abstract suspend fun onReceive(node: JsonNode)

    override fun await(): ReverseWebSocketConnection {
        lock.write {
            condition.await()
        }
        return this
    }

    override suspend fun disconnect(reason: CloseReason) {
        val currentSession = session
        if (currentSession === null) {
            throw IllegalStateException("Connection is not established.")
        }

        currentSession.close(reason)
    }

    override fun close() {
        lock.read {
            when (stateWithoutLock) {
                State.CLOSED -> throw IllegalStateException("Connection already closed.")
                State.CONNECTED -> runBlocking {
                    disconnect(CloseReason(CloseReason.Codes.NORMAL, "Connection closed."))
                }
                State.WAITING -> Unit
            }
            lock.upgrade {
                stateWithoutLock = State.CLOSED
                sessionWithoutLock = null
                server.stop()
                job.cancel("Connection closed.")
            }
        }
    }
}
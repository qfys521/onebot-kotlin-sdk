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

package cn.chuanwise.onebot.lib.v11

import cn.chuanwise.onebot.lib.Action
import cn.chuanwise.onebot.lib.AppWebSocketReceivingLoop
import cn.chuanwise.onebot.lib.Expect
import cn.chuanwise.onebot.lib.deserializeTo
import cn.chuanwise.onebot.lib.v11.data.ASYNC_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.BAD_REQUEST_HTTP_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.BAD_REQUEST_WEB_SOCKET_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.SUCCESS_HTTP_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.SUCCESS_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.UNSUPPORTED_OPERATION_HTTP_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.UNSUPPORTED_OPERATION_WEB_SOCKET_RET_CODE
import cn.chuanwise.onebot.lib.v11.data.action.ActionRequestPack
import cn.chuanwise.onebot.lib.v11.data.action.ResponseData
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession

/**
 * The policy of calling defined in
 * [OneBot 11 API](https://github.com/botuniverse/onebot-11/blob/d4456ee706f9ada9c2dfde56a2bcfc69752600e4/api/README.md).
 *
 * @author Chuanwise
 */
enum class CallPolicy(val suffix: String = "") {
    // sync, or async (depends on action itself).
    DEFAULT,
    ASYNC("_async"),
    RATE_LIMITED("_rate_limited"),
}

fun throwResponseException(
    reason: String,
    message: String?,
    exceptionThrower: (String) -> Nothing
): Nothing {
    val exceptionMessage = if (message == null) "$reason. " else "$reason: $message"
    exceptionThrower(exceptionMessage)
}

private fun formatHttpStyleRetCodeWarning(
    got: Int,
    expect: Int
) = "(warning that OneBot implementation returns HTTP-style $got, not standard WebSocket-style $expect, " +
        "see https://github.com/botuniverse/onebot-11/blob/master/communication/ws.md)."

suspend fun <P> doCall(
    session: WebSocketSession?,
    receivingLoop: AppWebSocketReceivingLoop,
    objectMapper: ObjectMapper,
    logger: KLogger,
    expect: Expect<P, *>,
    params: P,
    callPolicy: CallPolicy
): ResponseData<JsonNode?> {

    if (expect !is Action) {
        throw IllegalArgumentException("The expect must be an action for app connection.")
    }
    if (session === null) {
        throw IllegalStateException("Connection is not established.")
    }

    val channel = receivingLoop.allocateChannel()
    val uuid = receivingLoop.registerChannel(channel)

    try {
        val pack = ActionRequestPack(
            action = expect.name + callPolicy.suffix,
            params = params,
            echo = uuid.toString()
        )
        val string = objectMapper.writeValueAsString(pack)

        logger.debug { "Sending text: $string" }
        session.send(Frame.Text(string))

        val node = channel.receive()
        val resp = node.deserializeTo<ResponseData<JsonNode?>>(objectMapper)

        return when (resp.retCode) {
            SUCCESS_RET_CODE, ASYNC_RET_CODE -> resp
            SUCCESS_HTTP_RET_CODE -> resp

            UNSUPPORTED_OPERATION_WEB_SOCKET_RET_CODE -> throwResponseException("Unsupported operation", resp.message) {
                throw UnsupportedOperationException(it)
            }

            UNSUPPORTED_OPERATION_HTTP_RET_CODE -> throwResponseException("Unsupported operation", resp.message) {
                throw UnsupportedOperationException(
                    it + formatHttpStyleRetCodeWarning(
                        resp.retCode, UNSUPPORTED_OPERATION_WEB_SOCKET_RET_CODE
                    )
                )
            }

            BAD_REQUEST_WEB_SOCKET_RET_CODE -> throwResponseException("Bad request", resp.message) {
                throw IllegalArgumentException(it)
            }

            BAD_REQUEST_HTTP_RET_CODE -> throwResponseException("Bad request", resp.message) {
                throw IllegalArgumentException(
                    it + formatHttpStyleRetCodeWarning(
                        resp.retCode, BAD_REQUEST_WEB_SOCKET_RET_CODE
                    )
                )
            }

            else -> throw NoSuchElementException("Unexpected response return code: ${resp.retCode}.")
        }
    } finally {
        receivingLoop.unregisterChannel(uuid)
    }
}

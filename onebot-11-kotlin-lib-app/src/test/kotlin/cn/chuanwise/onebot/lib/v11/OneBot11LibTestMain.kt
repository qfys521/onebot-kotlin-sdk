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

import cn.chuanwise.onebot.lib.TEXT
import cn.chuanwise.onebot.lib.awaitUtilConnected
import cn.chuanwise.onebot.lib.v11.data.message.SingleMessageData
import cn.chuanwise.onebot.lib.v11.data.message.TextData
import cn.chuanwise.onebot.lib.v11.utils.getObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.io.FileInputStream

suspend fun main() {
    val filePath = "onebot-11-kotlin-lib-app/src/test/resources/configurations.json"

    val objectMapper = getObjectMapper()
    val configurations = withContext(Dispatchers.IO) {
        FileInputStream(filePath).use {
            objectMapper.readValue<OneBot11LibTestConfiguration>(it)
        }
    }

    val job = SupervisorJob()
    val coroutineScope = CoroutineScope(job + Dispatchers.IO)

    val connection = OneBot11AppWebSocketConnection(
        configurations.appWebSocketConnection,
        job, coroutineScope.coroutineContext
    ).awaitUtilConnected()

    val helloWorldTextMessage = SingleMessageData(
        type = TEXT,
        TextData("Hello World!")
    )
    connection.sendGroupMessage(configurations.botIsAdminGroupId, helloWorldTextMessage)

    // if commented, forget to close
//    connection.close()
//    coroutineScope.cancel("Test finished.")
}
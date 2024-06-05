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

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KLogger

abstract class AppReverseWebSocketConnection private constructor(
    protected val receivingLoop: AppWebSocketReceivingLoop,
    logger: KLogger,
    configuration: ReverseWebSocketConnectionConfiguration
) : ReverseWebSocketConnection(receivingLoop, logger, configuration), AppConnection {

    constructor(
        objectMapper: ObjectMapper,
        logger: KLogger,
        configuration: ReverseWebSocketConnectionConfiguration,
    ) : this(
        AppWebSocketReceivingLoop(objectMapper, logger),
        logger,
        configuration,
    )

    override fun close() {
        try {
            receivingLoop.close()
        } finally {
            super.close()
        }
    }
}

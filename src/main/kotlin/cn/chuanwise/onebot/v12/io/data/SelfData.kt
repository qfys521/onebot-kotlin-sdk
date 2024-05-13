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

package cn.chuanwise.onebot.v12.io.data

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * # Self Data
 *
 * Self data used to identify the bot in multi-bots shared connections in
 * [OneBot 12](https://12.onebot.dev/connect/data-protocol/basic-types/).
 *
 * @author Chuanwise
 */
data class SelfData(
    @JsonProperty("platform")
    val platform: String,

    @JsonProperty("user_id")
    val userID: String
)
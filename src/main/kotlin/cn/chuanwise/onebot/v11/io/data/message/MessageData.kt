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

package cn.chuanwise.onebot.v11.io.data.message

import cn.chuanwise.onebot.io.data.JacksonObject
import cn.chuanwise.onebot.io.data.deserializeTo
import cn.chuanwise.onebot.io.data.toPrimitive
import cn.chuanwise.onebot.v11.io.data.ANONYMOUS
import cn.chuanwise.onebot.v11.io.data.AT
import cn.chuanwise.onebot.v11.io.data.AUDIO
import cn.chuanwise.onebot.v11.io.data.AUTO_ESCAPE
import cn.chuanwise.onebot.v11.io.data.CACHE
import cn.chuanwise.onebot.v11.io.data.CONTACT
import cn.chuanwise.onebot.v11.io.data.CONTENT
import cn.chuanwise.onebot.v11.io.data.CUSTOM
import cn.chuanwise.onebot.v11.io.data.DATA
import cn.chuanwise.onebot.v11.io.data.DICE
import cn.chuanwise.onebot.v11.io.data.FACE
import cn.chuanwise.onebot.v11.io.data.FILE
import cn.chuanwise.onebot.v11.io.data.FORWARD
import cn.chuanwise.onebot.v11.io.data.GROUP
import cn.chuanwise.onebot.v11.io.data.ID
import cn.chuanwise.onebot.v11.io.data.IMAGE
import cn.chuanwise.onebot.v11.io.data.JSON
import cn.chuanwise.onebot.v11.io.data.LAT
import cn.chuanwise.onebot.v11.io.data.LOCATION
import cn.chuanwise.onebot.v11.io.data.LON
import cn.chuanwise.onebot.v11.io.data.MUSIC
import cn.chuanwise.onebot.v11.io.data.NAME
import cn.chuanwise.onebot.v11.io.data.NICKNAME
import cn.chuanwise.onebot.v11.io.data.NODE
import cn.chuanwise.onebot.v11.io.data.POKE
import cn.chuanwise.onebot.v11.io.data.PROXY
import cn.chuanwise.onebot.v11.io.data.QQ
import cn.chuanwise.onebot.v11.io.data.RECORD
import cn.chuanwise.onebot.v11.io.data.REPLY
import cn.chuanwise.onebot.v11.io.data.RPS
import cn.chuanwise.onebot.v11.io.data.SHAKE
import cn.chuanwise.onebot.v11.io.data.SHARE
import cn.chuanwise.onebot.v11.io.data.TEXT
import cn.chuanwise.onebot.v11.io.data.TIMEOUT
import cn.chuanwise.onebot.v11.io.data.TITLE
import cn.chuanwise.onebot.v11.io.data.TYPE
import cn.chuanwise.onebot.v11.io.data.URL
import cn.chuanwise.onebot.v11.io.data.USER_ID
import cn.chuanwise.onebot.v11.io.data.VIDEO
import cn.chuanwise.onebot.v11.io.data.XML
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer


/**
 * # Message Data
 *
 * Message low-level representation based on
 * [OneBot 11 Message Data](https://github.com/botuniverse/onebot-11/blob/master/message/segment.md).
 *
 * @author Chuanwise
 */
@JsonDeserialize(using = MessageDataDeserializer::class)
sealed class MessageData

@JsonSerialize(using = CQCodeMessageDataSerializer::class)
@JsonDeserialize(using = CQCodeMessageDataDeserializer::class)
data class CQCodeMessageData(
    val code: String,

    @JsonProperty(AUTO_ESCAPE)
    val autoEscape: Boolean = false
) : MessageData()

object CQCodeMessageDataDeserializer : StdDeserializer<CQCodeMessageData>(CQCodeMessageData::class.java) {
    private fun readResolve(): Any = CQCodeMessageDataDeserializer
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CQCodeMessageData {
        val node = p.codec.readTree<JsonNode>(p)
        return if (node is TextNode) {
            CQCodeMessageData(p.readValueAs(String::class.java))
        } else if (node is ObjectNode) {
            val value = JacksonObject(p.codec as ObjectMapper, node)
            CQCodeMessageData(
                code = value[DATA].toPrimitive().toString(),
                autoEscape = value[AUTO_ESCAPE].toPrimitive().toBoolean()
            )
        } else {
            throw IllegalArgumentException("Unexpected CQCode message data: $node")
        }
    }
}

object CQCodeMessageDataSerializer : StdSerializer<CQCodeMessageData>(CQCodeMessageData::class.java) {
    private fun readResolve(): Any = CQCodeMessageDataSerializer
    override fun serialize(value: CQCodeMessageData, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.code)
    }
}

data class SingleMessageData(
    @JsonProperty(TYPE)
    val type: String,

    @JsonProperty(DATA)
    val data: SegmentData
) : MessageData()

data class ArrayMessageData(
    @JsonProperty(DATA)
    val data: List<SingleMessageData>
): MessageData()

sealed class SegmentData

data class TextData(
    @JsonProperty(TYPE)
    val text: String
) : SegmentData()


data class ImageData(
    @JsonProperty(FILE)
    val file: String,

    // "flash" or none
    @JsonProperty(TYPE)
    val type: String?,

    // send only
    @JsonProperty(URL)
    val url: String?,

    // receive only
    @JsonProperty(CACHE)
    val cache: Boolean?,

    // receive only
    @JsonProperty(PROXY)
    val proxy: Boolean?,

    // receive only
    @JsonProperty(TIMEOUT)
    val timeout: Long?
) : SegmentData()

data class SoundData(
    @JsonProperty(FILE)
    val file: String,

    @JsonProperty(TYPE)
    val magic: Boolean?,

    // send only
    @JsonProperty(URL)
    val url: String?,

    // receive only
    @JsonProperty(CACHE)
    val cache: Boolean?,

    // receive only
    @JsonProperty(PROXY)
    val proxy: Boolean?,

    // receive only
    @JsonProperty(TIMEOUT)
    val timeout: Long?
) : SegmentData()

data class VideoData(
    @JsonProperty(FILE)
    val file: String,

    // send only
    @JsonProperty(URL)
    val url: String?,

    // receive only
    @JsonProperty(CACHE)
    val cache: Boolean?,

    // receive only
    @JsonProperty(PROXY)
    val proxy: Boolean?,

    // receive only
    @JsonProperty(TIMEOUT)
    val timeout: Long?
) : SegmentData()

data class AtData(
    // qq code or "all"
    @JsonProperty(QQ)
    val qq: String
) : SegmentData()

// RPSData (Rock, Paper, Scissors)
// DiceData, ShakeData has empty body
data object EmptyData : SegmentData()

data class PokeData(
    // https://github.com/mamoe/mirai/blob/f5eefae7ecee84d18a66afce3f89b89fe1584b78/mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L49
    @JsonProperty(TYPE)
    val type: String,

    @JsonProperty(ID)
    val id: String,

    @JsonProperty(NAME)
    val name: String?
) : SegmentData()

data object AnonymousSendingTag : SegmentData()


data class ShareData(
    @JsonProperty(URL)
    val url: String,

    @JsonProperty(TITLE)
    val title: String,

    // optional if send
    @JsonProperty(CONTENT)
    val content: String?,

    // optional if send
    // image url
    @JsonProperty(IMAGE)
    val image: String?
) : SegmentData()


data class RecommendationData(
    // "qq", "group"
    // "qq", "163", "xm"
    @JsonProperty(TYPE)
    val type: String,

    @JsonProperty(NICKNAME)
    val id: String,
) : SegmentData()


data class LocationData(
    @JsonProperty(LAT)
    val lat: String,

    @JsonProperty(LON)
    val lon: String,

    // optional if send
    @JsonProperty(TITLE)
    val title: String?,

    // optional if send
    @JsonProperty(CONTENT)
    val content: String?
) : SegmentData()


data class CustomMusicRecommendationData(
    // "custom"
    @JsonProperty(TYPE)
    val type: String?,

    // jump url
    @JsonProperty(URL)
    val url: String?,

    // audio url
    @JsonProperty(AUDIO)
    val audio: String,

    // title url
    @JsonProperty(TITLE)
    val title: String,

    @JsonProperty(CONTENT)
    val content: String,

    // cover url
    @JsonProperty(IMAGE)
    val image: String,
) : SegmentData()


data class IDTag(
    @JsonProperty(ID)
    val id: String
) : SegmentData()


data class SingleForwardNodeData(
    @JsonProperty(USER_ID)
    val userID: String,

    @JsonProperty(NICKNAME)
    val nickname: String,

    @JsonProperty(CONTENT)
    val content: MessageData
) : SegmentData()


data class MultiForwardNodeData(
    @JsonProperty(ID)
    val id: String,

    @JsonProperty(CONTENT)
    val content: List<SegmentData>
) : SegmentData()


data class SerializedData(
    // xml or json
    @JsonProperty(DATA)
    val data: String
) : SegmentData()

object MessageDataDeserializer : StdDeserializer<MessageData>(MessageData::class.java) {
    private fun readResolve(): Any = MessageDataDeserializer
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MessageData {
        return when (val node = p.codec.readTree<JsonNode>(p)) {
            is ArrayNode -> ArrayMessageData(
                data = node.map {
                    p.readValueAs(SingleMessageData::class.java)
                }
            )

            is ObjectNode -> {
                val value = JacksonObject(p.codec as ObjectMapper, node)
                val type = value[TYPE].toPrimitive().toString()
                SingleMessageData(
                    type = type,
                    data = when (type) {
                        TEXT -> value.deserializeTo<TextData>()
                        IMAGE -> value.deserializeTo<ImageData>()
                        RECORD -> value.deserializeTo<SoundData>()
                        VIDEO -> value.deserializeTo<VideoData>()
                        AT -> value.deserializeTo<AtData>()
                        RPS, DICE, SHAKE, ANONYMOUS -> EmptyData
                        POKE -> value.deserializeTo<PokeData>()
                        SHARE -> value.deserializeTo<ShareData>()
                        CONTACT, GROUP -> value.deserializeTo<RecommendationData>()
                        LOCATION -> value.deserializeTo<LocationData>()
                        MUSIC -> if (value[TYPE].toPrimitive().toString() == CUSTOM) {
                            value.deserializeTo<CustomMusicRecommendationData>()
                        } else {
                            value.deserializeTo<RecommendationData>()
                        }

                        REPLY, FORWARD, FACE -> value.deserializeTo<IDTag>()
                        NODE -> if (ID in value) {
                            value.deserializeTo<IDTag>()
                        } else {
                            value.deserializeTo<SingleForwardNodeData>()
                        }

                        XML, JSON -> value.deserializeTo<SerializedData>()
                        else -> throw IllegalArgumentException("Unexpected message type: $type")
                    }
                )
            }

            else -> throw IllegalArgumentException("Unexpected message data: $node")
        }
    }
}
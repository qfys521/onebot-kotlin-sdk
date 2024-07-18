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

package cn.chuanwise.onebot.lib.v11.data.action

import cn.chuanwise.onebot.lib.v11.data.event.AnonymousSenderData
import cn.chuanwise.onebot.lib.v11.data.event.SenderData
import cn.chuanwise.onebot.lib.v11.data.message.MessageData
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class MessageIdData(
    val messageId: Int
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IdData(
    val id: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SendGroupMessageData(
    val groupId: Long,
    val message: MessageData,
    val autoEscape: Boolean = false,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SendPrivateMessageData(
    val userId: Long,
    val message: MessageData,
    val autoEscape: Boolean = false,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SendMessageData(
    // "private" or "group"
    val messageType: String?,

    // if not null, send to this user
    val userId: Long?,

    // if not null, send to this group
    val groupId: Long?,
    val message: MessageData,
    val autoEscape: Boolean = false,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetMessageData(
    val time: Int,

    // "private" or "group"
    val messageType: String?,
    val messageId: Int,
    val realId: Int,
    val sender: SenderData,
    val message: MessageData,
)

data class MessageDataWrapper(
    val message: MessageData,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SendLikeData(
    val userId: Long,
    val times: Int,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupKickData(
    val groupId: Long,
    val userId: Long,
    val rejectAddRequest: Boolean = false,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupBanData(
    val groupId: Long,
    val userId: Long,

    // seconds, 0 to release
    val duration: Long = 30 * 60
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupAnonymousBanData(
    val groupId: Long,
    val anonymous: AnonymousSenderData?,
    val flag: String?,

    // seconds, 0 to release
    val duration: Long = 30 * 60
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupIdEnableData(
    val groupId: Long,
    val enable: Boolean = true
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupIdUserIdEnableData(
    val groupId: Long,
    val userId: Long,
    val enable: Boolean = true
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupCardData(
    val groupId: Long,
    val userId: Long,
    val card: String? = null
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupNameData(
    val groupId: Long,
    val groupName: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupLeaveData(
    val groupId: Long,
    val isDismiss: Boolean = false
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupSpecialTitleData(
    val groupId: Long,
    val userId: Long,
    val specialTitle: String? = null,

    // seconds, 0 to release
    val duration: Long = -1
)

data class SetFriendAddRequestData(
    val flag: String,
    val approve: Boolean = true,
    val remark: String? = null
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SetGroupAddRequestData(
    val flag: String,
    val subType: String,
    val approve: Boolean = true,
    val reason: String? = null
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetLoginInfoData(
    val userId: Long,
    val nickname: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetStrangerInfoData(
    val userId: Long,
    val noCache: Boolean = false
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class FriendListElement(
    val userId: Long,
    val nickname: String,
    val remark: String? = null
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetGroupInfoData(
    val groupId: Long,
    val noCache: Boolean = false
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetGroupInfoResponseData(
    val groupId: Long,
    val groupName: String,
    val memberCount: Int,
    val maxMemberCount: Int,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetGroupMemberInfoData(
    val groupId: Long,
    val userId: Long,
    val noCache: Boolean = false
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupMemberData(
    val groupId: Long,
    val userId: Long,
    val nickname: String,
    val card: String?,
    val sex: String?,
    val age: Int?,
    val area: String?,
    val joinTime: Int,
    val lastSentTime: Int,
    val level: String,
    val role: String,
    val unfriendly: Boolean,
    val title: String?,
    val titleExpireTime: Int?,
    val cardChangeable: Boolean
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupIdData(
    val groupId: Long
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetGroupHonorInfoData(
    val groupId: Long,

    // "talkative", "performer", "legend", "strong_newbie", "emotion" or "all"
    val type: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetGroupHonorInfoResponseData(
    val groupId: Long,
    val currentTalkative: GroupCurrentTalkativeData?,
    val talkativeList: List<GroupHonorOwner>,
    val performerList: List<GroupHonorOwner>,
    val legendList: List<GroupHonorOwner>,
    val strongNewbieList: List<GroupHonorOwner>,
    val emotionList: List<GroupHonorOwner>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupCurrentTalkativeData(
    val userId: Long,
    val nickname: String,
    val avatar: String,
    val dayCount: Int,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GroupHonorOwner(
    val userId: Long,
    val honor: String,
    val avatar: Int,
    val description: String,
)

data class DomainData(
    val domain: String
)

data class CookiesData(
    val cookies: String
)

data class GetCSRFTokenData(
    val token: String
)

data class GetCredentialsData(
    val cookies: String,
    val token: String
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetRecordData(
    val file: String,

    // "mp3", "amr", "wma", "m4a", "spx", "ogg", "wav" or "flac"
    val outFormat: String,
)

data class FileData(
    val file: String
)

data class YesOrNoData(
    val yes: Boolean
)

data class DelayData(
    val delay: Int
)

data class HandleQuickOperationData(
    val context: Any,
    val operation: Any,
)
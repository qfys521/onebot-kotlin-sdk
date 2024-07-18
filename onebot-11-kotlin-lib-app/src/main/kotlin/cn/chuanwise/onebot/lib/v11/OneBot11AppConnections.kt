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

@file:JvmName("OneBot11AppConnections")

package cn.chuanwise.onebot.lib.v11

import cn.chuanwise.onebot.lib.v11.data.action.DelayData
import cn.chuanwise.onebot.lib.v11.data.action.DomainData
import cn.chuanwise.onebot.lib.v11.data.action.FileData
import cn.chuanwise.onebot.lib.v11.data.action.GetGroupHonorInfoData
import cn.chuanwise.onebot.lib.v11.data.action.GetGroupInfoData
import cn.chuanwise.onebot.lib.v11.data.action.GetRecordData
import cn.chuanwise.onebot.lib.v11.data.action.GetStrangerInfoData
import cn.chuanwise.onebot.lib.v11.data.action.GroupIdData
import cn.chuanwise.onebot.lib.v11.data.action.GroupIdEnableData
import cn.chuanwise.onebot.lib.v11.data.action.GroupIdUserIdEnableData
import cn.chuanwise.onebot.lib.v11.data.action.IdData
import cn.chuanwise.onebot.lib.v11.data.action.MessageIdData
import cn.chuanwise.onebot.lib.v11.data.action.SendGroupMessageData
import cn.chuanwise.onebot.lib.v11.data.action.SendLikeData
import cn.chuanwise.onebot.lib.v11.data.action.SendMessageData
import cn.chuanwise.onebot.lib.v11.data.action.SendPrivateMessageData
import cn.chuanwise.onebot.lib.v11.data.action.SetFriendAddRequestData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupAddRequestData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupAnonymousBanData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupBanData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupCardData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupKickData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupLeaveData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupNameData
import cn.chuanwise.onebot.lib.v11.data.action.SetGroupSpecialTitleData
import cn.chuanwise.onebot.lib.v11.data.event.AnonymousSenderData
import cn.chuanwise.onebot.lib.v11.data.message.CqCodeMessageData
import cn.chuanwise.onebot.lib.v11.data.message.MessageData

/**
 * @see [SEND_PRIVATE_MESSAGE]
 */
suspend fun OneBot11AppConnection.sendPrivateMessage(userId: Long, message: MessageData): Int = call(
    SEND_PRIVATE_MESSAGE, SendPrivateMessageData(
        userId = userId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
).messageId

suspend fun OneBot11AppConnection.sendPrivateMessageAsync(userId: Long, message: MessageData) = callAsync(
    SEND_PRIVATE_MESSAGE, SendPrivateMessageData(
        userId = userId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

suspend fun OneBot11AppConnection.sendPrivateMessageRateLimited(userId: Long, message: MessageData) = callRateLimited(
    SEND_PRIVATE_MESSAGE, SendPrivateMessageData(
        userId = userId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

/**
 * @see [SEND_GROUP_MESSAGE]
 */
suspend fun OneBot11AppConnection.sendGroupMessage(groupId: Long, message: MessageData): Int = call(
    SEND_GROUP_MESSAGE, SendGroupMessageData(
        groupId = groupId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
).messageId

suspend fun OneBot11AppConnection.sendGroupMessageAsync(groupId: Long, message: MessageData) = callAsync(
    SEND_GROUP_MESSAGE, SendGroupMessageData(
        groupId = groupId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

suspend fun OneBot11AppConnection.sendGroupMessageRateLimited(groupId: Long, message: MessageData) = callRateLimited(
    SEND_GROUP_MESSAGE, SendGroupMessageData(
        groupId = groupId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

/**
 * @see [SEND_MESSAGE]
 */
suspend fun OneBot11AppConnection.sendMessage(
    messageType: String,
    userId: Long?,
    groupId: Long?,
    message: MessageData
): Int =
    call(
        SEND_MESSAGE, SendMessageData(
            messageType = messageType,
            userId = userId,
            groupId = groupId,
            message = message,
            autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
        )
    ).messageId

suspend fun OneBot11AppConnection.sendMessageAsync(
    messageType: String,
    userId: Long?,
    groupId: Long?,
    message: MessageData
) = callAsync(
    SEND_MESSAGE, SendMessageData(
        messageType = messageType,
        userId = userId,
        groupId = groupId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

suspend fun OneBot11AppConnection.sendMessageRateLimited(
    messageType: String,
    userId: Long?,
    groupId: Long?,
    message: MessageData
) = callRateLimited(
    SEND_MESSAGE, SendMessageData(
        messageType = messageType,
        userId = userId,
        groupId = groupId,
        message = message,
        autoEscape = (message as? CqCodeMessageData)?.autoEscape ?: false
    )
)

/**
 * @see [DELETE_MESSAGE]
 */
suspend fun OneBot11AppConnection.deleteMessage(messageId: Int) = call(
    DELETE_MESSAGE, MessageIdData(messageId)
)

suspend fun OneBot11AppConnection.deleteMessageAsync(messageId: Int) = callAsync(
    DELETE_MESSAGE, MessageIdData(messageId)
)

suspend fun OneBot11AppConnection.deleteMessageRateLimited(messageId: Int) = callRateLimited(
    DELETE_MESSAGE, MessageIdData(messageId)
)

/**
 * @see [GET_MESSAGE]
 */
suspend fun OneBot11AppConnection.getMessage(messageId: Int) = call(
    GET_MESSAGE, MessageIdData(messageId)
)

/**
 * @see [GET_FORWARD_MESSAGE]
 */
suspend fun OneBot11AppConnection.getForwardMessage(id: String) = call(
    GET_FORWARD_MESSAGE, IdData(id)
).message

/**
 * @see [SEND_LIKE]
 */
suspend fun OneBot11AppConnection.sendLike(userId: Long, times: Int) = call(
    SEND_LIKE, SendLikeData(
        userId = userId,
        times = times
    )
)

suspend fun OneBot11AppConnection.sendLikeAsync(userId: Long, times: Int) = callAsync(
    SEND_LIKE, SendLikeData(
        userId = userId,
        times = times
    )
)

suspend fun OneBot11AppConnection.sendLikeRateLimited(userId: Long, times: Int) = callRateLimited(
    SEND_LIKE, SendLikeData(
        userId = userId,
        times = times
    )
)

/**
 * @see [SET_GROUP_KICK]
 */
suspend fun OneBot11AppConnection.setGroupKick(groupId: Long, userId: Long, rejectAddRequest: Boolean) = call(
    SET_GROUP_KICK, SetGroupKickData(
        groupId = groupId,
        userId = userId,
        rejectAddRequest = rejectAddRequest
    )
)

suspend fun OneBot11AppConnection.setGroupKickAsync(groupId: Long, userId: Long, rejectAddRequest: Boolean) = callAsync(
    SET_GROUP_KICK, SetGroupKickData(
        groupId = groupId,
        userId = userId,
        rejectAddRequest = rejectAddRequest
    )
)

suspend fun OneBot11AppConnection.setGroupKickRateLimited(groupId: Long, userId: Long, rejectAddRequest: Boolean) =
    callRateLimited(
        SET_GROUP_KICK, SetGroupKickData(
            groupId = groupId,
            userId = userId,
            rejectAddRequest = rejectAddRequest
        )
    )

/**
 * @see [SET_GROUP_BAN]
 */
suspend fun OneBot11AppConnection.setGroupBan(groupId: Long, userId: Long, duration: Long) = call(
    SET_GROUP_BAN, SetGroupBanData(
        groupId = groupId,
        userId = userId,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupBanAsync(groupId: Long, userId: Long, duration: Long) = callAsync(
    SET_GROUP_BAN, SetGroupBanData(
        groupId = groupId,
        userId = userId,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupBanRateLimited(groupId: Long, userId: Long, duration: Long) = callRateLimited(
    SET_GROUP_BAN, SetGroupBanData(
        groupId = groupId,
        userId = userId,
        duration = duration
    )
)

/**
 * @see [SET_GROUP_ANONYMOUS_BAN]
 */
suspend fun OneBot11AppConnection.setGroupAnonymousBan(groupId: Long, flag: String, duration: Long) = call(
    SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
        groupId = groupId,
        anonymous = null,
        flag = flag,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupAnonymousBanAsync(groupId: Long, flag: String, duration: Long) = callAsync(
    SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
        groupId = groupId,
        anonymous = null,
        flag = flag,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupAnonymousBanRateLimited(groupId: Long, flag: String, duration: Long) =
    callRateLimited(
        SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
            groupId = groupId,
            anonymous = null,
            flag = flag,
            duration = duration
        )
    )

/**
 * @see [SET_GROUP_ANONYMOUS_BAN]
 */
suspend fun OneBot11AppConnection.setGroupAnonymousBan(groupId: Long, sender: AnonymousSenderData, duration: Long) =
    call(
        SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
            groupId = groupId,
            anonymous = sender,
            flag = null,
            duration = duration
        )
    )

suspend fun OneBot11AppConnection.setGroupAnonymousBanAsync(
    groupId: Long,
    sender: AnonymousSenderData,
    duration: Long
) = callAsync(
    SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
        groupId = groupId,
        anonymous = sender,
        flag = null,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupAnonymousBanRateLimited(
    groupId: Long,
    sender: AnonymousSenderData,
    duration: Long
) = callRateLimited(
    SET_GROUP_ANONYMOUS_BAN, SetGroupAnonymousBanData(
        groupId = groupId,
        anonymous = sender,
        flag = null,
        duration = duration
    )
)

/**
 * @see [SET_GROUP_WHOLE_BAN]
 */
suspend fun OneBot11AppConnection.setGroupWholeBan(groupId: Long, enable: Boolean) = call(
    SET_GROUP_WHOLE_BAN, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupWholeBanAsync(groupId: Long, enable: Boolean) = callAsync(
    SET_GROUP_WHOLE_BAN, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupWholeBanRateLimited(groupId: Long, enable: Boolean) = callRateLimited(
    SET_GROUP_WHOLE_BAN, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

/**
 * @see [SET_GROUP_ANONYMOUS]
 */
suspend fun OneBot11AppConnection.setGroupAdmin(groupId: Long, userId: Long, enable: Boolean) = call(
    SET_GROUP_ADMIN, GroupIdUserIdEnableData(
        groupId = groupId,
        userId = userId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupAdminAsync(groupId: Long, userId: Long, enable: Boolean) = callAsync(
    SET_GROUP_ADMIN, GroupIdUserIdEnableData(
        groupId = groupId,
        userId = userId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupAdminRateLimited(groupId: Long, userId: Long, enable: Boolean) =
    callRateLimited(
        SET_GROUP_ADMIN, GroupIdUserIdEnableData(
            groupId = groupId,
            userId = userId,
            enable = enable
        )
    )

/**
 * @see [SET_GROUP_ANONYMOUS]
 */
suspend fun OneBot11AppConnection.setGroupAnonymous(groupId: Long, enable: Boolean) = call(
    SET_GROUP_ANONYMOUS, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupAnonymousAsync(groupId: Long, enable: Boolean) = callAsync(
    SET_GROUP_ANONYMOUS, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

suspend fun OneBot11AppConnection.setGroupAnonymousRateLimited(groupId: Long, enable: Boolean) = callRateLimited(
    SET_GROUP_ANONYMOUS, GroupIdEnableData(
        groupId = groupId,
        enable = enable
    )
)

/**
 * @see [SET_GROUP_CARD]
 */
suspend fun OneBot11AppConnection.setGroupCard(groupId: Long, userId: Long, card: String) = call(
    SET_GROUP_CARD, SetGroupCardData(
        groupId = groupId,
        userId = userId,
        card = card
    )
)

suspend fun OneBot11AppConnection.setGroupCardAsync(groupId: Long, userId: Long, card: String) = callAsync(
    SET_GROUP_CARD, SetGroupCardData(
        groupId = groupId,
        userId = userId,
        card = card
    )
)

suspend fun OneBot11AppConnection.setGroupCardRateLimited(groupId: Long, userId: Long, card: String) = callRateLimited(
    SET_GROUP_CARD, SetGroupCardData(
        groupId = groupId,
        userId = userId,
        card = card
    )
)

/**
 * @see [SET_GROUP_NAME]
 */
suspend fun OneBot11AppConnection.setGroupName(groupId: Long, groupName: String) = call(
    SET_GROUP_NAME, SetGroupNameData(
        groupId = groupId,
        groupName = groupName
    )
)

suspend fun OneBot11AppConnection.setGroupNameAsync(groupId: Long, groupName: String) = callAsync(
    SET_GROUP_NAME, SetGroupNameData(
        groupId = groupId,
        groupName = groupName
    )
)

suspend fun OneBot11AppConnection.setGroupNameRateLimited(groupId: Long, groupName: String) = callRateLimited(
    SET_GROUP_NAME, SetGroupNameData(
        groupId = groupId,
        groupName = groupName
    )
)

/**
 * @see [SET_GROUP_LEAVE]
 */
suspend fun OneBot11AppConnection.setGroupLeave(groupId: Long, isDismiss: Boolean) = call(
    SET_GROUP_LEAVE, SetGroupLeaveData(
        groupId = groupId,
        isDismiss = isDismiss
    )
)

suspend fun OneBot11AppConnection.setGroupLeaveAsync(groupId: Long, isDismiss: Boolean) = callAsync(
    SET_GROUP_LEAVE, SetGroupLeaveData(
        groupId = groupId,
        isDismiss = isDismiss
    )
)

suspend fun OneBot11AppConnection.setGroupLeaveRateLimited(groupId: Long, isDismiss: Boolean) = callRateLimited(
    SET_GROUP_LEAVE, SetGroupLeaveData(
        groupId = groupId,
        isDismiss = isDismiss
    )
)

/**
 * @see [SET_GROUP_SPECIAL_TITLE]
 */
suspend fun OneBot11AppConnection.setGroupSpecialTitle(
    groupId: Long,
    userId: Long,
    specialTitle: String,
    duration: Long
) =
    call(
        SET_GROUP_SPECIAL_TITLE, SetGroupSpecialTitleData(
            groupId = groupId,
            userId = userId,
            specialTitle = specialTitle,
            duration = duration
        )
    )

suspend fun OneBot11AppConnection.setGroupSpecialTitleAsync(
    groupId: Long,
    userId: Long,
    specialTitle: String,
    duration: Long
) = callAsync(
    SET_GROUP_SPECIAL_TITLE, SetGroupSpecialTitleData(
        groupId = groupId,
        userId = userId,
        specialTitle = specialTitle,
        duration = duration
    )
)

suspend fun OneBot11AppConnection.setGroupSpecialTitleRateLimited(
    groupId: Long,
    userId: Long,
    specialTitle: String,
    duration: Long
) = callRateLimited(
    SET_GROUP_SPECIAL_TITLE, SetGroupSpecialTitleData(
        groupId = groupId,
        userId = userId,
        specialTitle = specialTitle,
        duration = duration
    )
)

/**
 * @see [SET_FRIEND_ADD_REQUEST]
 */
suspend fun OneBot11AppConnection.setFriendAddRequest(flag: String, approve: Boolean, remark: String? = null) = call(
    SET_FRIEND_ADD_REQUEST, SetFriendAddRequestData(
        flag = flag,
        approve = approve,
        remark = remark
    )
)

suspend fun OneBot11AppConnection.setFriendAddRequestAsync(flag: String, approve: Boolean, remark: String? = null) =
    callAsync(
        SET_FRIEND_ADD_REQUEST, SetFriendAddRequestData(
            flag = flag,
            approve = approve,
            remark = remark
        )
    )

suspend fun OneBot11AppConnection.setFriendAddRequestRateLimited(
    flag: String,
    approve: Boolean,
    remark: String? = null
) =
    callRateLimited(
        SET_FRIEND_ADD_REQUEST, SetFriendAddRequestData(
            flag = flag,
            approve = approve,
            remark = remark
        )
    )

/**
 * @see [SET_GROUP_ADD_REQUEST]
 */
suspend fun OneBot11AppConnection.setGroupAddRequest(flag: String, subType: String, approve: Boolean, reason: String) =
    call(
        SET_GROUP_ADD_REQUEST, SetGroupAddRequestData(
            flag = flag,
            approve = approve,
            subType = subType,
            reason = reason
        )
    )

suspend fun OneBot11AppConnection.setGroupAddRequestAsync(
    flag: String,
    subType: String,
    approve: Boolean,
    reason: String
) = callAsync(
    SET_GROUP_ADD_REQUEST, SetGroupAddRequestData(
        flag = flag,
        approve = approve,
        subType = subType,
        reason = reason
    )
)

suspend fun OneBot11AppConnection.setGroupAddRequestRateLimited(
    flag: String,
    subType: String,
    approve: Boolean,
    reason: String
) = callRateLimited(
    SET_GROUP_ADD_REQUEST, SetGroupAddRequestData(
        flag = flag,
        approve = approve,
        subType = subType,
        reason = reason
    )
)

/**
 * @see [GET_LOGIN_INFO]
 */
suspend fun OneBot11AppConnection.getLoginInfo() = call(
    GET_LOGIN_INFO, Unit
)

/**
 * @see [GET_STRANGER_INFO]
 */
suspend fun OneBot11AppConnection.getStrangerInfo(userId: Long, noCache: Boolean) = call(
    GET_STRANGER_INFO, GetStrangerInfoData(
        userId = userId,
        noCache = noCache
    )
)

/**
 * @see [GET_FRIEND_LIST]
 */
suspend fun OneBot11AppConnection.getGroupInfo(groupId: Long, noCache: Boolean) = call(
    GET_GROUP_INFO, GetGroupInfoData(
        groupId = groupId,
        noCache = noCache
    )
)

/**
 * @see [GET_FRIEND_LIST]
 */
suspend fun OneBot11AppConnection.getFriendList() = call(
    GET_FRIEND_LIST, Unit
)

/**
 * @see [GET_GROUP_LIST]
 */
suspend fun OneBot11AppConnection.getGroupList() = call(
    GET_GROUP_LIST, Unit
)

/**
 * @see [GET_GROUP_MEMBER_INFO]
 */
suspend fun OneBot11AppConnection.getGroupMemberList(groupId: Long) = call(
    GET_GROUP_MEMBER_LIST, GroupIdData(groupId)
)

/**
 * @see [GET_GROUP_HONOR_INFO]
 */
suspend fun OneBot11AppConnection.getGroupHonorInfo(groupId: Long, type: String) = call(
    GET_GROUP_HONOR_INFO, GetGroupHonorInfoData(
        groupId = groupId,
        type = type
    )
)

/**
 * @see [GET_COOKIES]
 */
suspend fun OneBot11AppConnection.getCookies(domain: String) = call(
    GET_COOKIES, DomainData(domain)
)

/**
 * @see [GET_CSRF_TOKEN]
 */
suspend fun OneBot11AppConnection.getCSRFToken() = call(
    GET_CSRF_TOKEN, Unit
)

/**
 * @see [GET_CREDENTIALS]
 */
suspend fun OneBot11AppConnection.getCredentials(domain: String) = call(
    GET_CREDENTIALS, DomainData(domain)
)

/**
 * @see [GET_RECORD]
 */
suspend fun OneBot11AppConnection.getRecord(file: String, outFormat: String) = call(
    GET_RECORD, GetRecordData(
        file = file,
        outFormat = outFormat
    )
)

/**
 * @see [GET_IMAGE]
 */
suspend fun OneBot11AppConnection.getImage(file: String) = call(
    GET_IMAGE, FileData(
        file = file
    )
)

/**
 * @see [CAN_SEND_IMAGE]
 */
suspend fun OneBot11AppConnection.canSendImage() = call(
    CAN_SEND_IMAGE, Unit
)

/**
 * @see [CAN_SEND_RECORD]
 */
suspend fun OneBot11AppConnection.canSendRecord() = call(
    CAN_SEND_RECORD, Unit
)

/**
 * @see [GET_STATUS]
 */
suspend fun OneBot11AppConnection.getStatus() = call(
    GET_STATUS, Unit
)

/**
 * @see [GET_VERSION_INFO]
 */
suspend fun OneBot11AppConnection.getVersionInfo() = call(
    GET_VERSION_INFO, Unit
)

/**
 * @see [SET_RESTART]
 */
suspend fun OneBot11AppConnection.setRestart(delay: Int) = call(
    SET_RESTART, DelayData(delay)
)

/**
 * @see [CLEAN_CACHE]
 */
suspend fun OneBot11AppConnection.cleanCache() = call(
    CLEAN_CACHE, Unit
)

suspend fun OneBot11AppConnection.cleanCacheAsync() = callAsync(
    CLEAN_CACHE, Unit
)
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

import cn.chuanwise.onebot.lib.AT
import cn.chuanwise.onebot.lib.FACE
import cn.chuanwise.onebot.lib.FLASH
import cn.chuanwise.onebot.lib.FORWARD
import cn.chuanwise.onebot.lib.GROUP
import cn.chuanwise.onebot.lib.IMAGE
import cn.chuanwise.onebot.lib.PRIVATE
import cn.chuanwise.onebot.lib.RECORD
import cn.chuanwise.onebot.lib.TEXT
import cn.chuanwise.onebot.lib.awaitUtilConnected
import cn.chuanwise.onebot.lib.v11.data.event.FriendAddRequestQuickOperationData
import cn.chuanwise.onebot.lib.v11.data.event.GroupMessageMessageMessageQuickOperationData
import cn.chuanwise.onebot.lib.v11.data.message.ArrayMessageData
import cn.chuanwise.onebot.lib.v11.data.message.AtData
import cn.chuanwise.onebot.lib.v11.data.message.CqCodeMessageData
import cn.chuanwise.onebot.lib.v11.data.message.IdData
import cn.chuanwise.onebot.lib.v11.data.message.ImageData
import cn.chuanwise.onebot.lib.v11.data.message.RecordData
import cn.chuanwise.onebot.lib.v11.data.message.SingleMessageData
import cn.chuanwise.onebot.lib.v11.data.message.TextData
import cn.chuanwise.onebot.lib.v11.utils.getObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.URL

class OneBot11LibTest {
    companion object {
        private val objectMapper = getObjectMapper()
        private val configurations = objectMapper.readValue<OneBot11LibTestConfiguration>(
            getResourceURL("configurations.json")
        )

        private val logger = KotlinLogging.logger { }

        private val appWebSocketConnection: OneBot11AppWebSocketConnection by lazy {
            OneBot11AppWebSocketConnection(configurations.appWebSocketConnection).awaitUtilConnected()
        }
        private val appReverseWebSocketConnection: OneBot11AppReverseWebSocketConnection by lazy {
            OneBot11AppReverseWebSocketConnection(configurations.appReverseWebSocketConnection).awaitUtilConnected()
        }

        private val appConnection = appReverseWebSocketConnection

        @JvmStatic
        fun getResourceURL(path: String): URL {
            val resourceURL = Companion::class.java.classLoader.getResource(path)
            if (resourceURL === null) {
                val examplePath = "$path.example"
                throw IllegalStateException(
                    "Cannot find resource: $path, edit and rename `$examplePath` to `$path` " +
                            "in the test resources directory `onebot-11-kotlin-lib-app/src/test/resources` and try again."
                )
            }
            return resourceURL
        }

//        @JvmStatic
//        @AfterAll
//        fun afterAll() {
//            appConnection.close()
//        }
    }

    private val singleTextMessageData = SingleMessageData(
        type = TEXT,
        TextData("Hello World!")
    )
    private val textMessageInCQFormat = CqCodeMessageData("CQ Hello World!")
    private val textMessageWithAtFriend = ArrayMessageData(
        data = listOf(
            SingleMessageData(
                type = TEXT,
                TextData("Hello, "),
            ),
            SingleMessageData(
                type = AT,
                AtData(configurations.friendUserID.toString()),
            ),
        )
    )
    private val catImageData = SingleMessageData(
        type = IMAGE,
        ImageData(
            file = getResourceURL("messages/cat.jpg").toString(),
            type = null,
            url = null,
            cache = 1,
            proxy = 0,
            timeout = null,
            summary = null
        )
    )
    private val shakingData = SingleMessageData(
        type = FACE,
        data = IdData("41")
    )
    private val recordData = SingleMessageData(
        type = RECORD,
        RecordData(
            file = getResourceURL("messages/big-bang-laughs.mp3").toString(),
            magic = null,
            url = null,
            cache = 1,
            proxy = 0,
            timeout = null
        )
    )

//    @Test
//    fun testEventReceiving(): Unit = runBlocking {
//        delay(1145141919810)
//    }

    @Test
    fun testSendPrivateMessage(): Unit = runBlocking {
        if (!configurations.testSendPrivateMessage) {
            return@runBlocking
        }

        listOf(singleTextMessageData, textMessageInCQFormat, catImageData).forEach {
            appConnection.sendPrivateMessage(
                userId = configurations.friendUserID,
                message = it,
            )
        }
    }

    @Test
    fun testSendGroupMessage(): Unit = runBlocking {
        listOf(shakingData, recordData).forEach {
            appConnection.sendGroupMessage(
                groupId = configurations.botIsAdminGroupID,
                message = it,
            )
        }
    }

    @Test
    fun testSendAndRecallMessage(): Unit = runBlocking {
        val groupMessageID = appConnection.sendMessage(
            messageType = GROUP,
            groupId = configurations.botIsAdminGroupID,
            userId = null,
            message = textMessageWithAtFriend,
        )
        delay(5000)
        appConnection.deleteMessage(groupMessageID)

        if (!configurations.testSendPrivateMessage) {
            return@runBlocking
        }
        val privateMessageID = appConnection.sendMessage(
            messageType = PRIVATE,
            groupId = null,
            userId = configurations.friendUserID,
            message = shakingData,
        )
        delay(5000)
        appConnection.deleteMessage(privateMessageID)
    }

    @Test
    fun testGetGroupInfo(): Unit = runBlocking {
        val loginInfo = appConnection.getLoginInfo()
    }


    @Test
    fun testGetForwardMessage(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithoutQuickOperation(MESSAGE_EVENT) {
            if (it.messageType != FORWARD) return@registerListenerWithoutQuickOperation
            appConnection.getMessage(it.messageID)
        }
    }

    @Test
    fun testSendLike(): Unit = runBlocking {
        appConnection.sendLike(
            userId = configurations.friendUserID,
            times = 10
        )
    }

    @Test
    fun testSetGroupKick(): Unit = runBlocking {
        appConnection.setGroupKick(
            groupId = configurations.botIsAdminAndOtherIsMember.groupId,
            userId = configurations.botIsAdminAndOtherIsMember.userId,
            rejectAddRequest = false
        )
    }

    @Test
    fun testSetGroupBan(): Unit = runBlocking {
        appConnection.setGroupBan(
            groupId = configurations.botIsAdminAndOtherIsMember.groupId,
            userId = configurations.botIsAdminAndOtherIsMember.userId,
            duration = 114L
        )
    }

    @Test
    fun testSetGroupWholeBan(): Unit = runBlocking {
        appConnection.setGroupWholeBan(
            groupId = configurations.botIsAdminAndOtherIsMember.userId,
            enable = true
        )
        delay(5000)
        appConnection.setGroupWholeBan(
            groupId = configurations.botIsAdminAndOtherIsMember.groupId,
            enable = true
        )
    }

    @Test
    fun testSetGroupAdmin(): Unit = runBlocking {
        appConnection.setGroupAdmin(
            groupId = configurations.botIsOwnerAndOtherIsMember.groupId,
            userId = configurations.botIsOwnerAndOtherIsMember.userId,
            enable = true
        )

        delay(5000)

        appConnection.setGroupAdmin(
            groupId = configurations.botIsOwnerAndOtherIsMember.groupId,
            userId = configurations.botIsOwnerAndOtherIsMember.userId,
            enable = false
        )
    }

    @Test
    fun testSetGroupAnonymous(): Unit = runBlocking {
        appConnection.setGroupAnonymous(
            groupId = configurations.botIsOwnerAndOtherIsMember.groupId,
            enable = true
        )

        delay(5000)

        appConnection.setGroupAnonymous(
            groupId = configurations.botIsOwnerAndOtherIsMember.groupId,
            enable = false
        )
    }

    @Test
    fun testSetGroupCard(): Unit = runBlocking {
        appConnection.setGroupCard(
            groupId = configurations.botIsAdminAndOtherIsMember.groupId,
            userId = configurations.botIsAdminAndOtherIsMember.userId,
            card = "Test Group Card"
        )

        delay(5000)

        appConnection.setGroupCard(
            groupId = configurations.botIsAdminAndOtherIsMember.groupId,
            userId = configurations.botIsAdminAndOtherIsMember.userId,
            card = ""
        )

    }

    @Test
    fun testSetGroupAnonymousBanByQuickOperation(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithQuickOperation(GROUP_MESSAGE_EVENT) {
            it.anonymous?.let {
                GroupMessageMessageMessageQuickOperationData(
                    ban = true,
                    banDuration = 114L
                )
            }
        }
        delay(114514)
    }

    @Test
    fun testSetGroupAnonymousBanByAnonymousSenderData(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithoutQuickOperation(GROUP_MESSAGE_EVENT) {
            val anonymous = it.anonymous ?: return@registerListenerWithoutQuickOperation
            appConnection.setGroupAnonymousBan(
                groupId = it.groupId,
                sender = anonymous,
                duration = 114L
            )
        }
    }

    @Test
    fun testSetGroupAnonymousBanByFlag(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithoutQuickOperation(GROUP_MESSAGE_EVENT) {
            val anonymous = it.anonymous ?: return@registerListenerWithoutQuickOperation
            appConnection.setGroupAnonymousBan(
                groupId = it.groupId,
                flag = anonymous.flag,
                duration = 114L
            )
        }
    }

    @Test
    fun testSetGroupName(): Unit = runBlocking {
        val group = appConnection.getGroupInfo(
            groupId = configurations.botIsOwnerGroupID,
            noCache = true
        )

        appConnection.setGroupName(
            groupId = group.groupId,
            groupName = "Test Group Name"
        )

        delay(5000)

        appConnection.setGroupName(
            groupId = group.groupId,
            groupName = group.groupName
        )

    }

    @Test
    fun testSetGroupLeave(): Unit = runBlocking {
        appConnection.setGroupLeave(
            groupId = configurations.botIsOwnerGroupID,
            isDismiss = true
        )
    }

    @Test
    fun testSetGroupSpecialTitle(): Unit = runBlocking {
        appConnection.setGroupSpecialTitle(
            groupId = configurations.botIsOwnerGroupID,
            userId = configurations.friendUserID,
            specialTitle = "TestTitle",
            duration = 3600L
        )

    }

    @Test
    fun testSetFriendAddRequestByQuickOperation(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithQuickOperation(FRIEND_ADD_REQUEST_EVENT) {
            FriendAddRequestQuickOperationData(
                approve = true,
            )
        }
        delay(114514)
    }

    @Test
    fun testSetFriendAddRequestByFlag(): Unit = runBlocking {
        appConnection.incomingChannel.registerListenerWithoutQuickOperation(FRIEND_ADD_REQUEST_EVENT) {
            appConnection.setFriendAddRequest(
                flag = it.flag,
                approve = true
            )
        }
        delay(114514)
    }

    @Test
    fun testGetLoginInfo(): Unit = runBlocking {
        appConnection.getLoginInfo()
    }

    @Test
    fun testGetFriendList(): Unit = runBlocking {
        listOf(appConnection.getFriendList())
    }

    @Test
    fun testGetStrangerInfo(): Unit = runBlocking {
        appConnection.getStrangerInfo(
            userId = 10000L,
            noCache = true
        )
    }

    @Test
    fun testGetGroupMemberList(): Unit = runBlocking {
        appConnection.getGroupMemberList(
            groupId = configurations.botIsMemberGroupID
        )
    }

    @Test
    fun testGetGroupHonorInfo(): Unit = runBlocking {
        appConnection.getGroupHonorInfo(
            groupId = configurations.botIsMemberGroupID,
            type = "all"
        )
    }

    @Test
    fun testGetCSRFToken(): Unit = runBlocking {
        appConnection.getCSRFToken()
    }

    @Test
    fun testGetCredentials(): Unit = runBlocking {
        logger.warn {
            """ 
                [Warn]
                Method: testGetCredentials()
                We cannot test this case because this method depends on the actual business needs.
            """.trimIndent()
        }
        assert(true)
    }

    @Test
    fun testGetCookies(): Unit = runBlocking {
        logger.warn {
            """ 
                [Warn]
                Method: testGetCookies()
                We cannot test this case because this method depends on the actual business needs.
            """.trimIndent()
        }
        assert(true)

    }

    @Test
    fun testGetImage(): Unit = runBlocking {
        val messageID = appConnection.sendMessage(
            messageType = IMAGE,
            message = SingleMessageData(
                data = ImageData(
                    file = getResourceURL("messages/cat.jpg").file.toString(),
                    cache = null,
                    proxy = null,
                    type = null,
                    url = null,
                    summary = null,
                    timeout = null
                ),
                type = IMAGE
            ),
            userId = null,
            groupId = configurations.botIsAdminGroupID
        )
        val image = when (val data = appConnection.getMessage(messageID).message) {
            is ArrayMessageData -> data.data.firstOrNull()?.data as ImageData
            is SingleMessageData -> data.data as ImageData
            else -> throw IllegalStateException()
        }
        appConnection.getImage(
            file = image.file
        )
    }

    @Test
    fun testCanSendImage(): Unit = runBlocking {
        appConnection.canSendImage()
    }

    @Test
    fun testCanSendRecord(): Unit = runBlocking {
        appConnection.canSendRecord()
    }

    @Test
    fun testGetStatus(): Unit = runBlocking {
        appConnection.getStatus()
    }

    @Test
    fun testGetVersionInfo(): Unit = runBlocking {
        appConnection.getVersionInfo()
    }

    @Test
    fun testSetRestart(): Unit = runBlocking {
        appConnection.setRestart(
            delay = 2000
        )
    }

    @Test
    fun testCleanCache(): Unit = runBlocking {
        appConnection.cleanCache()
    }

    @Test
    fun testSendFlashImage(): Unit = runBlocking {
        appConnection.sendGroupMessage(
            groupId = configurations.botIsAdminGroupID,
            message = SingleMessageData(
                type = IMAGE,
                data = ImageData(
                    file = getResourceURL("messages/cat.jpg").toString(),
                    cache = 1,
                    proxy = 0,
                    type = FLASH,
                    url = null,
                    summary = null,
                    timeout = null
                )
            )
        )
    }
}
package arguments

import me.aberrantfox.kutils.api.arguments.UserArg
import mock.*
import utilities.ArgumentTestFactory

class UserArgTest : ArgumentTestFactory {
    override val argumentType = UserArg()

    override val validArgs = listOf(
        FakeIds.User to userMock,
        "<@${FakeIds.User}>" to userMock,
        "<@!${FakeIds.User}>" to userMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing, FakeIds.Bot)
}

class BotUserArgTest : ArgumentTestFactory {
    override val argumentType = UserArg(allowsBot = true)

    override val validArgs = listOf(
        FakeIds.User to userMock,
        FakeIds.Bot to botUserMock,
        "<@${FakeIds.Bot}>" to botUserMock,
        "<@!${FakeIds.Bot}>" to botUserMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing)
}
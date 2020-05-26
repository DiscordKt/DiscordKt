package arguments

import me.aberrantfox.kutils.api.arguments.RoleArg
import mock.*
import utilities.ArgumentTestFactory

class RoleArgTest : ArgumentTestFactory {
    override val argumentType = RoleArg(guildId = FakeIds.Guild)

    override val validArgs = listOf(
        FakeIds.Role to singleRoleMock,
        FakeNames.Role_Single to singleRoleMock,
        FakeNames.Role_Multi to multiRoleMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing, FakeNames.Nothing, FakeNames.Role_Single + "more")
}
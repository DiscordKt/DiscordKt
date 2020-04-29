package arguments

import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import mock.*
import utilities.ArgumentTestFactory

class RoleArgTest : ArgumentTestFactory {
    override val argumentType = RoleArg

    override val validArgs = listOf(
        FakeIds.Role to singleRoleMock,
        FakeIds.Role to multiRoleMock,
        FakeNames.Role_Single to singleRoleMock,
        FakeNames.Role_Multi to multiRoleMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing, FakeNames.Nothing, FakeNames.Role_Single + "more")
}
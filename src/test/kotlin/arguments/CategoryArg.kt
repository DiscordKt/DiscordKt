package arguments

import me.aberrantfox.kutils.api.arguments.CategoryArg
import mock.*
import utilities.ArgumentTestFactory

class CategoryArgTest : ArgumentTestFactory {
    override val argumentType = CategoryArg(guildId = FakeIds.Guild)

    override val validArgs = listOf(
        FakeIds.Category to singleCategoryMock,
        FakeNames.Category_Single to singleCategoryMock,
        FakeNames.Category_Multi to multiCategoryMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing, FakeNames.Nothing, FakeNames.Category_Single + "more")
}
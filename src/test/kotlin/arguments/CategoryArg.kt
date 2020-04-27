package arguments

import me.aberrantfox.kjdautils.internal.arguments.CategoryArg
import mock.*
import utilities.SimpleArgTest

class CategoryArgTest : SimpleArgTest {
    override val argumentType = CategoryArg

    override val validArgs = listOf(
        FakeIds.Category to singleCategoryMock,
        FakeIds.Category to multiCategoryMock,
        FakeNames.Category_Single to singleCategoryMock,
        FakeNames.Category_Multi to multiCategoryMock
    )

    override val invalidArgs = listOf(FakeIds.Nothing, FakeNames.Nothing, FakeNames.Category_Single + "more")
}
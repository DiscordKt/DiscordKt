package arguments


import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.CategoryArg
import mock.FakeIds
import mock.GherkinMessages
import mock.attemptConvert
import mock.convertToSingle
import net.dv8tion.jda.api.entities.Category
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object CategorySpec : Spek({
    Feature("Category Command Argument") {
        Scenario(GherkinMessages.ValidArgumentIsPassed) {
            Then(GherkinMessages.ConversionSucceeds) {
                assertEquals(FakeIds.Category, (CategoryArg.convertToSingle("1") as Category).id)
            }
        }

        Scenario("An invalid category is passed") {
            Then(GherkinMessages.ConversationFails) {
                assertTrue(CategoryArg.attemptConvert("2") is ArgumentResult.Error)
            }
        }

        Scenario("An empty string is passed") {
            Then(GherkinMessages.ConversationFails) {
                assertTrue(CategoryArg.attemptConvert("") is ArgumentResult.Error)
            }
        }
    }
})

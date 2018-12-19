package arguments


import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import mock.GherkinMessages
import mock.attemptConvert
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object IntegerArgSpec: Spek({
    Feature("Integer Command Argument") {
        Scenario(GherkinMessages.ValidArgumentIsPassed) {
            Then(GherkinMessages.ConversionSucceeds) {
                assertEquals(3, IntegerArg.convertToSingle("3"))
            }
        }

        Scenario("Passing Integer.MAX_VALUE to be converted") {
            Then(GherkinMessages.ConversionSucceeds) {
                assertEquals(Integer.MAX_VALUE, IntegerArg.convertToSingle("${Integer.MAX_VALUE}"))
            }
        }

        Scenario("A double value is passed to be converted") {
            Then(GherkinMessages.ConversationFails) {
                assertTrue(IntegerArg.attemptConvert("2.3") is ArgumentResult.Error)
            }
        }

        Scenario("A blank value is passed to be converted") {
            Then(GherkinMessages.ConversationFails) {
                assertTrue(IntegerArg.attemptConvert("") is ArgumentResult.Error)
            }
        }
    }
})
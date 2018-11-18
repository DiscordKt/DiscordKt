package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.ChoiceArg
import mock.Constants
import mock.attemptConvert
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object ChoiceArgSpec : Spek({
    Feature("Choice Command Argument") {
        Scenario("Binary Choice Arg is passed a valid choice") {
            Then("The valid choice is returned as the correct type and value") {
                assertEquals(true, ChoiceArg.convertToSingle("true"))
            }
        }

        Scenario("Binary Choice is passed a valid choice with incorrect letter case") {
            Then(Constants.ConversionSucceeds) {
                assertEquals(false, ChoiceArg.convertToSingle("FalSe"))
            }
        }

        Scenario("An instance of a custom choice arg is passed many arguments") {
            var arg = ChoiceArg("default", "")

            When("The arg has 3 values: 'a', 'b', and 'c'.") {
                arg = ChoiceArg("test-custom", "a", "b", "c")
            }

            Then("'a' is detected as a valid argument") {
                assertEquals("a", arg.convertToSingle("a"))
            }

            Then("'b' is detected as a valid argument") {
                assertEquals("b", arg.convertToSingle("b"))
            }

            Then("'c' is detected as a valid argument") {
                assertEquals("c", arg.convertToSingle("c"))
            }

            Then("The standard false/true values no longer work") {
                assertTrue(arg.attemptConvert("true") is ArgumentResult.Error)
                assertTrue(arg.attemptConvert("false") is ArgumentResult.Error)
            }

            Then("A blank string is not a valid argument") {
                assertTrue(arg.attemptConvert("") is ArgumentResult.Error)
            }
        }
    }
})
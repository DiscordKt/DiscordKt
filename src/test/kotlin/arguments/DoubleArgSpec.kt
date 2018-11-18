package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.DoubleArg
import mock.Constants
import mock.attemptConvert
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object DoubleArgSpec : Spek({
    Feature("Double Command Argument") {
        Scenario("3.23 is passed ") {
            Then(Constants.ConversionSucceeds) {
                assertEquals(3.23, DoubleArg.convertToSingle("3.23"))
            }
        }

        Scenario("7 is passed without a decimal point, i.e. as an integer") {
            Then(Constants.ConversionSucceeds) {
                assertEquals(7.0, DoubleArg.convertToSingle("7"))
            }
        }

        Scenario("A blank value is passed") {
            Then(Constants.ConversaionFails) {
                assertTrue(DoubleArg.attemptConvert("") is ArgumentResult.Error)
            }
        }

        Scenario("Double.MIN_VALUE is passed") {
            Then(Constants.ConversionSucceeds) {
                assertEquals(Double.MIN_VALUE, DoubleArg.convertToSingle("${Double.MIN_VALUE}"))
            }
        }
    }
})
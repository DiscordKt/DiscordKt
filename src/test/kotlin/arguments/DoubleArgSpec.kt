package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.DoubleArg
import mock.converToError
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

object DoubleArgSpec : Spek({
    Feature("Double Command Argument") {
        Scenario("3.23 is passed ") {
            Then("3.23 is parsed to the correct type and returned") {
                assertEquals(3.23, DoubleArg.convertToSingle("3.23"))
            }
        }

        Scenario("7 is passed without a decimal point, i.e. as an integer") {
            Then("7.0 is returned without issue") {
                assertEquals(7.0, DoubleArg.convertToSingle("7"))
            }
        }

        Scenario("A blank value is passed") {
            Then("The Conversion fails") {
                assertEquals(ArgumentResult.Error::class.java, DoubleArg.converToError("")::class.java)
            }
        }

        Scenario("Double.MIN_VALUE is passed") {
            Then("The value is parsed to the correct type, and returned") {
                assertEquals(Double.MIN_VALUE, DoubleArg.convertToSingle("${Double.MIN_VALUE}"))
            }
        }
    }
})
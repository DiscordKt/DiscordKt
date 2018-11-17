package arguments

import io.mockk.mockk
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.IntegerArg
import mock.converToError
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

object IntegerArgSpec: Spek({
    Feature("Integer Command Argument") {
        Scenario("Passing 3 to be converted") {
            Then("3 is correctly parsed into the correct type and value ") {
                assertEquals(3, IntegerArg.convertToSingle("3"))
            }
        }

        Scenario("Passing Integer.MAX_VALUE to be converted") {
            Then("Integer.MAX_VALUE is correctly returned as an integer, as the correct value") {
                assertEquals(Integer.MAX_VALUE, IntegerArg.convertToSingle("${Integer.MAX_VALUE}"))
            }
        }

        Scenario("A double value is passed to be converted") {
            Then("The Conversion fails") {
                assertEquals(ArgumentResult.Error::class.java, IntegerArg.converToError("2.3")::class.java)
            }
        }

        Scenario("A blank value is passed to be converted") {
            Then("The Conversion fails") {
                assertEquals(ArgumentResult.Error::class.java, IntegerArg.converToError("")::class.java)
            }
        }
    }
})

private fun convertArgToSingle(arg: String): Int {
    val event = mockk<CommandEvent>()
    val argResult = IntegerArg.convert(arg, listOf(arg), event) as ArgumentResult.Single
    return argResult.result as Int
}

private fun convertArgToError(arg: String) = IntegerArg.convert(arg, listOf(arg), mockk())

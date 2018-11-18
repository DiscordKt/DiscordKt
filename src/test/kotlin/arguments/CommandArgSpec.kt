package arguments

import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.CommandArg
import mock.Constants
import mock.attemptConvert
import mock.convertToSingle
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object CommandArgSpec : Spek({
    Feature("CommandObject command Argument") {
        Scenario("A valid command name is passed") {
            Then(Constants.ConversionSucceeds) {
                assertEquals("ping", (CommandArg.convertToSingle("ping") as Command).name)
            }
        }

        Scenario("An invalid command name is passed") {
            Then(Constants.ConversaionFails) {
                assertTrue(CommandArg.attemptConvert("unknown") is ArgumentResult.Error)
            }
        }

        Scenario("A valid command is passed with different letter casing") {
            Then(Constants.ConversionSucceeds) {
                assertEquals("ping", (CommandArg.convertToSingle("pInG") as Command).name)
            }
        }
    }
})
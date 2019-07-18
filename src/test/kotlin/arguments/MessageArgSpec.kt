package arguments

import me.aberrantfox.kjdautils.internal.command.arguments.MessageArg
import mock.FakeIds
import mock.GherkinMessages
import mock.convertToSingle
import net.dv8tion.jda.api.entities.Message
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals

object MessageArgSpec : Spek ({
    Feature("Message Command Argument") {
        Scenario(GherkinMessages.ValidArgumentIsPassed) {
            Then(GherkinMessages.ConversionSucceeds) {
                assertEquals(FakeIds.Message, (MessageArg.convertToSingle(FakeIds.Message) as Message).id)
            }
        }
    }
})
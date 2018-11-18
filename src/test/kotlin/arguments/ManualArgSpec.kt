package arguments

import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.arguments.Manual
import mock.attemptConvert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertTrue

object ManualArgSpec : Spek({
    Feature("Manual Command Argument") {
        Scenario("Any kind of value is passed to the parser") {
            val longString = ('a' .. 'z').take(1000).toString()
            val randomContents = ArrayList<String>()

            When("The contents passed to the parser is of all shapes and sizes") {
               randomContents.addAll(arrayListOf("", "abc123", ";;!@#o43", "1", "a", longString))
            }

            Then("All succeed, as manual is a hands off approach") {
                randomContents.forEach { argument ->
                    assertTrue { Manual.attemptConvert(argument) is ArgumentResult.Multiple }
                }
            }
        }
    }
})
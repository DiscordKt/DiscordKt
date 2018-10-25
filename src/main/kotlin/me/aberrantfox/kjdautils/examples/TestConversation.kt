package me.aberrantfox.kjdautils.examples

import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.api.dsl.Convo
import me.aberrantfox.kjdautils.api.dsl.conversation
import me.aberrantfox.kjdautils.internal.command.arguments.*
import net.dv8tion.jda.core.entities.User
import java.awt.Color


@Convo
fun testConverstion() = conversation {
    name = "test-conversation"
    description = "Test conversation to test the implementation within KUtils."

    steps {
        step {
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 1"
                    value = "To test various use cases I'd like you to tell me a user Id to start with."
                }
                setColor(Color.CYAN)
            }
            expect = UserArg
        }
        step {
            prompt = embed {
                setTitle("Test Conversation")
                field {
                    name = "Step 2"
                    value = "Alright, now tell me a random word."
                }
                setColor(Color.CYAN)
            }
            expect = WordArg
        }
    }

    onComplete {
        val user = it.responses.component1() as User
        val word = it.responses.component2() as String
        val summary = embed {
            setTitle("Summary - That's what you've told me")
            setThumbnail(user.avatarUrl)
            field {
                name = "Some user account"
                value = "The account of **${user.name}** was created on **${user.creationTime}**."
            }
            field {
                name = "Random word"
                value = "You've said **$word**."
            }
            addBlankField(true)
            field {
                name = "Test Completed"
                value = "Thanks for participating!"
            }
            setColor(Color.GREEN)
        }
        it.respond(summary)
    }
}

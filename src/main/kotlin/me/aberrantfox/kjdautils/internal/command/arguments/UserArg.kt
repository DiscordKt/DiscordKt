package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake

open class UserArg(override val name : String = "DiscordUser") : ArgumentType {
    companion object : UserArg()

    override val examples = arrayListOf("@Bob", "268856125007331328", "275544730887127040")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val retrieved = tryRetrieveSnowflake(event.jda) { it.retrieveUserById(arg.trimToID()).complete() }

        return if (retrieved != null) {
            ArgumentResult.Single(retrieved)
        } else {
            ArgumentResult.Error("Couldn't retrieve user: $arg")
        }
    }
}
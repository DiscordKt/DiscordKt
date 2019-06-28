package me.aberrantfox.kjdautils.internal.command.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake

open class UserArg(override val name: String = "User", val allowsBot: Boolean = false) : ArgumentType {
    companion object : UserArg()

    override val examples = arrayListOf("@Bob", "268856125007331328", "275544730887127040")
    override val consumptionType = ConsumptionType.Single
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {
        val user = event.discord.jda.getUserById(arg.trimToID()) ?: return ArgumentResult.Error("Couldn't retrieve user: $arg")

        if (!allowsBot && user.isBot) return ArgumentResult.Error("A bot is not a valid user arg.")

        return ArgumentResult.Single(user)
    }
}

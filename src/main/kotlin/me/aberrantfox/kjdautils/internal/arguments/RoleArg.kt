package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.*

open class RoleArg(override val name : String = "Role", private val guildId: String = ""): ArgumentType<Role>() {
    companion object : RoleArg()

    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        val guild = if (guildId.isNotEmpty()) event.discord.jda.getGuildById(guildId) else event.guild
        require(guild != null) { "RoleArg failed to resolve guild!" }

        //If the arg is an ID, resolve it here, otherwise resolve by name
        if (arg.trimToID().isLong()) {
            val role = event.discord.jda.getRoleById(arg.trimToID())

            if (role != null)
                return ArgumentResult.Success(role)
        }

        val argString = args.joinToString(" ").toLowerCase()
        val viableNames = guild.roles
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()
        val result = longestMatch?.let { viableNames.filter { it.name == longestMatch.name } } ?: emptyList()

        return when (result.size) {
            0 -> ArgumentResult.Error("Could not resolve any roles by name.")
            1 -> {
                val role = result.first()
                val argList = args.take(role.name.split(" ").size)
                ArgumentResult.Success(role, argList)
            }
            else -> ArgumentResult.Error("Resolving role by name returned multiple matches. Please use an ID.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>) =
        event.guild?.roles?.map { it.name }?.toMutableList().takeIf { !it.isNullOrEmpty() } ?: mutableListOf("Staff")
}

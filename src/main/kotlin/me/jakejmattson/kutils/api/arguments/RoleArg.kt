package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.*
import net.dv8tion.jda.api.entities.Role

/**
 * Accepts a Discord Role entity as an ID, a mention, or by name.
 */
open class RoleArg(override val name: String = "Role", private val guildId: String = "", private val allowsGlobal: Boolean = false) : ArgumentType<Role>() {
    companion object : RoleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        if (arg.trimToID().isLong()) {
            val role = event.discord.jda.getRoleById(arg.trimToID())

            if (!allowsGlobal && guildId != role?.guild?.id)
                return ArgumentResult.Error("$name must be from this guild.")

            if (role != null)
                return ArgumentResult.Success(role)
        }

        val guild = (if (guildId.isNotEmpty()) event.discord.jda.getGuildById(guildId) else event.guild)
            ?: return ArgumentResult.Error("Cannot resolve a role by name from a DM. Please invoke in a guild or use an ID.")

        val argString = args.joinToString(" ").toLowerCase()
        val viableNames = guild.roles
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()?.takeUnless { it.name.length < arg.length }
        val result = longestMatch?.let { viableNames.filter { it.name == longestMatch.name } } ?: emptyList()

        return when (result.size) {
            0 -> ArgumentResult.Error("Could not resolve any roles by name.")
            1 -> {
                val role = result.first()
                val argList = args.take(role.name.split(" ").size)
                ArgumentResult.Success(role, argList.size)
            }
            else -> ArgumentResult.Error("Resolving role by name returned multiple matches. Please use an ID.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>) =
        event.guild?.roles?.map { it.name }?.takeIf { !it.isNullOrEmpty() } ?: listOf("Staff")
}

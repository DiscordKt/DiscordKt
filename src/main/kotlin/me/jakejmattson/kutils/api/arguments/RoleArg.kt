package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import net.dv8tion.jda.api.entities.Role

/**
 * Accepts a Discord Role entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class RoleArg(override val name: String = "Role", private val guildId: String = "", private val allowsGlobal: Boolean = false) : ArgumentType<Role>() {
    /**
     * Accepts a Discord Role entity as an ID, a mention, or by name from within this guild.
     */
    companion object : RoleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        val resolvedGuildId = guildId.ifBlank { event.guild?.id }.takeUnless { it.isNullOrBlank() }

        if (arg.trimToID().toLongOrNull() != null) {
            val role = event.discord.jda.getRoleById(arg.trimToID())

            if (!allowsGlobal && resolvedGuildId != role?.guild?.id)
                return Error("Must be from this guild")

            if (role != null)
                return Success(role)
        }

        resolvedGuildId
            ?: return Error("Please invoke in a guild or use an ID")

        val guild = event.discord.jda.getGuildById(resolvedGuildId)
            ?: return Error("Guild not found")
        val argString = args.joinToString(" ").toLowerCase()
        val viableNames = guild.roles
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()?.takeUnless { it.name.length < arg.length }
        val result = longestMatch?.let { viableNames.filter { it.name == longestMatch.name } } ?: emptyList()

        return when (result.size) {
            0 -> Error("Not found")
            1 -> {
                val role = result.first()
                val argList = args.take(role.name.split(" ").size)
                Success(role, argList.size)
            }
            else -> Error("Found multiple matches")
        }
    }

    override fun generateExamples(event: CommandEvent<*>) =
        event.guild?.roles?.map { it.name }?.takeIf { !it.isNullOrEmpty() } ?: listOf("Staff")

    override fun formatData(data: Role) = data.name
}

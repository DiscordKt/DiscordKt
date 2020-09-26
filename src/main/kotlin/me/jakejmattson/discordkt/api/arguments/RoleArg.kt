package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.dsl.GlobalCommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

/**
 * Accepts a Discord Role entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class RoleArg(override val name: String = "Role", private val guildId: Snowflake? = null, private val allowsGlobal: Boolean = false) : ArgumentType<Role>() {
    /**
     * Accepts a Discord Role entity as an ID, a mention, or by name from within this guild.
     */
    companion object : RoleArg()

    override suspend fun convert(arg: String, args: List<String>, event: GlobalCommandEvent<*>): ArgumentResult<Role> {
        val resolvedGuildId = guildId ?: event.guild?.id

        val roleById = event.discord.api.guilds.toList().flatMap { it.roles.toList() }.firstOrNull { it.id == arg.toSnowflakeOrNull() }

        if (!allowsGlobal && resolvedGuildId != roleById?.guild?.id)
            return Error("Must be from this guild")

        if (roleById != null)
            return Success(roleById)

        resolvedGuildId ?: return Error("Please invoke in a guild or use an ID")

        val guild = event.discord.api.getGuild(resolvedGuildId)
            ?: return Error("Guild not found")

        val argString = args.joinToString(" ").toLowerCase()

        val viableNames = guild.roles
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .toList()
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()?.takeUnless { it.name.length < arg.length }
        val result = longestMatch.let { viableNames.filter { it.name == longestMatch?.name } }

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

    override fun generateExamples(event: GlobalCommandEvent<*>) = listOf("@everyone")

    override fun formatData(data: Role) = data.name
}

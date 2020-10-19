package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.jakejmattson.discordkt.internal.utils.resolveEntityByName

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

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        val resolvedGuildId = guildId ?: event.guild?.id ?: return Error("Guild not found")
        val roleById = event.discord.api.guilds.toList().flatMap { it.roles.toList() }.firstOrNull { it.id == arg.toSnowflakeOrNull() }

        if (roleById != null) {
            return if (allowsGlobal || resolvedGuildId == roleById.guildId)
                Success(roleById)
            else
                Error("Must be from this guild")
        }

        val guild = resolvedGuildId?.let { event.discord.api.getGuild(it) }
            ?: return Error("Guild not found")

        val argString = args.joinToString(" ").toLowerCase()
        val entities = guild.roles.toList()

        return resolveEntityByName(argString, entities) { name }
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf("@everyone")

    override fun formatData(data: Role) = data.name
}

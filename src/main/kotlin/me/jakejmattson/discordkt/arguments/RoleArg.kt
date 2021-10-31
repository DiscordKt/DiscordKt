package me.jakejmattson.discordkt.arguments

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull
import me.jakejmattson.discordkt.internal.utils.resolveEntityByName

/**
 * Accepts a Discord Role entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class RoleArg(override val name: String = "Role",
                          override val description: String = internalLocale.roleArgDescription,
                          private val guildId: Snowflake? = null,
                          private val allowsGlobal: Boolean = false) : Argument<Role> {
    /**
     * Accepts a Discord Role entity as an ID, a mention, or by name from within this guild.
     */
    public companion object : RoleArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Role> {
        val guild = guildId?.let { event.discord.kord.getGuild(it) } ?: event.guild

        if (!allowsGlobal && guild == null)
            return Error("Guild not found")

        val roles = if (allowsGlobal)
            event.discord.kord.guilds.toList().flatMap { it.roles.toList() }
        else
            guild!!.roles.toList()

        val snowflake = arg.toSnowflakeOrNull()
        val roleById = roles.firstOrNull { it.id == snowflake }

        if (roleById != null)
            return Success(roleById)

        return resolveEntityByName(args, roles) { name }
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf("@everyone")
    override fun formatData(data: Role): String = data.name
}
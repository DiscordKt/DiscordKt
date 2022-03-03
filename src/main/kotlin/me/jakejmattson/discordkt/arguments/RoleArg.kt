package me.jakejmattson.discordkt.arguments

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import me.jakejmattson.discordkt.commands.DiscordContext
import me.jakejmattson.discordkt.dsl.internalLocale

/**
 * Accepts a Discord Role entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether this entity can be retrieved from outside this guild.
 */
public open class RoleArg(override val name: String = "Role",
                          override val description: String = internalLocale.roleArgDescription,
                          private val guildId: Snowflake? = null,
                          private val allowsGlobal: Boolean = false) : RoleArgument<Role> {
    /**
     * Accepts a Discord Role entity as an ID, a mention, or by name from within this guild.
     */
    public companion object : RoleArg()

    override suspend fun transform(input: Role, context: DiscordContext): Result<Role> {
        if (!allowsGlobal && input.guild.id != context.guild?.id)
            return Error("Must be from this guild")

        return Success(input)
    }
}
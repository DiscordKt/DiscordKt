package me.jakejmattson.discordkt.dsl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.annotations.NestedDSL

public data class PermissionBuilder(val discord: Discord, val guild: Guild?) {
    internal val users = mutableSetOf<Snowflake>()
    internal val roles = mutableSetOf<Snowflake>()

    @NestedDSL
    public fun users(vararg users: User) {
        this.users.addAll(users.map { it.id })
    }

    @NestedDSL
    public fun users(vararg users: Snowflake) {
        this.users.addAll(users)
    }

    @NestedDSL
    public fun roles(vararg roles: Role) {
        this.roles.addAll(roles.map { it.id })
    }

    @NestedDSL
    public fun roles(vararg roles: Snowflake) {
        this.roles.addAll(roles)
    }
}

public data class Permission(private val action: PermissionBuilder.() -> Unit) {
    internal val users = mutableMapOf<Guild?, MutableList<Snowflake>>()
    internal val roles = mutableMapOf<Guild?, MutableList<Snowflake>>()

    internal fun calculate(discord: Discord, guild: Guild?) {
        val builder = PermissionBuilder(discord, guild)
        action.invoke(builder)
        this.users.getOrPut(guild) { mutableListOf() }.addAll(builder.users)
        this.roles.getOrPut(guild) { mutableListOf() }.addAll(builder.roles)
    }

    public fun hasPermission(guild: Guild?, user: User): Boolean = user.id in users.getOrDefault(guild, emptyList())

    public fun hasPermission(guild: Guild?, role: Role): Boolean = role.id in roles.getOrDefault(guild, emptyList())
}

@BuilderDSL
public fun permission(builder: PermissionBuilder.() -> Unit): Permission = Permission(builder)

/**
 * Permission values and helper functions.
 *
 * @property hierarchy A list of all permission levels available - lowest to highest.
 * @property commandDefault The default level of permission required to use a command.
 */
public interface PermissionSet {
    public val hierarchy: List<Permission>
    public val commandDefault: Permission

    public val GUILD_OWNER: Permission
        get() = permission { users(guild!!.ownerId) }

    public val EVERYONE: Permission
        get() = permission { roles(guild!!.everyoneRole.id) }

    /**
     * Get the highest permission achievable to this user.
     */
    public suspend fun getPermission(guild: Guild?, user: User): Permission? =
        hierarchy.lastOrNull { it.hasPermission(guild, user) }

    /**
     * Get the highest permission achievable to this user.
     */
    public suspend fun getPermission(guild: Guild?, role: Role): Permission? =
        hierarchy.lastOrNull { it.hasPermission(guild, role) }

    public suspend fun getPermissionLevel(guild: Guild?, user: User): Int =
        getPermission(guild, user)?.let { hierarchy.indexOf(it) } ?: -1

    public suspend fun getPermissionLevel(guild: Guild?, role: Role): Int =
        getPermission(guild, role)?.let { hierarchy.indexOf(it) } ?: -1

    public suspend fun hasPermission(permission: Permission, member: Member): Boolean {
        val guild = member.getGuild()
        val roleLevels = member.roles.toList().map { getPermissionLevel(guild, it) } + getPermissionLevel(guild, member.asUser())
        return (roleLevels.maxOrNull() ?: -1) >= hierarchy.indexOf(permission)
    }

    public suspend fun hasPermission(permission: Permission, user: User, guild: Guild?): Boolean {
        return if (guild != null)
            hasPermission(permission, user.asMember(guild.id))
        else
            getPermissionLevel(guild, user) >= hierarchy.indexOf(permission)
    }
}
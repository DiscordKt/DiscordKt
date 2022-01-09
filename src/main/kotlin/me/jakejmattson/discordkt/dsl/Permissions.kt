package me.jakejmattson.discordkt.dsl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
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
    private val users = mutableMapOf<Guild?, MutableList<Snowflake>>()
    private val roles = mutableMapOf<Guild?, MutableList<Snowflake>>()

    internal fun calculate(discord: Discord, guild: Guild?) {
        val builder = PermissionBuilder(discord, guild)
        action.invoke(builder)
        this.users.getOrPut(guild) { mutableListOf() }.addAll(builder.users)
        this.roles.getOrPut(guild) { mutableListOf() }.addAll(builder.roles)
    }

    public fun hasPermission(guild: Guild?, snowflake: Snowflake): Boolean = snowflake in users.getOrDefault(guild, emptyList()) || snowflake in roles.getOrDefault(guild, emptyList())
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

    public operator fun Permission.compareTo(permission: Permission): Int = hierarchy.indexOf(this).compareTo(hierarchy.indexOf(permission))

    /**
     * Get the highest permission achievable to this user or role.
     */
    public suspend fun getPermission(guild: Guild?, snowflake: Snowflake): Permission? =
        hierarchy.lastOrNull { it.hasPermission(guild, snowflake) }

    /**
     * Get the index of the highest permission achievable in this context. [Int.MAX_VALUE] if none apply.
     */
    public suspend fun getPermissionLevel(guild: Guild?, snowflake: Snowflake): Int =
        getPermission(guild, snowflake)?.let { hierarchy.indexOf(it) } ?: -1

    /**
     * Check if a [Member] has the given permission level or higher.
     */
    public suspend fun hasPermission(permission: Permission, discord: Discord, user: User, guild: Guild?): Boolean =
        getPermissionLevel(guild, user.id) >= hierarchy.indexOf(permission)
}
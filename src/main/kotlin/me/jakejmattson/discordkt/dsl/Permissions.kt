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
    public fun users(user: Snowflake) {
        this.users.add(user)
    }

    @NestedDSL
    public fun users(users: List<Snowflake>) {
        this.users.addAll(users)
    }

    @NestedDSL
    public fun roles(role: Snowflake) {
        this.roles.add(role)
    }

    @NestedDSL
    public fun roles(roles: List<Snowflake>) {
        this.roles.addAll(roles)
    }
}

public data class Permission(val name: String, private val action: PermissionBuilder.() -> Unit) {
    internal val users = mutableMapOf<Guild?, MutableList<Snowflake>>()
    internal val roles = mutableMapOf<Guild?, MutableList<Snowflake>>()

    var level: Int = -1
        internal set

    internal fun calculate(discord: Discord, guild: Guild?) {
        val builder = PermissionBuilder(discord, guild)
        action.invoke(builder)
        this.users.getOrPut(guild) { mutableListOf() }.addAll(builder.users)
        this.roles.getOrPut(guild) { mutableListOf() }.addAll(builder.roles)
    }

    public fun hasPermission(guild: Guild?, user: User): Boolean = user.id in users.getOrDefault(guild, emptyList())

    public fun hasPermission(guild: Guild?, role: Role): Boolean = role.id in roles.getOrDefault(guild, emptyList())

    public operator fun compareTo(permission: Permission): Int = this.level.compareTo(permission.level)
}

@BuilderDSL
public fun permission(name: String, builder: PermissionBuilder.() -> Unit): Permission = Permission(name, builder)

/**
 * Permission values and helper functions.
 *
 * @property hierarchy A list of all permission levels available - lowest to highest.
 * @property commandDefault The default level of permission required to use a command.
 */
public interface PermissionSet {
    public val hierarchy: List<Permission>
    public val commandDefault: Permission

    private fun Permission?.toLevel() = this?.level ?: -1

    public suspend fun getPermission(member: Member): Int {
        val allLevels = member.roles.toList().map { getPermission(it) } + getPermission(member.asUser(), member.getGuild())
        return allLevels.maxOfOrNull { it.toLevel() } ?: -1
    }

    public suspend fun getPermission(user: User, guild: Guild?): Permission? = hierarchy.lastOrNull { it.hasPermission(guild, user) }

    public suspend fun getPermission(role: Role): Permission? = hierarchy.lastOrNull { it.hasPermission(role.guild.asGuild(), role) }

    public suspend fun hasPermission(permission: Permission, member: Member): Boolean = getPermission(member) >= hierarchy.indexOf(permission)

    public suspend fun hasPermission(permission: Permission, user: User, guild: Guild?): Boolean {
        val member = guild?.id?.let { user.asMemberOrNull(it) }

        return if (member != null)
            hasPermission(permission, member)
        else
            getPermission(user, null).toLevel() >= permission.level
    }

    public suspend fun isHigherLevel(member1: Member, member2: Member): Boolean = getPermission(member1) > getPermission(member2)

    public suspend fun isLowerLevel(member1: Member, member2: Member): Boolean = getPermission(member1) < getPermission(member2)

    public suspend fun isSameLevel(member1: Member, member2: Member): Boolean = getPermission(member1) == getPermission(member2)
}
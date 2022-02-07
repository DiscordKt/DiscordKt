package me.jakejmattson.discordkt.dsl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.internal.annotations.BuilderDSL
import me.jakejmattson.discordkt.internal.annotations.NestedDSL

/**
 * Builder class for a permission.
 * @param discord The [Discord] instance.
 * @param guild The contextual [Guild].
 *
 * @see permission
 */
public data class PermissionBuilder(val discord: Discord, val guild: Guild?) {
    internal val users = mutableSetOf<Snowflake>()
    internal val roles = mutableSetOf<Snowflake>()

    /**
     * Allow a user [Snowflake].
     */
    @NestedDSL
    public fun users(user: Snowflake) {
        this.users.add(user)
    }

    /**
     * Allow a list of user [Snowflake].
     */
    @NestedDSL
    public fun users(users: List<Snowflake>) {
        this.users.addAll(users)
    }

    /**
     * Allow a role [Snowflake].
     */
    @NestedDSL
    public fun roles(role: Snowflake) {
        this.roles.add(role)
    }

    /**
     * Allow a list of role [Snowflake].
     */
    @NestedDSL
    public fun roles(roles: List<Snowflake>) {
        this.roles.addAll(roles)
    }
}

/**
 * A single permission level.
 *
 * @param name The display name of this permission.
 * @property level The numeric value of this permission, assigned from the hierarchy.
 */
public data class Permission(val name: String, private val action: PermissionBuilder.() -> Unit) {
    internal val users = mutableMapOf<Snowflake?, MutableList<Snowflake>>()
    internal val roles = mutableMapOf<Snowflake?, MutableList<Snowflake>>()

    var level: Int = -1
        internal set

    internal fun calculate(discord: Discord, guild: Guild?) {
        val builder = PermissionBuilder(discord, guild)
        action.invoke(builder)
        this.users.getOrPut(guild?.id) { mutableListOf() }.addAll(builder.users)
        this.roles.getOrPut(guild?.id) { mutableListOf() }.addAll(builder.roles)
    }

    public fun hasPermission(guild: Guild?, user: User): Boolean = user.id in users.getOrDefault(guild?.id, emptyList())

    public fun hasPermission(guild: Guild?, role: Role): Boolean = role.id in roles.getOrDefault(guild?.id, emptyList())

    internal fun allowsEveryone(guild: Guild?): Boolean = roles[guild?.id]?.contains(guild?.everyoneRole?.id) == true

    /**
     * Compare two permission by their numeric level.
     */
    public operator fun compareTo(permission: Permission): Int = this.level.compareTo(permission.level)
}

/**
 * Create a new permission.
 *
 * @param name The display name of this permission.
 */
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

    /**
     * Pre-built permission tier for all users.
     */
    @BuilderDSL
    public fun everyone(): Permission = permission("Everyone") { roles(guild!!.everyoneRole.id) }

    /**
     * Pre-built permission tier for guild owners.
     */
    @BuilderDSL
    public fun guildOwner(): Permission = permission("Guild Owner") { users(guild!!.ownerId) }

    /**
     * Get the highest available [Permission] by checking user id and roles.
     */
    public suspend fun getPermission(member: Member): Permission? {
        val allLevels = member.roles.toList().map { getPermission(it) } + getPermission(member.asUser(), member.getGuild())
        return allLevels.maxByOrNull { it.toLevel() }
    }

    /**
     * Get the highest available [Permission] of this user id. This does not check against roles.
     */
    public suspend fun getPermission(user: User, guild: Guild?): Permission? = hierarchy.lastOrNull { it.allowsEveryone(guild) || it.hasPermission(guild, user) }

    /**
     * Get the highest available [Permission] of this role id. This does not check against users.
     */
    public suspend fun getPermission(role: Role): Permission? = hierarchy.lastOrNull { it.allowsEveryone(role.guild.asGuild()) || it.hasPermission(role.guild.asGuild(), role) }

    /**
     * Check if a [Member] has the provided [Permission].
     */
    public suspend fun hasPermission(permission: Permission, member: Member): Boolean = getPermission(member).toLevel() >= hierarchy.indexOf(permission)

    /**
     * Check if a [User] has the provided [Permission].
     * Will attempt to resolve a [Member] for checking roles.
     */
    public suspend fun hasPermission(permission: Permission, user: User, guild: Guild?): Boolean {
        val member = guild?.id?.let { user.asMemberOrNull(it) }

        return if (member != null)
            hasPermission(permission, member)
        else
            getPermission(user, null).toLevel() >= permission.level
    }
}
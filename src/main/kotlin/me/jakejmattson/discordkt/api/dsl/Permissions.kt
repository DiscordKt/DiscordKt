package me.jakejmattson.discordkt.api.dsl

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import me.jakejmattson.discordkt.api.Discord

/**
 * Holds information used to determine if a command has permission to run.
 *
 * @param discord The [Discord] instance.
 * @param user The discord user who invoked the command.
 * @param guild The guild that this command was invoked in.
 */
public data class PermissionContext(val discord: Discord, val user: User, val guild: Guild?) {
    /**
     * Attempt to get the member in this context.
     */
    public suspend fun getMember(): Member? = guild?.id?.let { user.asMember(it) }
}

/**
 * The interface that all permission enums must inherit from.
 */
public interface PermissionSet {
    /**
     * Whether an enum value can be applied to a given situation.
     *
     * @param context The event data used to determine value.
     */
    public suspend fun hasPermission(context: PermissionContext): Boolean
}

/**
 * Permission values and helper functions.
 *
 * @param levels A list of all permission levels available.
 * @param commandDefault The default level of permission required to use a command.
 */
public class PermissionBundle(public val levels: List<Enum<*>>, public val commandDefault: Enum<*>) {
    /**
     * Get the highest (first) permission achievable in this context.
     */
    public suspend fun getPermission(permissionContext: PermissionContext): Enum<*>? =
        levels.firstOrNull { (it as PermissionSet).hasPermission(permissionContext) }

    /**
     * Get the index of the highest permission achievable in this context. [Int.MAX_VALUE] if none apply.
     */
    public suspend fun getPermissionLevel(permissionContext: PermissionContext): Int =
        getPermission(permissionContext)?.let { levels.indexOf(it) } ?: Int.MAX_VALUE

    /**
     * Compare two [PermissionContext] by their permission level, obtained with [getPermissionLevel].
     */
    public suspend fun compare(context1: PermissionContext, context2: PermissionContext): Int =
        getPermissionLevel(context1).compareTo(getPermissionLevel(context2))

    /**
     * Check if a [Member] has the given permission level or higher.
     */
    public suspend fun hasPermission(discord: Discord, member: Member, permission: Enum<*>): Boolean =
        getPermissionLevel(PermissionContext(discord, member, member.getGuild())) <= permission.ordinal

    /**
     * Compare two [PermissionContext] by their permission level.
     *
     * @return true if the first context provided has a higher level than the second.
     */
    public suspend fun isHigherLevel(context1: PermissionContext, context2: PermissionContext): Boolean = compare(context1, context2) < 0

    /**
     * Compare two [PermissionContext] by their permission level.
     *
     * @return true if the first context provided has a lower level than the second.
     */
    public suspend fun isLowerLevel(context1: PermissionContext, context2: PermissionContext): Boolean = compare(context1, context2) > 0

    /**
     * Compare two [PermissionContext] by their permission level.
     *
     * @return true if the first context provided has the same level as the second.
     */
    public suspend fun isSameLevel(context1: PermissionContext, context2: PermissionContext): Boolean = compare(context1, context2) == 0

    private suspend fun Member.toPermissionContext(discord: Discord) = PermissionContext(discord, asUser(), guild.asGuild())

    /**
     * Compare two [Member] by converting to a [PermissionContext] and the finding their resulting permission level.
     *
     * @return true if the first member provided has a higher level than the second.
     */
    public suspend fun isHigherLevel(discord: Discord, member1: Member, member2: Member): Boolean {
        val context1 = member1.toPermissionContext(discord)
        val context2 = member2.toPermissionContext(discord)
        return isHigherLevel(context1, context2)
    }

    /**
     * Compare two [Member] by converting to a [PermissionContext] and the finding their resulting permission level.
     *
     * @return true if the first member provided has a lower level than the second.
     */
    public suspend fun isLowerLevel(discord: Discord, member1: Member, member2: Member): Boolean {
        val context1 = member1.toPermissionContext(discord)
        val context2 = member2.toPermissionContext(discord)
        return isLowerLevel(context1, context2)
    }

    /**
     * Compare two [Member] by converting to a [PermissionContext] and the finding their resulting permission level.
     *
     * @return true if the first member provided has the same level as the second.
     */
    public suspend fun isSameLevel(discord: Discord, member1: Member, member2: Member): Boolean {
        val context1 = member1.toPermissionContext(discord)
        val context2 = member2.toPermissionContext(discord)
        return isSameLevel(context1, context2)
    }
}
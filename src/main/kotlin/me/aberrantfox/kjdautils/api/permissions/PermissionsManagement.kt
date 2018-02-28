package me.aberrantfox.kjdautils.api.permissions

import me.aberrantfox.kjdautils.extensions.jda.getHighestRole
import me.aberrantfox.kjdautils.extensions.jda.isEqualOrHigherThan
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.api.dsl.KJDAConfiguration
import me.aberrantfox.kjdautils.extensions.stdlib.idToUser
import me.aberrantfox.kjdautils.extensions.stdlib.toRole
import me.aberrantfox.kjdautils.api.types.CommandName
import me.aberrantfox.kjdautils.api.types.RoleID
import me.aberrantfox.kjdautils.api.types.UserID
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role

data class PermissionManager(private val roles: List<Role>, val guild: Guild, val config: KJDAConfiguration) {
    private val map: HashMap<RoleID, HashSet<CommandName>> = HashMap()
    fun setPermission(roleID: RoleID, name: CommandName) {
        val lower = name.toLowerCase()
        map.keys.map { map[it]!! }
            .filter { it.contains(lower) }
            .forEach { it.remove(lower) }

        if(map.containsKey(roleID)) {
            map[roleID]!!.add(lower)
        } else {
            map[roleID] = hashSetOf(lower)
        }
    }

    fun roleRequired(commandName: CommandName): Role? {
        val containingMap = map.entries.firstOrNull { it.value.contains(commandName.toLowerCase()) }
        return containingMap?.key?.toRole(guild)
    }

    fun canPerformAction(userId: UserID, actionRoleID: RoleID): Boolean {
        if(userId == config.ownerID) return true

        val highestRole = userId.idToUser(guild.jda).toMember(guild).getHighestRole()
        val actionRole = actionRoleID.toRole(guild)

        return highestRole?.isEqualOrHigherThan(actionRole) ?: false
    }

    fun canUseCommand(userId: UserID, commandName: CommandName): Boolean {
        if(userId == config.ownerID) return true

        val highestRole = userId.idToUser(guild.jda).toMember(guild).getHighestRole()
        val roles = getAllRelevantRoleIds(highestRole?.id)

        return roles.map { map[it] }
            .any { it!!.contains(commandName) }
    }

    fun listAvailableCommands(roleID: RoleID?): String {
        val roles = getAllRelevantRoleIds(roleID)

        if(roles.isEmpty()) return "None"

        return roles.map { map[it] }
            .reduceRight { a, b -> a!!.addAll(b!!) ; a }!!
            .joinToString(", ") { a -> a }
    }

    private fun getAllRelevantRoleIds(roleID: RoleID?): List<String> {
        if(roleID == null) return ArrayList()

        val role = roles.first { it.id == roleID }
        val lowerRoles = ArrayList(roles
            .filter { it.position < role.position }
            .map { it.id }
            .toList())

        lowerRoles.add(roleID)

        return lowerRoles.filter { map.containsKey(it) }
    }
}
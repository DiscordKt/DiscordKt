package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Category

open class CategoryArg(override val name: String = "Category", private val guildId: String = ""): ArgumentType<Category>() {
    companion object : CategoryArg()

    override val consumptionType = ConsumptionType.Multiple

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Category> {
        if (arg.trimToID().isLong()) {
            val category = event.discord.jda.getCategoryById(arg.trimToID())

            if (category != null)
                return ArgumentResult.Success(category)
        }

        val guild = if (guildId.isNotEmpty()) event.discord.jda.getGuildById(guildId) else event.guild
        guild ?: return ArgumentResult.Error("Cannot resolve a category by name from a DM. Please invoke in a guild or use an ID.")

        val argString = args.joinToString(" ").toLowerCase()
        val viableNames = guild.categories
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()?.takeUnless { it.name.length < arg.length }
        val result = longestMatch?.let { viableNames.filter { it.name == longestMatch.name } } ?: emptyList()

        return when (result.size) {
            0 -> ArgumentResult.Error("Could not resolve any categories by name.")
            1 -> {
                val category = result.first()
                val argList = args.take(category.name.split(" ").size)
                ArgumentResult.Success(category, argList)
            }
            else -> ArgumentResult.Error("Resolving category by name returned multiple matches. Please use an ID.")
        }
    }

    override fun generateExamples(event: CommandEvent<*>): MutableList<String> {
        return event.guild?.categories?.map { it.id }?.toMutableList() ?: mutableListOf("Chat Channels")
    }
}

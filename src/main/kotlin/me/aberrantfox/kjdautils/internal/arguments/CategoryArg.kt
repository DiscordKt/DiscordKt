package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.isLong
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType

open class CategoryArg(override val name: String = "Category", private val guildId: String = "") : ArgumentType {
    companion object : CategoryArg()

    override val examples = arrayListOf("302134543639511050", "Staff", "Chat Channels")
    override val consumptionType = ConsumptionType.Multiple
    override fun convert(arg: String, args: List<String>, event: CommandEvent): ArgumentResult {

        val guild = if (guildId.isNotEmpty()) event.discord.jda.getGuildById(guildId) else event.guild
        guild ?: return ArgumentResult.Error("Failed to resolve guild! Pass a valid guild id to CategoryArg.")

        //If the arg is an ID, resolve it here, otherwise resolve by name
        if (arg.trimToID().isLong()) {
            val category = event.discord.jda.getCategoryById(arg)
                ?: return ArgumentResult.Error("Could not resolve category by ID.")

            return ArgumentResult.Single(category)
        }

        var categories = guild.categories
        val categoryBuilder = StringBuilder()
        fun String.startsWithIgnoreCase(string: String) = this.toLowerCase().startsWith(string.toLowerCase())

        //Consume arguments until only one category matches the filter
        args.takeWhile {
            val padding = if (categoryBuilder.isNotEmpty()) " " else ""
            categoryBuilder.append("$padding$it")
            categories = categories.filter { it.name.startsWithIgnoreCase(categoryBuilder.toString()) }

            categories.size > 1
        }

        val error = ArgumentResult.Error("Couldn't retrieve category :: $categoryBuilder")

        //Get the single category that survived filtering
        val resolvedCategory = categories.firstOrNull() ?: return error
        val resolvedName = resolvedCategory.name

        //Determine how many args this category would consume
        val lengthOfCategory = resolvedName.split(" ").size

        //Check if the category that survived filtering matches the args given
        val argList = args.take(lengthOfCategory)
        val isValid = resolvedName.toLowerCase() == argList.joinToString(" ").toLowerCase()

        return if (isValid) ArgumentResult.Multiple(resolvedCategory, argList) else error
    }
}

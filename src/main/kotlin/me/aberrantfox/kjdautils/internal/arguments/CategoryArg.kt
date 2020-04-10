package me.aberrantfox.kjdautils.internal.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.stdlib.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.Category

open class CategoryArg(override val name: String = "Category", private val guildId: String = ""): ArgumentType<Category>() {
    companion object : CategoryArg()

    override val consumptionType = ConsumptionType.Multiple
    override val examples = mutableListOf("Chat Channels")
    override var exampleFactory = createExampleFactory {
        it.guild?.categories?.map { it.id }?.toMutableList()
    }

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Category> {
        val guild = if (guildId.isNotEmpty()) event.discord.jda.getGuildById(guildId) else event.guild
        guild ?: return ArgumentResult.Error("Failed to resolve guild! Pass a valid guild id to CategoryArg.")

        //If the arg is an ID, resolve it here, otherwise resolve by name
        if (arg.trimToID().isLong()) {
            val category = event.discord.jda.getCategoryById(arg)
                ?: return ArgumentResult.Error("Could not resolve category by ID.")

            return ArgumentResult.Success(category)
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

        //Get the single category that survived filtering
        val resolvedCategory = categories.firstOrNull() ?: return ArgumentResult.Error("Couldn't retrieve category :: $categoryBuilder")
        val resolvedName = resolvedCategory.name

        //Determine how many args this category would consume
        val lengthOfCategory = resolvedName.split(" ").size

        //Check if the category that survived filtering matches the args given
        val argList = args.take(lengthOfCategory)
        val isValid = resolvedName.toLowerCase() == argList.joinToString(" ").toLowerCase()

        return if (isValid) ArgumentResult.Success(resolvedCategory, argList) else ArgumentResult.Error("Couldn't retrieve category :: $categoryBuilder")
    }
}

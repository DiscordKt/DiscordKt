package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.channel.Category
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.dsl.arguments.*
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.jakejmattson.discordkt.api.extensions.*

/**
 * Accepts a Discord Category entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class CategoryArg(override val name: String = "Category", private val guildId: Snowflake? = null, private val allowsGlobal: Boolean = false) : ArgumentType<Category>() {
    /**
     * Accepts a Discord Category entity as an ID, a mention, or by name from within this guild.
     */
    companion object : CategoryArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Category> {
        val resolvedGuildId = guildId ?: event.guild?.id

        if (arg.trimToID().toLongOrNull() != null) {
            val category = arg.toSnowflake()?.let { event.discord.api.getChannel(it) } as? Category

            if (!allowsGlobal && resolvedGuildId != category?.id)
                return Error("Must be from this guild")

            if (category != null)
                return Success(category)
        }

        resolvedGuildId ?: return Error("Please invoke in a guild or use an ID")

        val guild = event.discord.api.getGuild(resolvedGuildId)
            ?: return Error("Guild not found")
        val argString = args.joinToString(" ").toLowerCase()

        val viableNames = guild.channels
            .filter { argString.startsWith(it.name.toLowerCase()) }
            .toList()
            .sortedBy { it.name.length }

        val longestMatch = viableNames.lastOrNull()?.takeUnless { it.name.length < arg.length }
        val result = longestMatch.let { viableNames.filter { it.name == longestMatch?.name } }

        return when (result.size) {
            0 -> Error("Not found")
            1 -> {
                val category = result.first() as Category
                val argList = args.take(category.name.split(" ").size)
                Success(category, argList.size)
            }
            else -> Error("Found multiple matches")
        }
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id.longValue.toString())
    override fun formatData(data: Category) = data.name
}

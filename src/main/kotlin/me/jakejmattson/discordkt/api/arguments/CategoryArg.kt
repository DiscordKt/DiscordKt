package me.jakejmattson.discordkt.api.arguments

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.Category
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.jakejmattson.discordkt.internal.utils.resolveEntityByName

/**
 * Accepts a Discord Category entity as an ID, a mention, or by name.
 *
 * @param guildId The guild ID used to determine which guild to search in.
 * @param allowsGlobal Whether or not this entity can be retrieved from outside this guild.
 */
open class CategoryArg(override val name: String = "Category",
                       override val description: String = internalLocale.categoryArgDescription,
                       private val guildId: Snowflake? = null,
                       private val allowsGlobal: Boolean = false) : Argument<Category> {
    /**
     * Accepts a Discord Category entity as an ID, a mention, or by name from within this guild.
     */
    companion object : CategoryArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Category> {
        val guild = guildId?.let { event.discord.kord.getGuild(it) } ?: event.guild

        if (!allowsGlobal && guild == null)
            return Error("Guild not found")

        val categories = if (allowsGlobal)
            event.discord.kord.guilds.toList().flatMap { it.channels.filterIsInstance<Category>().toList() }
        else
            guild!!.channels.filterIsInstance<Category>().toList()

        val snowflake = arg.toSnowflakeOrNull()
        val categoryById = categories.firstOrNull { it.id == snowflake }

        if (categoryById != null)
            return Success(categoryById)

        return resolveEntityByName(args, categories) { name }
    }

    override suspend fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id.asString)
    override fun formatData(data: Category) = data.name
}
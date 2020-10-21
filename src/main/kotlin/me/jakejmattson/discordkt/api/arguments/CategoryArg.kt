package me.jakejmattson.discordkt.api.arguments

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.channel.Category
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull
import me.jakejmattson.discordkt.internal.utils.resolveEntityByName

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
        val guild = guildId?.let { event.discord.api.getGuild(it) } ?: event.guild

        if (!allowsGlobal && guild == null)
            return Error("Guild not found")

        val categories = if (allowsGlobal)
            event.discord.api.guilds.toList().flatMap { it.channels.filterIsInstance<Category>().toList() }
        else
            guild!!.channels.filterIsInstance<Category>().toList()

        val snowflake = arg.toSnowflakeOrNull()
        val categoryById = categories.firstOrNull { it.id == snowflake }

        if (categoryById != null)
            return Success(categoryById)

        return resolveEntityByName(args, categories) { name }
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id.value)
    override fun formatData(data: Category) = data.name
}

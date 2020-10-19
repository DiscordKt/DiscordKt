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
        val resolvedGuildId = guildId ?: event.guild?.id
        val categoryById = arg.toSnowflakeOrNull()?.let { event.discord.api.getChannelOf<Category>(it) }

        if (categoryById != null) {
            return if (allowsGlobal || resolvedGuildId == categoryById.guildId)
                Success(categoryById)
            else
                Error("Must be from this guild")
        }

        val guild = resolvedGuildId?.let { event.discord.api.getGuild(it) }
            ?: return Error("Guild not found")

        val argString = args.joinToString(" ").toLowerCase()
        val entities = guild.channels.filterIsInstance<Category>().toList()

        return resolveEntityByName(argString, entities) { name }
    }

    override fun generateExamples(event: CommandEvent<*>) = listOf(event.channel.id.value)
    override fun formatData(data: Category) = data.name
}

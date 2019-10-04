package mock


import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.RestAction


val categoryMock = mock<Category> {
    on { id } doReturn FakeIds.Category
}

val messageMock = mock<Message> {
    on { id } doReturn FakeIds.Message
}

val guildMock = mock<Guild> {
    on { id } doReturn FakeIds.Guild
}

val restActionMessageMock = mock<RestAction<Message>> {
     on { complete() } doReturn messageMock
}

val channelMock = mock<TextChannel> {
    on { retrieveMessageById(FakeIds.Message) } doReturn restActionMessageMock
}

val jdaMock = mock<JDA> {
    on { getCategoryById(FakeIds.Category) } doReturn categoryMock
    on { getTextChannelById(FakeIds.Channel) } doReturn channelMock
    on { getGuildById(FakeIds.Guild) } doReturn guildMock
}

val discordMock = mock<Discord> {
   on { jda } doReturn  jdaMock
}


val pingCommandMock = mock<Command> {
    on { name } doReturn "ping"
    on { execute } doReturn {}
}

val commandContainerMock = mock<CommandsContainer> {
    on { get("ping") } doReturn pingCommandMock
}

val commandEventMock = mock<CommandEvent> {
    on { discord } doReturn discordMock
    on { container } doReturn commandContainerMock
    on { channel } doReturn channelMock
}

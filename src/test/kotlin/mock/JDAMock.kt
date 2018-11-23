package mock


import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Category
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.requests.RestAction


val categoryMock = mock<Category> {
    on { id } doReturn FakeIds.Category
}

val messageMock = mock<Message> {
    on { id } doReturn FakeIds.Message
}

val restActionMessageMock = mock<RestAction<Message>> {
     on { complete() } doReturn messageMock
}

val channelMock = mock<TextChannel> {
    on { getMessageById(FakeIds.Message) } doReturn restActionMessageMock
}

val jdaMock = mock<JDA> {
    on { getCategoryById(FakeIds.Category) } doReturn categoryMock
    on { getTextChannelById(FakeIds.Channel) } doReturn channelMock
}

val pingCommandMock = mock<Command> {
    on { name } doReturn "ping"
    on { execute } doReturn {}
}

val commandContainerMock = mock<CommandsContainer> {
    on { get("ping") } doReturn pingCommandMock
}

val commandEventMock = mock<CommandEvent> {
    on { jda } doReturn jdaMock
    on { container } doReturn commandContainerMock
    on { channel } doReturn channelMock
}

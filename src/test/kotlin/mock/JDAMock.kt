package mock


import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Category


val categoryMock = mock<Category> {
    on { id } doReturn "1"
}

val jdaMock = mock<JDA> {
    on { getCategoryById("1") } doReturn categoryMock
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
}
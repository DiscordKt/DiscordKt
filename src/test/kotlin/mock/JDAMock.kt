package mock


import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Category


val categoryMock = mock<Category> {
    on { id } doReturn "1"
}

val jdaMock = mock<JDA> {
    on { getCategoryById("1") } doReturn categoryMock
}

val commandEventMock = mock<CommandEvent> {
    on { jda } doReturn jdaMock
}
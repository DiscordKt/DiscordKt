package mock

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.*
import io.mockk.*
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.dsl.command.*

val singleCategoryMock = mockk<Category> {
    every { id } returns FakeIds.Category
    every { name } returns FakeNames.Category_Single
    every { guild.id } returns FakeIds.Guild
}

val multiCategoryMock = mockk<Category> {
    every { id } returns FakeIds.Category
    every { name } returns FakeNames.Category_Multi
    every { guild.id } returns FakeIds.Guild
}

val singleRoleMock = mockk<Role> {
    every { id } returns FakeIds.Role
    every { name } returns FakeNames.Role_Single
    every { guild.id } returns FakeIds.Guild
}

val multiRoleMock = mockk<Role> {
    every { id } returns FakeIds.Role
    every { name } returns FakeNames.Role_Multi
    every { guild.id } returns FakeIds.Guild
}

val userMock = mockk<User> {
    every { id } returns FakeIds.User
    every { isBot } returns false
}

val messageMock = mockk<Message> {
    every { id } returns FakeIds.Message
}

val guildMock = mockk<Guild> {
    every { id } returns FakeIds.Guild
}

val channelMock = mockk<TextChannel> {

}

val kordMock = mockk<Kord> {

}

val discordMock = mockk<Discord> {
    every { kord } returns kordMock
}

val commandContainerMock = mockk<CommandsContainer> {

}

val commandEventMock = mockk<CommandEvent<*>> {
    every { container } returns commandContainerMock
    every { discord } returns discordMock
    every { guild } returns guildMock
    every { channel } returns channelMock
}

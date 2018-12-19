package mock

class GherkinMessages { companion object {
    const val ConversionSucceeds = "A valid choice is returned, as the correct type and Value"
    const val ConversationFails = "The conversion fails, and an error is returned"
    const val ValidArgumentIsPassed = "A valid argument is passed to the convert function"
}}

class FakeIds { companion object {
    const val Category = "1"
    const val Message = "2"
    const val Channel = "3"
}}
package mock

import com.gitlab.kordlib.common.entity.Snowflake

class FakeIds {
    companion object {
        val Category = Snowflake("1")
        val Message = Snowflake("2")
        val Channel = Snowflake("3")
        val Guild = Snowflake("4")
        val Role = Snowflake("5")
        val User = Snowflake("6")

        const val Nothing = "-1"
    }
}

class FakeNames {
    companion object {
        const val Category_Single = "General"
        const val Category_Multi = "Voice Channels"

        const val Role_Single = "Staff"
        const val Role_Multi = "Staff 2"

        const val Nothing = "Nothing"
    }
}
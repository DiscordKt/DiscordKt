package me.jakejmattson.kutils.api.dsl.configuration

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy

private fun createDefaultBuilder(token: String) =
    JDABuilder.createDefault(token)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .enableIntents(GatewayIntent.GUILD_MEMBERS)

data class ClientConfiguration(var token: String,
                               var jdaBuilder: JDABuilder = createDefaultBuilder(token))
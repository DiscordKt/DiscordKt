package me.aberrantfox.kjdautils.internal.services

import me.aberrantfox.kjdautils.discord.Discord
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

class ScriptEngineService(discord: Discord) {
    private val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    init {
        setIdeaIoUseFallback()

        with(engine) {
            put("discord", discord)
            eval("println(\"ScriptEngineService online!\")")
        }
    }

    fun evaluateScript(code: String) = engine.eval(code)
}

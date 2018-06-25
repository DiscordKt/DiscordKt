package me.aberrantfox.kjdautils.internal.plugins

import me.aberrantfox.kjdautils.api.KUtils
import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.File
import javax.script.ScriptEngine

typealias KotlinEngine = KotlinJsr223JvmLocalScriptEngineFactory

class PluginService(container: CommandsContainer, kUtils: KUtils) {
    private val engine: ScriptEngine = KotlinEngine().scriptEngine

    init {
        engine.apply {
            put("container", container)
            put("kutils", kUtils)

            eval("""
            import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
            import me.aberrantfox.kjdautils.api.dsl.commands
            import me.aberrantfox.kjdautils.api.KUtils

            val container = bindings["container"] as CommandsContainer
            val kutils = bindings["kutils"] as KUtils
            """.trimIndent())
        }
    }

    fun loadScripts(directory: String) = File(directory).listFiles()
            .filter { it.extension == "kts" }
            .forEach { loadPlugin(it) }

    fun loadPlugin(file: File) = loadPlugin(file.readText())

    fun loadPlugin(code: String) = engine.eval(code)
}
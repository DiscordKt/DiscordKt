package me.aberrantfox.kjdautils.internal.plugins

import me.aberrantfox.kjdautils.api.dsl.CommandsContainer
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.File
import javax.script.ScriptEngine

typealias KotlinEngine = KotlinJsr223JvmLocalScriptEngineFactory

class PluginService(container: CommandsContainer) {
    private val engine: ScriptEngine = KotlinEngine().scriptEngine

    init {
        engine.put("container", container)
    }

    fun loadScripts(directory: String) = File(directory).listFiles()
            .filter { it.extension == "kts" }
            .forEach { loadPlugin(it) }

    fun loadPlugin(file: File) = loadPlugin(file.readText())

    fun loadPlugin(code: String) = engine.eval(code)
}
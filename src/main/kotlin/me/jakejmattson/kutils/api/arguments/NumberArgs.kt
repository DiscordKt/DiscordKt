package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random

private inline fun <reified T : Number> genericConversion(name: String, arg: String): ArgumentResult<T> {
    val result = when (T::class) {
        Byte::class -> arg.toByteOrNull()
        Short::class -> arg.toShortOrNull()
        Int::class -> arg.toIntOrNull()
        Long::class -> arg.toLongOrNull()
        Float::class -> arg.toFloatOrNull()
        Double::class -> arg.toDoubleOrNull()
        else -> null
    } ?: return ArgumentResult.Error("Couldn't parse $name from $arg.")

    return ArgumentResult.Success(result as T)
}

private val integerExamples = (0..10).map { it.toString() }
private val decimalExamples = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))

open class ByteArg(override val name: String = "Byte") : ArgumentType<Byte>() {
    companion object : ByteArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Byte>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = integerExamples
}

open class ShortArg(override val name: String = "Short") : ArgumentType<Short>() {
    companion object : ShortArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Short>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = integerExamples
}

open class IntArg(override val name: String = "Int") : ArgumentType<Int>() {
    companion object : IntArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Int>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = integerExamples
}

open class LongArg(override val name: String = "Long") : ArgumentType<Long>() {
    companion object : LongArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Long>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = integerExamples
}

open class FloatArg(override val name: String = "Float") : ArgumentType<Float>() {
    companion object : FloatArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Float>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = decimalExamples
}

open class DoubleArg(override val name: String = "Double") : ArgumentType<Double>() {
    companion object : DoubleArg()

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion<Double>(name, arg)
    override fun generateExamples(event: CommandEvent<*>) = decimalExamples
}
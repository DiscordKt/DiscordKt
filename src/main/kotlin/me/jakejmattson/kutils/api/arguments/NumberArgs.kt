package me.jakejmattson.kutils.api.arguments

import me.jakejmattson.kutils.api.dsl.arguments.*
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import kotlin.random.Random
import kotlin.reflect.KClass

private fun <T : Number> genericConversion(name: String, arg: String, clazz: KClass<T>): ArgumentResult<T> {
    val result = when (clazz) {
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



sealed class IntegerType<T: Number>(override val name: String, private val clazz: KClass<T>) : ArgumentType<T>() {
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion(name, arg, clazz)
    override fun generateExamples(event: CommandEvent<*>) = (0..10).map { it.toString() }
}

sealed class DecimalType<T: Number>(private val typeName: String, private val clazz: KClass<T>) : ArgumentType<T>() {
    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>) = genericConversion(typeName, arg, clazz)
    override fun generateExamples(event: CommandEvent<*>) = listOf("%.2f".format(Random.nextDouble(0.00, 9.99)))
}

/**
 * Accept a whole number in the byte range.
 */
open class ByteArg(override val name: String = "Byte") : IntegerType<Byte>(name, Byte::class) {
    companion object : ByteArg()
}

/**
 * Accept a whole number in the short range.
 */
open class ShortArg(override val name: String = "Short") : IntegerType<Short>(name, Short::class) {
    companion object : ShortArg()
}

/**
 * Accept a whole number in the int range.
 */
open class IntArg(override val name: String = "Int") : IntegerType<Int>(name, Int::class) {
    companion object : IntArg()
}

/**
 * Accept a whole number in the long range.
 */
open class LongArg(override val name: String = "Long") : IntegerType<Long>(name, Long::class) {
    companion object : LongArg()
}

/**
 * Accept a decimal number in the float range.
 */
open class FloatArg(override val name: String = "Float") : DecimalType<Float>(name, Float::class) {
    companion object : FloatArg()
}

/**
 * Accept a decimal number in the double range.
 */
open class DoubleArg(override val name: String = "Double") : DecimalType<Double>(name, Double::class) {
    companion object : DoubleArg()
}
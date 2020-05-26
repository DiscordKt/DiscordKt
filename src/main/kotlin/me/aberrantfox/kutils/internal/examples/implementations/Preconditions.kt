package me.aberrantfox.kutils.internal.examples.implementations

import me.aberrantfox.kutils.api.annotations.Precondition
import me.aberrantfox.kutils.api.dsl.preconditions.*

//Preconditions are a way to validate the state of things, such as user roles, execution channel, etc.
//All preconditions must pass before a command can begin to be processed.
@Precondition
fun nameBeginsWithLetter() = precondition {
    if (it.author.name.toLowerCase().first() in 'a'..'z') {
        return@precondition Pass
    } else {
        return@precondition Fail("Your name must start with a letter!")
    }
}
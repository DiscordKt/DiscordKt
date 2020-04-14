package me.aberrantfox.kjdautils.examples.implementations

import me.aberrantfox.kjdautils.api.annotation.*
import me.aberrantfox.kjdautils.api.dsl.command.commands

//Services are the "business logic" within KUtils.
//They leverage dependency injection to allow you to simply define behaviors without worrying about instances.
//Each service can also intelligently accept injections. KUtils will figure out the required order to build each.
//The Services below will be built in the order you see written, as each service requires the last.
@Service
class NoDependency {
    val fieldExample = "NONE"
}

@Service
class SingleDependency(noDependency: NoDependency) {
    fun functionExample() = "SINGLE"
}

@Service
class DoubleDependency(private val noDependency: NoDependency, private val singleDependency: SingleDependency) {
    fun injectionExample() = "${noDependency.fieldExample}, ${singleDependency.functionExample()}, DOUBLE"
}

@CommandSet("Services Demo")
fun dependsOnAllServices(none: NoDependency, single: SingleDependency, double: DoubleDependency) = commands {
    command("Injection") {
        description = "I depend on all services"
        execute {
            it.respond(
                """
                    This command has access to every service.
                    ```
                    NoDependency    : ${none.fieldExample}
                    SingleDependency: ${single.functionExample()}
                    DoubleDependency: ${double.injectionExample()}
                    ```
                """.trimIndent()
            )
        }
    }
}
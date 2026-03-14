package me.jakejmattson.discordkt.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.test.AssertionMode
import io.kotest.engine.concurrency.SpecExecutionMode
import io.kotest.engine.concurrency.TestExecutionMode

object KotestProjectConfig : AbstractProjectConfig() {
    override val assertionMode = AssertionMode.Error
    override val failfast = true
    override val specExecutionMode = SpecExecutionMode.Concurrent
    override val testExecutionMode = TestExecutionMode.Concurrent
}
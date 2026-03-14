package me.jakejmattson.discordkt.arguments

import io.kotest.core.config.AbstractPackageConfig
import me.jakejmattson.discordkt.dsl.internalLocale
import me.jakejmattson.discordkt.locale.LocaleEN

class PackageConfig : AbstractPackageConfig() {
    init {
        internalLocale = LocaleEN()
    }
}
package org.jetbrains.dokka.base.allModulePage

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaGenerator
import org.jetbrains.dokka.utilities.DokkaLogger

class DokkaMultimoduleGenerator(val configuration: DokkaConfiguration, logger: DokkaLogger) {
    val generator = DokkaGenerator(configuration, logger)

    fun generate() {
        generator.generateAllModulesPage()
    }
}
package org.jetbrains.dokka.transformers.documentation

import org.jetbrains.dokka.model.DModuleView
import org.jetbrains.dokka.model.DPass
import org.jetbrains.dokka.plugability.DokkaContext

interface DocumentableMerger {
    operator fun invoke(moduleViews: Collection<DPass>, context: DokkaContext): DModuleView
}
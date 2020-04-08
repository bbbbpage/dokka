package org.jetbrains.dokka.transformers.documentation

import org.jetbrains.dokka.model.DModuleView
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.pages.RootPageNode

interface DocumentableToPageTranslator {
    operator fun invoke(moduleView: DModuleView): RootPageNode
}
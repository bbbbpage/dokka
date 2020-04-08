package org.jetbrains.dokka.transformers.pages

import org.jetbrains.dokka.DokkaModuleDescriptor
import org.jetbrains.dokka.pages.RootPageNode

interface PageCreator {
    fun create(modules: List<DokkaModuleDescriptor>): RootPageNode
}
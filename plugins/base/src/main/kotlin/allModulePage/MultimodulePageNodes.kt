package org.jetbrains.dokka.base.allModulePage

import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.pages.*


class MultimoduleRootPageNode(
    override val name: String,
    val links: List<Pair<DRI, String>>,
    override val dri: Set<DRI>,
    val pageContentBuilder: PageContentBuilder,
    val block: MultimoduleRootPageNode.() -> ContentGroup
) : RootPageNode(), ContentPage {

    override val children: List<PageNode> = emptyList()

    override val content: ContentNode = block(this)
    override val documentable: Documentable? = null
    override val embeddedResources: List<String> = emptyList()
    override fun modified(name: String, children: List<PageNode>): RootPageNode =
        MultimoduleRootPageNode(
            name,
            links,
            dri,
            pageContentBuilder,
            block
        )
    override fun modified(
        name: String,
        content: ContentNode,
        dri: Set<DRI>,
        embeddedResources: List<String>,
        children: List<PageNode>
    ): ContentPage = MultimoduleRootPageNode(
        name,
        links,
        dri,
        pageContentBuilder,
        block
    )
}
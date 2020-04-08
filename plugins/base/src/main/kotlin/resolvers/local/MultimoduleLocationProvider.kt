package org.jetbrains.dokka.base.resolvers.local

import org.jetbrains.dokka.base.renderers.platforms
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.dokka.pages.RootPageNode

object MultimoduleLocationProviderFactory : LocationProviderFactory {
    override fun getLocationProvider(pageNode: RootPageNode): LocationProvider =
        MultimoduleLocationProvider(pageNode)
}

class MultimoduleLocationProvider(private val root: RootPageNode) : LocationProvider {
    override fun resolve(dri: DRI, platforms: List<PlatformData>, context: PageNode?): String =
        if (dri.classNames == null) "index.html"
        else "${dri.classNames}/index.html"

    override fun resolve(node: PageNode, context: PageNode?, skipExtension: Boolean): String =
        (node as? ContentPage)?.let { cp ->
            cp.dri.firstOrNull()
                ?.let { resolve(it, cp.platforms(), null) }?.let {
                    if (skipExtension) it?.removeSuffix(".html") else it
                }
        }.orEmpty()


    override fun resolveRoot(node: PageNode): String = "index.html"

    override fun ancestors(node: PageNode): List<PageNode> = listOf(root)

}
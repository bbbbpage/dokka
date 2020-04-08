package org.jetbrains.dokka.base.allModulePage

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaModuleDescriptor
import org.jetbrains.dokka.EnvironmentAndFacade
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.PlatformData
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.parsers.MarkdownParser
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.transformers.pages.PageCreator
import org.jetbrains.dokka.utilities.DokkaLogger
import java.io.File

class MultimodulePageCreator(
    private val context: DokkaContext
) : PageCreator {
    private val configuration: DokkaConfiguration = context.configuration
    private val logger: DokkaLogger = context.logger
    private val platforms: Map<PlatformData, EnvironmentAndFacade> = context.platforms

    override fun create(modules: List<DokkaModuleDescriptor>): RootPageNode {
        val parser = MarkdownParser(platforms.values.first().facade, logger = logger)
        val submodulesWithDocs = modules.map { module ->
            DRI(packageName = "ext", classNames = module.name) to module.docFile.let(::File).readText()
                .let {
                    parser.parse(it).firstParagraph()
                }
        }
        val moduleDoc =
            modules.firstOrNull()?.let { File(it.path).parentFile.parentFile.resolve(File(it.docFile).name) }?.let {
                parser.parse(it.readText())
            }.firstParagraph()

        val commentsConverter = context.plugin(DokkaBase::class)?.querySingle { commentsToContentConverter }
        val signatureProvider = context.plugin(DokkaBase::class)?.querySingle { signatureProvider }
        if (commentsConverter == null || signatureProvider == null)
            throw IllegalStateException("Both comments converter and signature provider must not be null")

        val platformDatas = configuration.passesConfigurations.map { it.platformData }.toSet()
        val builder = PageContentBuilder(commentsConverter, signatureProvider, context.logger)

        return MultimoduleRootPageNode("Modules", submodulesWithDocs, setOf(DRI(packageName = "ext")), builder) {
            builder.contentFor(dri = DRI("ext"), platformData = platformDatas, kind = ContentKind.Cover) {
                text("All modules:")
                if (moduleDoc.isNotEmpty()) text(moduleDoc)
                table(DRI("ext"), platformData = platformDatas) {
                    links.map { (dri, doc) ->
                        buildGroup {
                            link(dri.classNames.orEmpty(), dri)
                            text(doc)
                        }
                    }
                }
            }
        }
    }

    fun DocumentationNode?.firstParagraph() =
        this?.children.orEmpty().flatMap { it.root.children }.filterIsInstance<P>().firstOrNull()
            ?.docTagSummary()
            .orEmpty()
}
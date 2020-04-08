package org.jetbrains.dokka.gradle

import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.dokka.DokkaBootstrap
import java.net.URLClassLoader
import java.util.function.BiConsumer

open class DokkaMultimoduleTask : DefaultTask() {

    @Input
    var outputDirectory: String = ""

    @Input
    var documentationFileName: String = ""

    var dokkaRuntime: Configuration? = null

    var pluginsConfiguration: Configuration? = null

    @TaskAction
    fun dokkaMultiplatform() {
        val kotlinColorsEnabledBefore = System.getProperty(DokkaTask.COLORS_ENABLED_PROPERTY) ?: "false"
        System.setProperty(DokkaTask.COLORS_ENABLED_PROPERTY, "false")

        try {
            loadFatJar()

//            val pluginClassLoader =
//                URLClassLoader(pluginsConfiguration?.resolve().orEmpty().map { it.toURI().toURL() }.toTypedArray(), ClassloaderContainer.fatJarClassLoader)
            val bootstrapClass =
                ClassloaderContainer.fatJarClassLoader!!.loadClass("org.jetbrains.dokka.DokkaMultimoduleBootstrapImpl")
            val bootstrapInstance = bootstrapClass.constructors.first().newInstance()
            val bootstrapProxy: DokkaBootstrap = automagicTypedProxy(
                javaClass.classLoader,
                bootstrapInstance
            )

            val gson = GsonBuilder().setPrettyPrinting().create()
            val dokkaTask = project.tasks.findByName(DOKKA_TASK_NAME) as? DokkaTask
            val config = dokkaTask?.let { it.config ?: it.getConfiguration() }
            val configuration = project.subprojects
                .map {
                    GradleDokkaModuleDescriptor().apply {
                        name = it.name
                        path = it.projectDir.resolve(it.tasks.withType(DokkaTask::class.java).first().outputDirectory)
                            .toString()
                        docFile = it.projectDir.resolve(documentationFileName).toString()
                    }
                }
                .let {
                    config!!.apply {
                        descriptors = it
                    }
                }


//            val configuration = config?.copy(
//                passesConfigurations = getProjects(project).map { p ->
//                    GradlePassConfigurationImpl(name = p.name).apply {
//                        moduleName = p.name
//                        documentationFile =
//                            p.buildFile.parentFile.resolve(documentationFileName).takeIf { it.isFile && it.exists() }
//                                ?.absolutePath
//                        sourceRoots = p.getSources().map { it.parent }.distinct()
//                            .map { GradleSourceRootImpl().apply { path = it } }.toMutableList()
//                    }
//                }
//            ) ?: throw IllegalStateException("Cannot obtain dokka configuration")
//
            bootstrapProxy.configure(
                BiConsumer { level, message ->
                    when (level) {
                        "debug" -> logger.debug(message)
                        "info" -> logger.info(message)
                        "progress" -> logger.lifecycle(message)
                        "warn" -> logger.warn(message)
                        "error" -> logger.error(message)
                    }
                },
                gson.toJson(configuration)
            )

            bootstrapProxy.generate()
        } finally {
            System.setProperty(DokkaTask.COLORS_ENABLED_PROPERTY, kotlinColorsEnabledBefore)
        }
    }

    private fun getProjects(project: Project): Set<Project> =
        project.subprojects + project.subprojects.flatMap { getProjects(it) }

    private fun Project.getSources() =
        getCompileTasks().flatMap { it.inputs.files }.filterNot { it.name.endsWith(".jar") }.distinct()

    private fun Project.getCompileTasks(): List<Task> =
        (project.tasks.filter { it.name.contains("compileKotlin") } + project.tasks.findByName("compileJava"))
            .filterNotNull()

    private fun loadFatJar() {
        if (ClassloaderContainer.fatJarClassLoader == null) {
            val jars = dokkaRuntime!!.resolve()
            ClassloaderContainer.fatJarClassLoader = URLClassLoader(
                jars.map { it.toURI().toURL() }.toTypedArray(),
                ClassLoader.getSystemClassLoader().parent
            )
        }
    }
}

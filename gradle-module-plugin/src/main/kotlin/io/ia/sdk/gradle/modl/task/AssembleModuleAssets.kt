package io.ia.sdk.gradle.modl.task

import io.ia.sdk.gradle.modl.PLUGIN_TASK_GROUP
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Task which should apply to any subproject which provides dependencies to the module in the form of jar files.
 *
 * This task is auto-applied to any module projects/subprojects which have applied the `java` plugin.
 */
open class AssembleModuleAssets @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

    companion object {
        const val ID = "assembleModuleAssets"
    }

    init {
        this.group = PLUGIN_TASK_GROUP
        this.description =
            "Assembles module assets into the 'moduleContents' folder in the module project's build directory."
    }

    /**
     * Folder that assets will be collected into.
     */
    @OutputDirectory
    val moduleContentDir: DirectoryProperty = objects.directoryProperty()

    /**
     * Source directories for assets provided by subprojects
     */
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val moduleArtifactDirs: SetProperty<DirectoryProperty> = objects.setProperty(DirectoryProperty::class.java)

    @Input
    val license: Property<String> = objects.property(String::class.java)

    @TaskAction
    fun execute() {
        project.logger.info("Assembling module structure in '${moduleContentDir.get().asFile.absolutePath}'")

        val sources = moduleArtifactDirs.get().map { it.get() }

        project.copy { copySpec ->
            copySpec.from(sources)
            copySpec.into(moduleContentDir)
            copySpec.exclude("manifest.json")
        }

        if (license.isPresent && license.get().isNotEmpty()) {
            project.copy {
                it.from(license.get())
                it.into(moduleContentDir)
            }
        }
    }
}

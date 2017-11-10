package ru.kinca.gradle.googledrive

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class DefaultsIntegrationSpec
extends Specification
{
    private static final String DESTINATION_NAME = 'whatever'
    private static final File FILE_PARAM =
        new File("./subdir/$DESTINATION_NAME")

    void "Default permission is set on existing 'uploadToDrive' task"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)
        def task = project.tasks.getByName('uploadToDrive')

        when:
        project.extensions.configure(GoogleDriveUploaderPlugin.EXTENSION_NAME) {
            it.file = FILE_PARAM
        }

        then:
        with(task) {
            destinationName == DESTINATION_NAME
            permissions == UploadTask.DEFAULT_PERMISSIONS
            file == FILE_PARAM
        }
    }

    void "Default permission is set on new UploadTask"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)

        when:
        def task = project.task('newUploadToDrive', type: UploadTask)
        task.file = FILE_PARAM

        then:
        with(task) {
            permissions == UploadTask.DEFAULT_PERMISSIONS
            file == FILE_PARAM
            destinationName == DESTINATION_NAME
        }
    }
}

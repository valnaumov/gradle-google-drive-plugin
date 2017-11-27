package ru.kinca.gradle.googledrive

import com.google.api.services.drive.model.Permission

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class DefaultsIntegrationSpec
extends Specification
{
    private static final String DESTINATION_NAME = 'whatever'
    private static final File FILE = new File("./subdir/$DESTINATION_NAME")
    private static final List<Permission> PERMISSIONS =
        [new Permission().setRole('a role')]

    void "Default permission is set on existing 'uploadToDrive' task"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)
        def task = project.tasks.getByName(GoogleDriveUploaderPlugin
            .DEFAULT_TASK_NAME)

        when:
        project.extensions.configure(GoogleDriveUploaderPlugin.EXTENSION_NAME) {
            it.file = FILE
        }

        then:
        with(task) {
            destinationName == DESTINATION_NAME
            permissions == UploadTask.DEFAULT_PERMISSIONS
            file == FILE
        }
    }

    void "Default permission is set on new UploadTask"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)

        when:
        def task = project.task('newUploadToDrive', type: UploadTask)
        task.file = FILE

        then:
        with(task) {
            permissions == UploadTask.DEFAULT_PERMISSIONS
            file == FILE
            destinationName == DESTINATION_NAME
        }
    }

    void "Defaults are not applied to an existing 'uploadToDrive' task"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)
        def task = project.tasks.getByName(GoogleDriveUploaderPlugin
            .DEFAULT_TASK_NAME)

        when: 'values are specified explicitly'
        project.extensions.configure(GoogleDriveUploaderPlugin.EXTENSION_NAME) {
            it.file = FILE
            it.permissions = PERMISSIONS
            it.destinationName = DESTINATION_NAME
        }

        then:
        with(task) {
            destinationName == DESTINATION_NAME
            permissions == PERMISSIONS
            file == FILE
        }
    }

    void "Defaults are not applied to a new UploadTask"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)

        when: 'values are specified explicitly'
        def task = project.task('newUploadToDrive', type: UploadTask)

        task.with {
            file = FILE
            permissions = PERMISSIONS
            destinationName = DESTINATION_NAME
        }

        then:
        with(task) {
            destinationName == DESTINATION_NAME
            permissions == PERMISSIONS
            file == FILE
        }
    }
}

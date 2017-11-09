package ru.kinca.gradle.googledrive

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class DefaultsIntegrationSpec
extends Specification
{
    void "Default permission is set on existing 'uploadToDrive' task"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)

        expect:
        project.tasks.getByName('uploadToDrive').permissions ==
            UploadTask.DEFAULT_PERMISSIONS
    }

    void "Default permission is set on new UploadTask"()
    {
        given:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GoogleDriveUploaderPlugin)

        expect:
        project.task('newUploadToDrive', type: UploadTask).permissions ==
            UploadTask.DEFAULT_PERMISSIONS
    }
}

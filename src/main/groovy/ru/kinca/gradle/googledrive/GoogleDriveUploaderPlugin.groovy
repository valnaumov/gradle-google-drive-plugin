package ru.kinca.gradle.googledrive

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin that compresses specified files to an archive and uploads it to
 * Google Drive.
 *
 * @author Valentin Naumov.
 */
class GoogleDriveUploaderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.quiet 'GoogleDriveUploaderPlugin applied.'
    }
}

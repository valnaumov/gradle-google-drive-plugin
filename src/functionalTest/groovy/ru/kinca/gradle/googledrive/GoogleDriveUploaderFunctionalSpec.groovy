package ru.kinca.gradle.googledrive

import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.drive.model.File as GFile

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Shared
import spock.lang.Specification

class GoogleDriveUploaderFunctionalSpec
extends Specification
{
    private static final String UPLOAD_TASK_NAME = 'uploadToDrive'
    private static final String DESTINATION_NAME = 'uploaded-build.gradle'

    private static final String CLIENT_SECRET =
        System.getenv('DRIVE_CLIENT_SECRET')
    private static final String CLIENT_ID =
        System.getenv('DRIVE_CLIENT_ID')

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    @Shared
    GoogleClient googleClient  = new GoogleClient(CLIENT_ID, CLIENT_SECRET,
            new MemoryDataStoreFactory())

    @ClassRule
    @Shared
    TemporaryDriveFolder temporaryDriveFolder =
        new TemporaryDriveFolder(googleClient)

    void setupSpec()
    {
        googleClient
    }

    void setup()
    {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'ru.kinca.google-drive-uploader'
            }
        """
    }

    void 'Uploads single file to existing dir'()
    {
        setup:
        buildFile << """
            googleDrive {
                destinationFolder = '${temporaryDriveFolder.createdFolder
                    .getName()}'
                destinationName = '${DESTINATION_NAME}'
                file = project.file('build.gradle')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(UPLOAD_TASK_NAME)
            .withPluginClasspath()
            .build()

        then:
        result.task(":${UPLOAD_TASK_NAME}").outcome == TaskOutcome.SUCCESS
        temporaryDriveFolder.hasFile(DESTINATION_NAME)

        result.output.contains("File '$buildFile.absolutePath' is uploaded" +
            " to ${temporaryDriveFolder.createdFolder.getName()}" +
            " and named $DESTINATION_NAME.")
    }

    void 'Uploads single file and creates missing dirs'()
    {
        setup:
        GFile secondDir = temporaryDriveFolder.createFolder('second')
        String destinationFolder = temporaryDriveFolder.createdFolder
            .getName() + '/' + secondDir.getName()

        buildFile << """
            googleDrive {
                destinationFolder = '$destinationFolder'
                destinationName = '${DESTINATION_NAME}'
                file = project.file('build.gradle')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(UPLOAD_TASK_NAME)
            .withPluginClasspath()
            .build()

        then:
        result.task(":${UPLOAD_TASK_NAME}").outcome == TaskOutcome.SUCCESS
        DriveUtils.hasFile(googleClient.drive, secondDir, DESTINATION_NAME)

        result.output.contains("File '$buildFile.absolutePath' is uploaded" +
            " to $destinationFolder and named $DESTINATION_NAME.")
    }
}

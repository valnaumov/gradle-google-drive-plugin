package ru.kinca.gradle.googledrive

import com.google.api.client.http.FileContent
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.drive.model.File as GFile

import org.gradle.internal.impldep.org.apache.commons.io.IOUtils
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule

import spock.lang.Shared

class GoogleDriveUploaderFunctionalSpec
extends AbstractFunctionalSpec
{
    private static final String DESTINATION_NAME = 'uploaded.txt'

    private static final String CLIENT_SECRET =
        System.getenv('DRIVE_CLIENT_SECRET')
    private static final String CLIENT_ID =
        System.getenv('DRIVE_CLIENT_ID')

    @Shared
    File uploadedFile = getFile('uploaded.txt')

    @Shared
    GoogleClient googleClient = new GoogleClient(CLIENT_ID, CLIENT_SECRET,
            new MemoryDataStoreFactory())

    @Rule
    TemporaryDriveFolder temporaryDriveFolder =
        new TemporaryDriveFolder(googleClient)

    void 'Uploads single file to existing dir'()
    {
        setup:
        buildFile << """
            googleDrive {
                destinationFolder = '${temporaryDriveFolder.createdFolder
                    .getName()}'
                destinationName = '$DESTINATION_NAME'
                file = project.file('$uploadedFilePathEscaped')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
            }
        """

        when:
        def result = build()

        then:
        result.task(":${GoogleDriveUploaderPlugin.DEFAULT_TASK_NAME}")
            .outcome == TaskOutcome.SUCCESS
        temporaryDriveFolder.hasFile(DESTINATION_NAME)

        verifyHasLink(result.output)
        result.output.contains("File '$uploadedFile.canonicalPath' is" +
            " uploaded to '${temporaryDriveFolder.createdFolder.getName()}'" +
            " and named '$DESTINATION_NAME'.")
    }

    void 'Uploads single file and creates missing dirs'()
    {
        setup:
        String createdDirName = 'subdir'
        String destinationFolder = temporaryDriveFolder.createdFolder
            .getName() + '/' + createdDirName

        buildFile << """
            googleDrive {
                destinationFolder = '$destinationFolder'
                destinationName = '$DESTINATION_NAME'
                file = project.file('$uploadedFilePathEscaped')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
            }
        """

        when:
        def result = build()

        then:
        result.task(":${GoogleDriveUploaderPlugin.DEFAULT_TASK_NAME}")
            .outcome == TaskOutcome.SUCCESS

        GFile createdDir = DriveUtils.findInFolder(googleClient.drive,
            temporaryDriveFolder.createdFolder, createdDirName).first()
        DriveUtils.hasFile(googleClient.drive, createdDir, DESTINATION_NAME)

        verifyHasLink(result.output)
        result.output.contains("File '$uploadedFile.canonicalPath' is" +
            " uploaded to '$destinationFolder' and named '$DESTINATION_NAME'.")
    }

    void 'Updates existing file by default'()
    {
        setup:
        def existingGFile = uploadExistingFile()

        String destinationFolder = temporaryDriveFolder.createdFolder.getName()
        buildFile << """
            googleDrive {
                destinationFolder = '$destinationFolder'
                destinationName = '$DESTINATION_NAME'
                file = project.file('$uploadedFilePathEscaped')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
            }
        """

        when:
        def result = build()

        then:
        result.task(":${GoogleDriveUploaderPlugin.DEFAULT_TASK_NAME}")
            .outcome == TaskOutcome.SUCCESS

        // File id remains the same.
        def remoteUpdatedFilesIS = googleClient.drive.files().get(existingGFile
            .getId()).executeMediaAsInputStream()

        // Streams should be closed.
        remoteUpdatedFilesIS.withCloseable { a ->
            uploadedFile.newInputStream().withCloseable { b ->
                IOUtils.contentEquals(a, b)
            }
        }

        verifyHasLink(result.output)
        with(result.output) {
            contains("File with name '$DESTINATION_NAME' already" +
                " exists, id: ${existingGFile.getId()}. Updating...")

            contains("File '$uploadedFile.canonicalPath' is uploaded" +
                " to '$destinationFolder' and named '$DESTINATION_NAME'.")
        }
    }

    void 'Does not update existing file if set so'()
    {
        setup:
        def existingGFile = uploadExistingFile()

        String destinationFolder = temporaryDriveFolder.createdFolder.getName()
        buildFile << """
            googleDrive {
                destinationFolder = '$destinationFolder'
                destinationName = '$DESTINATION_NAME'
                file = project.file('$uploadedFilePathEscaped')
                clientId  = '$CLIENT_ID'
                clientSecret = '$CLIENT_SECRET'
                updateIfExists = false
            }
        """

        when:
        def result = buildAndFail()

        then:
        googleClient.drive.files().get(existingGFile.getId()).execute()
            .getModifiedTime() == existingGFile.getModifiedTime()

        result.output.contains('Remote file(s) already exists, id: '
            + [existingGFile.getId()])
    }

    GFile uploadExistingFile()
    {
        def existingFileContent = new FileContent('application/octet-stream',
            getFile('existing.txt'))

        googleClient.drive.files().create(
            new GFile()
                .setName(DESTINATION_NAME)
                .setParents([temporaryDriveFolder.createdFolder.getId()]),
            existingFileContent).execute()
    }

    static void verifyHasLink(
        String output)
    {
        println "output : $output"
        assert output =~ $/Short link: https://drive.google.com/open\?id=\w+/$
    }

    /**
     * @return escaped local path of file that is to be uploaded to drive.
     */
    String getUploadedFilePathEscaped()
    {
        uploadedFile.absolutePath.replace('\\', '\\\\')
    }
}

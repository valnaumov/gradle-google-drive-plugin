package ru.kinca.gradle.googledrive

import com.google.api.client.http.FileContent
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveRequest
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.model.Permission

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that uploads specified file to Google Drive. Opens a browser to
 * authorize, if was not authorized before.
 *
 * @author Valentin Naumov
 */
class UploadTask
extends DefaultTask
{
    protected static final List<Permission> DEFAULT_PERMISSIONS =
        [new Permission().setType('anyone').setRole('reader')]

    protected static final Boolean DEFAULT_UPDATE_IF_EXISTS = true

    private final PropertyState<String> destinationFolderPropertyState

    private final PropertyState<String> destinationNamePropertyState

    private final PropertyState<File> filePropertyState

    private final PropertyState<String> clientIdPropertyState

    private final PropertyState<String> clientSecretPropertyState

    private final PropertyState<List<Permission>> permissionsPropertyState

    private final PropertyState<Boolean> updateIfExistsPropertyState

    UploadTask()
    {
        destinationFolderPropertyState = project.property(String)
        destinationNamePropertyState = new PropertyStateWithDefaultValue<>(
            { file.name })
        filePropertyState = project.property(File)
        clientIdPropertyState = project.property(String)
        clientSecretPropertyState = project.property(String)
        permissionsPropertyState = new PropertyStateWithDefaultValue<
            List<Permission>>(DEFAULT_PERMISSIONS)

        // Primitive properties are assigned default values, we need to override
        updateIfExistsPropertyState = new PropertyStateWithDefaultValue<>(
            DEFAULT_UPDATE_IF_EXISTS)
    }

    @TaskAction
    void upload()
    {
        GoogleClient googleClient = new GoogleClient(
            clientId,
            clientSecret,
            new FileDataStoreFactory(
                new File(System.getProperty('user.home'),
                '.credentials/google-drive-uploader')))

        String destinationFolderId = DriveUtils.makeDirs(
            googleClient.drive, 'root',
            GoogleDriveUploaderPlugin.toPathElements(destinationFolder))

        DriveFile driveFile = new DriveFile()
        driveFile.setName(destinationName)
        driveFile.setParents([destinationFolderId])

        FileContent content = new FileContent('application/octet-stream', file)
        DriveRequest<DriveFile> modificationRequest

        List<DriveFile> existingDestinationFiles = DriveUtils.findInFolder(
            googleClient.drive, destinationFolderId, destinationName)
        if (existingDestinationFiles)
        {
            if (updateIfExists)
            {
                // Update the most recent, if the are many with the same name
                DriveFile updatedFile = existingDestinationFiles
                    .toSorted { it.getModifiedTime() }.first()

                logger.info("File with name '${destinationName}' already" +
                    " exists, id: ${updatedFile.getId()}. Updating...")
                modificationRequest = googleClient.drive.files().update(
                    updatedFile.getId(), null, content)
            }
            else
            {
                throw new GradleException('Remote file(s) already exists,' +
                    " id: ${existingDestinationFiles*.getId()}")
            }
        }
        else
        {
            logger.info('Creating file...')
            modificationRequest = googleClient.drive.files()
                .create(driveFile, content)
        }

        modificationRequest.getMediaHttpUploader().with {
            progressListener = {
                logger.info('Uploaded: {} {}[bytes]({})',
                    it.uploadState,
                    String.format('%,3d', it.numBytesUploaded),
                    String.format('%2.1f%%', it.progress * 100))
            }
        }

        DriveFile updated = modificationRequest.execute()

        logger.debug('Creating permissions...')
        permissions.each {
            googleClient.drive.permissions().create(updated.getId(), it)
        }

        logger.info("File '${file.canonicalPath}' is uploaded to" +
            " '$destinationFolder' and named '$destinationName'.")
        logger.quiet("Google Drive short link: ${getLink(updated)}")
    }

    private static String getLink(
        DriveFile file)
    {
        "https://drive.google.com/open?id=${file.getId()}"
    }

    @Input
    String getDestinationFolder()
    {
        destinationFolderPropertyState.get()
    }

    void setDestinationFolder(
        String value)
    {
        destinationFolderPropertyState.set(value)
    }

    void setDestinationFolderProvider(
        Provider<String> value)
    {
        destinationFolderPropertyState.set(value)
    }

    @Input
    String getDestinationName()
    {
        destinationNamePropertyState.get()
    }

    void setDestinationName(
        String value)
    {
        destinationNamePropertyState.set(value)
    }

    void setDestinationNameProvider(
        Provider<String> value)
    {
        destinationNamePropertyState.set(value)
    }

    @InputFile
    File getFile()
    {
        filePropertyState.get()
    }

    void setFile(
        File value)
    {
        filePropertyState.set(value)
    }

    void setFileProvider(
        Provider<File> value)
    {
        filePropertyState.set(value)
    }

    String getClientId()
    {
        clientIdPropertyState.get()
    }

    void setClientId(
        String value)
    {
        clientIdPropertyState.set(value)
    }

    void setClientIdProvider(
        Provider<String> value)
    {
        clientIdPropertyState.set(value)
    }

    String getClientSecret()
    {
        clientSecretPropertyState.get()
    }

    void setClientSecret(
        String value)
    {
        clientSecretPropertyState.set(value)
    }

    void setClientSecretProvider(
        Provider<String> value)
    {
        clientSecretPropertyState.set(value)
    }

    @Input
    List<Permission> getPermissions()
    {
        permissionsPropertyState.get()
    }

    void setPermissions(
        List<Permission> value)
    {
        permissionsPropertyState.set(value)
    }

    void setPermissionsProvider(
        Provider<List<Permission>> value)
    {
        permissionsPropertyState.set(value)
    }

    @Input
    Boolean getUpdateIfExists()
    {
        updateIfExistsPropertyState.get()
    }

    void setUpdateIfExists(
        Boolean value)
    {
        updateIfExistsPropertyState.set(value)
    }

    void setUpdateIfExistsProvider(
        Provider<Boolean> value)
    {
        updateIfExistsPropertyState.set(value)
    }
}

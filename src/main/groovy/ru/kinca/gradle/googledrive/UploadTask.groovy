package ru.kinca.gradle.googledrive

import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.http.FileContent
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveRequest
import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.model.Permission

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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

    private final Property<String> destinationFolderProperty

    private final Property<String> destinationNameProperty

    private final Property<File> fileProperty

    private final Property<String> clientIdProperty

    private final Property<String> clientSecretProperty

    private final Property<List<Permission>> permissionsProperty

    private final Property<Boolean> updateIfExistsProperty

    private final Property<File> credentialsDirProperty

    UploadTask()
    {
        destinationFolderProperty = project.objects.property(String)
        destinationNameProperty = project.objects.property(String)
        fileProperty = project.objects.property(File)
        clientIdProperty = project.objects.property(String)
        clientSecretProperty = project.objects.property(String)
        permissionsProperty = project.objects.property(List)

        // Wrapper type properties are assigned default values, we need to
        // override
        updateIfExistsProperty = project.objects.property(Boolean)
        updateIfExistsProperty.set(null as Boolean)

        credentialsDirProperty = project.objects.property(File)
    }

    @TaskAction
    void upload()
    {
        GoogleClient googleClient = new GoogleClient(
            clientId,
            clientSecret,
            new FileDataStoreFactory(credentialsDir))

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
        BatchRequest permissionsBatchRequest = googleClient.drive.batch()
        permissions.each {
            googleClient.drive.permissions().create(updated.getId(), it)
                .queue(permissionsBatchRequest, new SimpleJsonBatchCallBack(
                'Could not update permissions'))
        }
        permissionsBatchRequest.execute()

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
        destinationFolderProperty.get()
    }

    void setDestinationFolder(
        String value)
    {
        destinationFolderProperty.set(value)
    }

    void setDestinationFolderProvider(
        Provider<String> value)
    {
        destinationFolderProperty.set(value)
    }

    @Input
    String getDestinationName()
    {
        destinationNameProperty.getOrElse(file.name)
    }

    void setDestinationName(
        String value)
    {
        destinationNameProperty.set(value)
    }

    void setDestinationNameProvider(
        Provider<String> value)
    {
        destinationNameProperty.set(value)
    }

    @InputFile
    File getFile()
    {
        fileProperty.get()
    }

    void setFile(
        File value)
    {
        fileProperty.set(value)
    }

    void setFileProvider(
        Provider<File> value)
    {
        fileProperty.set(value)
    }

    String getClientId()
    {
        clientIdProperty.get()
    }

    void setClientId(
        String value)
    {
        clientIdProperty.set(value)
    }

    void setClientIdProvider(
        Provider<String> value)
    {
        clientIdProperty.set(value)
    }

    String getClientSecret()
    {
        clientSecretProperty.get()
    }

    void setClientSecret(
        String value)
    {
        clientSecretProperty.set(value)
    }

    void setClientSecretProvider(
        Provider<String> value)
    {
        clientSecretProperty.set(value)
    }

    @Input
    List<Permission> getPermissions()
    {
        permissionsProperty.getOrElse(DEFAULT_PERMISSIONS)
    }

    void setPermissions(
        List<Permission> value)
    {
        permissionsProperty.set(value)
    }

    void setPermissionsProvider(
        Provider<List<Permission>> value)
    {
        permissionsProperty.set(value)
    }

    @Input
    Boolean getUpdateIfExists()
    {
        updateIfExistsProperty.getOrElse(DEFAULT_UPDATE_IF_EXISTS)
    }

    void setUpdateIfExists(
        Boolean value)
    {
        updateIfExistsProperty.set(value)
    }

    void setUpdateIfExistsProvider(
        Provider<Boolean> value)
    {
        updateIfExistsProperty.set(value)
    }

    @Internal
    File getCredentialsDir()
    {
        credentialsDirProperty.present ? credentialsDirProperty.get()
            : new File(System.getProperty('user.home'),
                '.credentials/google-drive-uploader')
    }

    void setCredentialsDir(
        File value)
    {
        credentialsDirProperty.set(value)
    }

    void setCredentialsDirProvider(
        Provider<File> value)
    {
        credentialsDirProperty.set(value)
    }
}

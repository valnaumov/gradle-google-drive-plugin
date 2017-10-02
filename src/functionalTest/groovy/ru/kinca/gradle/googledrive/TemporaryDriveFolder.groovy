package ru.kinca.gradle.googledrive

import com.google.api.services.drive.model.File

import org.junit.rules.ExternalResource

/**
 * @author Valentin Naumov
 */
class TemporaryDriveFolder
extends ExternalResource
{
    private GoogleClient googleClient
    private File createdFolder

    TemporaryDriveFolder(
        GoogleClient googleClient)
    {
        this.googleClient = googleClient
    }

    @Override
    protected void before()
    throws Throwable
    {
        createdFolder = googleClient.drive.files().create(DriveUtils.newFolder()
            .setName(createUniqueName())).execute()
    }

    String createUniqueName()
    {
        UUID.randomUUID()
    }

    @Override
    protected void after()
    {
        googleClient.drive.files().delete(createdFolder.getId()).execute()
    }
    
    File getCreatedFolder()
    {
        createdFolder
    }

    boolean hasFile(
        String name)
    {
        DriveUtils.hasFile(googleClient.drive, createdFolder, name)
    }

    File createFolder(
        String name)
    {
        googleClient.drive.files().create(DriveUtils.newFolder()
            .setName(name)
            .setParents([ createdFolder.getId() ])).execute()
    }
}

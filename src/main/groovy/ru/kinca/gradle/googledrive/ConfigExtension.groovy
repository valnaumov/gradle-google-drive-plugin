package ru.kinca.gradle.googledrive

import com.google.api.services.drive.model.Permission

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Extension that allows to configure the plugin in a declarative way.
 *
 * @author Valentin Naumov
 */
class ConfigExtension
{
    private final Property<String> destinationFolderProperty

    private final Property<String> destinationNameProperty

    private final Property<File> fileProperty

    private final Property<String> clientIdProperty

    private final Property<String> clientSecretProperty

    private final Property<List<Permission>> permissionProperty

    private final Property<Boolean> updateIfExistsProperty

    ConfigExtension(
        Project project)
    {
        destinationFolderProperty = project.objects.property(String)
        destinationNameProperty = project.objects.property(String)
        fileProperty = project.objects.property(File)
        clientIdProperty = project.objects.property(String)
        clientSecretProperty = project.objects.property(String)
        permissionProperty = project.objects.property(List)

        // Wrapper type properties are assigned default values, we need to
        // override
        updateIfExistsProperty = project.objects.property(Boolean)
        updateIfExistsProperty.set(null as Boolean)
    }

    String getDestinationFolder()
    {
        destinationFolderProperty.get()
    }

    void setDestinationFolder(
        String value)
    {
        destinationFolderProperty.set(value)
    }

    Provider<String> getDestinationFolderProvider()
    {
        destinationFolderProperty
    }

    String getDestinationName()
    {
        destinationNameProperty.get()
    }

    void setDestinationName(
        String value)
    {
        destinationNameProperty.set(value)
    }

    Provider<String> getDestinationNameProvider()
    {
        destinationNameProperty
    }

    File getFile()
    {
        fileProperty.get()
    }

    void setFile(
        File value)
    {
        fileProperty.set(value)
    }

    Provider<File> getFileProvider()
    {
        fileProperty
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

    Provider<String> getClientIdProvider()
    {
        clientIdProperty
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

    Provider<String> getClientSecretProvider()
    {
        clientSecretProperty
    }

    List<Permission> getPermissions()
    {
        permissionProperty.get()
    }

    void setPermissions(
        List<Permission> value)
    {
        permissionProperty.set(value)
    }

    Provider<List<Permission>> getPermissionsProvider()
    {
        permissionProperty
    }

    Boolean getUpdateIfExists()
    {
        updateIfExistsProperty.get()
    }

    void setUpdateIfExists(
        Boolean value)
    {
        updateIfExistsProperty.set(value)
    }

    Provider<Boolean> getUpdateIfExistsProvider()
    {
        updateIfExistsProperty
    }
}

package ru.kinca.gradle.googledrive

import org.gradle.api.Project
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

/**
 * Extension that allows to configure the plugin in a declarative way.
 *
 * @author Valentin Naumov
 */
class ConfigExtension
{
    private final PropertyState<String> destinationFolderPropertyState

    private final PropertyState<String> destinationNamePropertyState

    private final PropertyState<File> filePropertyState

    private final PropertyState<String> clientIdPropertyState

    private final PropertyState<String> clientSecretPropertyState

    ConfigExtension(
        Project project)
    {
        destinationFolderPropertyState = project.property(String)
        destinationNamePropertyState = project.property(String)
        filePropertyState = project.property(File)
        clientIdPropertyState = project.property(String)
        clientSecretPropertyState = project.property(String)
    }

    String getDestinationFolder()
    {
        destinationFolderPropertyState.get()
    }

    void setDestinationFolder(
        String value)
    {
        destinationFolderPropertyState.set(value)
    }

    Provider<String> getDestinationFolderProvider()
    {
        destinationFolderPropertyState
    }

    String getDestinationName()
    {
        destinationNamePropertyState.get()
    }

    void setDestinationName(
        String value)
    {
        destinationNamePropertyState.set(value)
    }

    Provider<String> getDestinationNameProvider()
    {
        destinationNamePropertyState
    }

    File getFile()
    {
        filePropertyState.get()
    }

    void setFile(
        File value)
    {
        filePropertyState.set(value)
    }

    Provider<File> getFileProvider()
    {
        filePropertyState
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

    Provider<String> getClientIdProvider()
    {
        clientIdPropertyState
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

    Provider<String> getClientSecretProvider()
    {
        clientSecretPropertyState
    }
}

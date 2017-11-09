package ru.kinca.gradle.googledrive

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

import groovy.util.logging.Slf4j

import java.security.GeneralSecurityException

/**
 * A class that encapsulates creating and authorizing the {@lnik Drive}
 * instance.
 *
 * @author Valentin Naumov
 */
@Slf4j('logger')
class GoogleClient
{
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance()
    private static final String APPLICATION_NAME =
        'ru.kinca.google-drive-uploader'

    /**
     * If modifying these scopes, delete your previously saved credentials.
     */
    private static final List<String> SCOPES = [DriveScopes.DRIVE]

    private final String clientId
    private final String clientSecret
    private final DataStoreFactory dataStoreFactory

    private HttpTransport httpTransport
    private Credential credential

    private Drive drive

    GoogleClient(
        String clientId,
        String clientSecret,
        DataStoreFactory dataStoreFactory)
    {
        this.clientSecret = clientSecret
        this.clientId = clientId
        this.dataStoreFactory = dataStoreFactory
    }

    private void init()
    {
        if (!httpTransport)
        {
            try
            {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            }
            catch (GeneralSecurityException | IOException e)
            {
                throw new RuntimeException(
                    'Unable to establish http transport.', e)
            }
        }
    }

    private void authorize()
    throws IOException
    {
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientId, clientSecret,
                SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType('offline')
                .build()

        credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize('drive-user')
    }

    /**
     * Creates an authorized and ready to use {@link Drive}, returns the same
     * instance each time.
     *
     * @return authorized and ready to use {@link Drive} instance.
     */
    Drive getDrive()
    {
        if (drive)
        {
            return drive
        }

        init()
        authorize()
        new Drive.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}

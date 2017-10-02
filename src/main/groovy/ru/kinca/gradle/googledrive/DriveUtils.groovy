package ru.kinca.gradle.googledrive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File

/**
 * Useful utilities for {@link Drive}.
 *
 * @author Valentin Naumov
 */
@SuppressWarnings([ 'DuplicateStringLiteral', 'UnnecessaryGString' ])
final class DriveUtils
{
    private static final String FOLDER_MIME_TYPE =
        'application/vnd.google-apps.folder'
    private static final String DEFAULT_SPACES = 'drive'
    private static final String DEFAULT_COPRORA = 'user'

    private DriveUtils()
    {
    }

    /**
     * Creates a specified {@code path}, creating missing folders on drive
     * starting from the {@code parentId}.
     *
     * @param drive
     *        the Drive instance
     * @param parentId
     *        the folder to resolve path against
     * @param path
     * @return id of created folder
     */
    static String makeDirs(
        Drive drive,
        String parentId,
        List<String> path)
    {
        path.inject(parentId) { String currentParent, currName ->
            List<File> existingFolders = drive.files().list()
                .setSpaces(DEFAULT_SPACES)
                .setCorpora(DEFAULT_COPRORA)
                .setQ("name = '$currName'" +
                    " and '$currentParent' in parents" +
                    " and not trashed" +
                    " and mimeType = '$FOLDER_MIME_TYPE'")
                .execute()
                .getFiles()

            File folder
            if (existingFolders)
            {
                folder = existingFolders[0]
            }
            else
            {
                def toCreate = new File()
                    .setName(currName)
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setParents([currentParent])

                folder = drive.files().create(toCreate).execute()
            }

            folder.getId()
        }
    }

    static boolean hasFile(
        Drive drive,
        File folder,
        String fileName)
    {
        !drive.files().list()
            .setSpaces(DEFAULT_SPACES)
            .setCorpora(DEFAULT_COPRORA)
            .setQ("name = '$fileName'" +
                " and '${folder.getId()}' in parents" +
                " and not trashed" +
                " and mimeType != '$FOLDER_MIME_TYPE'")
            .execute().getFiles().empty
    }
    
    static File newFolder()
    {
        new File().setMimeType(FOLDER_MIME_TYPE)
    }
}

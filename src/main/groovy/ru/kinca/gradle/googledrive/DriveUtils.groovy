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
    private static final String DEFAULT_CORPORA = 'user'

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
                .setCorpora(DEFAULT_CORPORA)
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

    /**
     * Searches for a file (a folder or a regular file) in a folder specified
     * by its id.
     *
     * @param drive
     * @param parentId
     *        id of the containing folder.
     * @param childName
     *        name of the file to search for.
     * @return list of files found.
     */
    static List<File> findInFolder(
        Drive drive,
        String parentId,
        String childName)
    {
        String query = "'$parentId' in parents" +
            " and not trashed" +
            " and name = '$childName'"
        drive.files().list()
            .setSpaces(DEFAULT_SPACES)
            .setCorpora(DEFAULT_CORPORA)
            .setFields('files(id, name)')
            .setQ(query)
            .execute()
            .getFiles()
    }

    /**
     * Searches for a file (a folder or a regular file) in a folder specified
     * by its id.
     *
     * @param drive
     * @param parent
     *        containing folder.
     * @param childName
     *        name of the file to search for.
     * @return list of files found.
     */
    static List<File> findInFolder(
        Drive drive,
        File parent,
        String childName)
    {
        findInFolder(drive, parent.getId(), childName)
    }

    /**
     * Tests whether a given folder contains any regular files with name
     * {@code fileName}.
     *
     * @param drive
     * @param parent
     * @param childName
     * @return
     */
    static boolean hasFile(
        Drive drive,
        File parent,
        String childName)
    {
        findInFolder(drive, parent.getId(), childName).any {
            it.getMimeType() != FOLDER_MIME_TYPE
        }
    }

    static File newFolder()
    {
        new File().setMimeType(FOLDER_MIME_TYPE)
    }
}

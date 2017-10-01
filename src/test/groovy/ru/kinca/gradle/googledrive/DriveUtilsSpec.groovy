package ru.kinca.gradle.googledrive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import spock.lang.Specification

class DriveUtilsSpec
extends Specification
{
    Drive drive = Mock()

    void 'Makes dirs if none exist'()
    {
        setup:
        String parentId = 'parent-id'

        File existingFolder = new File()
            .setName('existing')
            .setId('existing-id')
        File missingFolder1 = new File()
            .setName('missingFolder1')
            .setId('missingFolder1-id')
        File missingFolder2 = new File()
            .setName('missingFolder2')
            .setId('missingFolder2-id')

        def folders = [existingFolder, missingFolder1, missingFolder2]
        def path = folders.collect {it.getName()}
        List<String> ids = [parentId, *folders.collect {it.getId()}]

        drive.files() >> Stub(Drive.Files) {
            create(_) >> { args ->
                File file = args[0]
                assert file.getMimeType() == DriveUtils.FOLDER_MIME_TYPE

                int folderIdx = path.indexOf(file.getName())
                assert file.getParents()[0] == ids[folderIdx]

                Stub(Drive.Files.Create) {
                    execute() >> new File().setId(ids[folderIdx + 1])
                }
            }

            list() >> Stub(Drive.Files.List) {
                // Mocking fluent API
                /setSpaces|setCorpora|setQ/(*_) >> it
                def noFiles = new FileList()
                execute() >>> [new FileList().setFiles([existingFolder]),
                               noFiles, noFiles]
            }
        }

        expect:
        'Non-existent folders created and' +
            ' Id of last folder in the path is returned'
        DriveUtils.makeDirs(drive, parentId, path) == ids.last()
    }
}

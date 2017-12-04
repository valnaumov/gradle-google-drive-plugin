package ru.kinca.gradle.googledrive

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

/**
 * @author Valentin Naumov
 */
abstract class AbstractFunctionalSpec
extends Specification
{
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    void setup()
    {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'ru.kinca.google-drive-uploader'
            }
        """
    }

    private GradleRunner createRunner()
    {
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(GoogleDriveUploaderPlugin.DEFAULT_TASK_NAME,
                '-i', '--stacktrace')
            .withPluginClasspath()
    }

    BuildResult buildAndFail()
    {
        createRunner().buildAndFail()
    }

    BuildResult build()
    {
        createRunner().build()
    }

    static getFile(
        String pathInResources)
    {
        new File('./src/functionalTest/resources/' + pathInResources)
    }
}

package com.github.mojos.distribute;

/*
 * Copyright 2001-2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes sources a Python module using distribute
 *
 * @goal process-sources
 * @phase process-sources
 */
public class ProcessSourcesMojo extends AbstractMojo {

    private static final String PROJECT_NAME = "${PROJECT_NAME}";
    private static final String VERSION = "${VERSION}";

    /**
     * @parameter default-value="${project.version}"
     * @required
     */
    private String packageVersion;

    /**
     * Allows overriding the default version
     */
    @Getter
    @Setter
    private String version;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${project.basedir}/src/main/python"
     * @required
     */
    private String sourceDirectory;

    /**
     * @parameter default-value="${project.artifactId}"
     * @required
     */
    private String packageName;

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (version != null) {
            packageVersion = version;
        }

        //Copy sourceDirectory
        final File sourceDirectoryFile = new File(sourceDirectory);
        final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "maven-python").toFile();

        try {
            FileUtils.copyDirectory(sourceDirectoryFile, buildDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy source", e);
        }

        final File setupTemplate = Paths.get(buildDirectory.getPath(), "setup-template.py").toFile();
        final boolean setupTemplateProvided = setupTemplate.exists();

        final File setupOutput = Paths.get(buildDirectory.getPath(), "setup.py").toFile();

        try {
            if (setupTemplateProvided) {
                //update VERSION to latest version
                List<String> lines = new ArrayList<>();
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(setupTemplate))) {
                    lines.addAll(IOUtils.readLines(inputStream));
                }

                int index = 0;
                for (String line : lines) {
                    line = line.replace(VERSION, packageVersion);
                    line = line.replace(PROJECT_NAME, packageName);
                    lines.set(index, line);
                    index++;
                }

                final OutputStream outputStream = new FileOutputStream(setupOutput);
                try {
                    IOUtils.writeLines(lines, "\n", outputStream);
                } finally {
                    outputStream.flush();
                    outputStream.close();
                }
            }
            project.getProperties().setProperty("python.distribute.plugin.setup.path", setupOutput.getCanonicalPath());
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to find " + setupOutput.getPath(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read " + setupOutput.getPath(), e);
        }
    }
}
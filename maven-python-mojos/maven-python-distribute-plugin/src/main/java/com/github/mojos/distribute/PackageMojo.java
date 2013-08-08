package com.github.mojos.distribute;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Packages a Python module using distribute
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

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

    /**
     * @parameter default-value="python"
     * @required
     */
    private String pythonExecutable;

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (version != null) {
            packageVersion = version;
        }

        //Copy sourceDirectory
        final File sourceDirectoryFile = new File(sourceDirectory);
        final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "py").toFile();

        try {
            FileUtils.copyDirectory(sourceDirectoryFile, buildDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy source", e);
        }

        final File setup = Paths.get(buildDirectory.getPath(), "setup.py").toFile();
        final boolean setupProvided = setup.exists();

        final File setupTemplate = setupProvided ? setup : Paths.get(buildDirectory.getPath(), "setup-template.py").toFile();

        try {
            if (!setupProvided) {
                //update VERSION to latest version
                List<String> lines = new ArrayList<String>();
                final InputStream inputStream = new BufferedInputStream(new FileInputStream(setupTemplate));
                try {
                    lines.addAll(IOUtils.readLines(inputStream));
                } finally {
                    inputStream.close();
                }

                int index = 0;
                for (String line : lines) {
                    line = line.replace(VERSION, packageVersion);
                    line = line.replace(PROJECT_NAME, packageName);
                    lines.set(index, line);
                    index++;
                }

                final OutputStream outputStream = new FileOutputStream(setup);
                try {
                    IOUtils.writeLines(lines, "\n", outputStream);
                } finally {
                    outputStream.flush();
                    outputStream.close();
                }
            }

            //execute setup script
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, setup.getCanonicalPath(), "bdist_egg");
            processBuilder.directory(buildDirectory);
            processBuilder.redirectErrorStream(true);

            Process pr = processBuilder.start();
            int exitCode = pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                getLog().info(line);
            }

            if (exitCode != 0) {
                throw new MojoExecutionException("python setup.py returned error code " + exitCode);
            }

        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to find " + setup.getPath(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read " + setup.getPath(), e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Unable to execute python " + setup.getPath(), e);
        }


    }
}

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Packages a Python module using distribute
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="python"
     * @required
     */
    private String pythonExecutable;

    /**
     * @parameter default-value="egg"
     * @required
     */
    private String distributionType;

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "maven-python").toFile();
        final String setupOutputCanonicalPath = project.getProperties().getProperty("python.distribute.plugin.setup.path");

        try {
            String bdistName;
            switch(distributionType) {
                case "egg":
                    bdistName = "bdist_egg";
                    break;
                case "wheel":
                    bdistName = "bdist_wheel";
                    break;
                default:
                    throw new MojoExecutionException("invalid distributionType (egg or wheel supported): " + distributionType);
            }

            //execute setup script
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, setupOutputCanonicalPath, bdistName);
            processBuilder.directory(buildDirectory);

            Process pr = processBuilder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            while (!pr.waitFor(100, TimeUnit.MILLISECONDS)) {
                stdout.lines().forEachOrdered(line->getLog().info(line));
                stderr.lines().forEachOrdered(line->getLog().error(line));
            }

            stdout.lines().forEachOrdered(line->getLog().debug(line));
            stderr.lines().forEachOrdered(line->getLog().warn(line));

            int exitCode = pr.exitValue();
            if (exitCode != 0) {
                throw new MojoExecutionException("python setup.py returned error code " + exitCode);
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to find " + setupOutputCanonicalPath, e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read " + setupOutputCanonicalPath, e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Unable to execute python " + setupOutputCanonicalPath, e);
        }
    }
}
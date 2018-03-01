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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class to run a setup.py command
 */
public abstract class AbstractSetupCommandMojo extends AbstractMojo {
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
     * Implementations should add any commands and arguments they wish to pass to setup.py here.
     *
     * @param args List to add setup.py commands to.
     * @throws MojoExecutionException
     */
    abstract void addSetupArgs(List<String> args) throws MojoExecutionException;

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException {
        final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "maven-python").toFile();
        final String setupOutputCanonicalPath = project.getProperties().getProperty("python.distribute.plugin.setup.path", "src/main/python/setup.py");

        try {
            List<String> args = new ArrayList<>();
            args.add(pythonExecutable);
            args.add(setupOutputCanonicalPath);
            addSetupArgs(args);
            //execute setup script
            ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[args.size()]));
            processBuilder.directory(buildDirectory);

            Process pr = processBuilder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            while (!pr.waitFor(100, TimeUnit.MILLISECONDS)) {
                stdout.lines().forEachOrdered(line -> getLog().debug(line));
                stderr.lines().forEachOrdered(this::logErrorOrWarning);
            }

            stdout.lines().forEachOrdered(line -> getLog().debug(line));
            stderr.lines().forEachOrdered(this::logErrorOrWarning);

            int exitCode = pr.exitValue();
            if (exitCode != 0) {
                throw new MojoExecutionException("'" + String.join(" ", processBuilder.command()) + "' returned error code " + exitCode);
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Unable to find " + setupOutputCanonicalPath, e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read " + setupOutputCanonicalPath, e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Unable to execute python " + setupOutputCanonicalPath, e);
        }
    }

    private void logErrorOrWarning(String line) {
        if (line.toLowerCase().contains("error"))
            getLog().error(line);
        else
            getLog().warn(line);
    }
}

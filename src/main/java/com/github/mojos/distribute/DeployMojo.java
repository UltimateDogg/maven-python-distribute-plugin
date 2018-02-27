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

import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uploads a Python module to PyPI using distribute
 *
 * @goal deploy
 * @phase deploy
 */
public class DeployMojo extends AbstractSetupCommandMojo {
    /**
     * @parameter
     */
    private String repository;

    /**
     * @parameter
     */
    private String distributionType;

    static List<String> builtDistributionTypes = Collections.synchronizedList(new ArrayList<>());

    @Override
    void addSetupArgs(List<String> args) throws MojoExecutionException {
        if (distributionType != null) {
            args.add(PackageMojo.getDistributionTypeArg(distributionType));
        } else {
            args.addAll(builtDistributionTypes);
        }
        args.add("upload");
        if (repository != null) {
            args.add("-r");
            args.add(repository);
        }
    }
}

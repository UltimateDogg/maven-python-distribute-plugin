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

import java.util.List;

/**
 * Packages a Python module using distribute
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractSetupCommandMojo {
    /**
     * @parameter default-value="egg"
     * @required
     */
    private String distributionType;

    static String getDistributionTypeArg(String distributionType) throws MojoExecutionException {
        switch (distributionType) {
            case "egg":
            case "wheel":
            case "wininst":
            case "rpm":
            case "dumb":
                return "bdist_" + distributionType;
            case "bdist":
                return "bdist";
            case "source":
                return "sdist";
            case "docs":
                return "build_sphinx";
            default:
                throw new MojoExecutionException("Invalid distributionType (egg, wheel, wininst, rpm, bdist, dumb, source, or docs supported): " + distributionType);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    @Override
    public void addSetupArgs(List<String> args) throws MojoExecutionException {
        String distributionTypeArg = getDistributionTypeArg(distributionType);
        args.add(distributionTypeArg);
        DeployMojo.builtDistributionTypes.add(distributionTypeArg);
    }
}
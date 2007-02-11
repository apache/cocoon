/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
package org.apache.maven.plugin.eclipse.writers;

import java.io.File;

import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Common base class for all Eclipse Writers.
 * 
 * @author <a href="mailto:rahul.thakur.xdev@gmail.com">Rahul Thakur</a>
 * @version $Id$
 */
public abstract class AbstractEclipseResourceWriter
{

    private Log log;

    private File eclipseProjectDir;

    private MavenProject project;

    protected IdeDependency[] deps;

    /**
     * @param log
     * @param eclipseProjectDir
     * @param project
     */
    public AbstractEclipseResourceWriter( Log log, File eclipseProjectDir, MavenProject project, IdeDependency[] deps )
    {
        this.log = log;
        this.eclipseProjectDir = eclipseProjectDir;
        this.project = project;
        this.deps = deps;
    }

    /**
     * @return the eclipseProjectDir
     */
    public File getEclipseProjectDirectory()
    {
        return eclipseProjectDir;
    }

    /**
     * @return the log
     */
    public Log getLog()
    {
        return log;
    }

    /**
     * @return the project
     */
    public MavenProject getProject()
    {
        return project;
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.jcr;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.FileSource;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.xml.sax.InputSource;

/**
 * JackrabbitRepository is a JCR repository component based on <a
 * href="http://incubator.apache.org/jackrabbit">Jackrabbit</a>
 *
 * <p>
 * The configuration is as follows:
 *
 * <pre>
 *    &lt;jcr-repository&gt;
 *      &lt;credentials login="<i>expression</i>" password="<i>expression</i>"/&gt;
 *      &lt;home src="file://path/to/repository"/&gt;
 *      &lt;configuration src="resource://your/application/jcr/repository.xml"/&gt;
 *    &lt;/jcr-repository&gt;
 * </pre>
 *
 * The <code>home</code> URI points to the base location of the repository,
 * and <code>configuration</code> points to the Jackrabbit repository
 * configuration file.
 *
 * @see AbstractRepository
 * @version $Id$
 */
public class JackrabbitRepository extends AbstractRepository {

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        // Java VM must be at least 1.4
        if (SystemUtils.isJavaVersionAtLeast(140) == false) {
            String message = "The jcr block needs at least a java VM version 1.4 to run properly. Please update to a newer java or exclude the jcr block from your Cocoon block configuration."; 
            getLogger().error(message);
            throw new ConfigurationException(message);
        }

        String homeURI = config.getChild("home").getAttribute("src");
        String homePath;
        String configURI = config.getChild("configuration").getAttribute("src");

        // having to release sources is a major PITA...
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);

            // Ensure home uri is a file and absolutize it
            Source homeSrc = resolver.resolveURI(homeURI);
            try {
                if (!(homeSrc instanceof FileSource)) {
                    throw new ConfigurationException("Home path '" + homeURI + "' should map to a file, at " +
                                                     config.getChild("home").getLocation());
                }
                homePath = ((FileSource) homeSrc).getFile().getAbsolutePath();
            } finally {
                resolver.release(homeSrc);
            }

            // Load repository configuration
            Source configSrc = resolver.resolveURI(configURI);
            RepositoryConfig repoConfig;
            try {
                InputSource is = SourceUtil.getInputSource(configSrc);
                repoConfig = RepositoryConfig.create(is, homePath);
            } finally {
                resolver.release(configSrc);
            }
            // And create the repository
            this.delegate = RepositoryImpl.create(repoConfig);

        } catch (ConfigurationException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ConfigurationException("Cannot access configuration information at " + config.getLocation(), e);
        } finally {
            this.manager.release(resolver);
        }
    }
    
    public void dispose() {
        // Shutdown the repository to release the concurrent access lock
        RepositoryImpl repo = (RepositoryImpl)delegate;
        super.dispose();
        repo.shutdown();
    }
}

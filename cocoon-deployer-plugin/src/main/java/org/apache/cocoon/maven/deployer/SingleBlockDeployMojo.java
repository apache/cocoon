/*
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.maven.deployer;

import org.apache.cocoon.deployer.BlockDeployer;
import org.apache.cocoon.deployer.generated.deploy.x10.Block;
import org.apache.cocoon.deployer.generated.deploy.x10.Cocoon;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.generated.deploy.x10.Mount;
import org.apache.cocoon.deployer.resolver.NullVariableResolver;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.project.artifact.MavenMetadataSource;

import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @goal test-deploy
 * @phase package
 * @requiresProject true
 */
public class SingleBlockDeployMojo
    extends AbstractMojo {
    //~ Instance fields ---------------------------------------------------------------------------------

    /**
     * Artifact factory, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * Local maven repository.
     *
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * Remote repositories which will be searched for blocks.
     *
     * @required
     * @readonly
     */
    private List remoteArtifactRepositories;

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     * @required
     * @readonly
     */
    private MavenMetadataSource metadataSource;

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws MojoExecutionException DOCUMENT ME!
     */
    public void execute()
        throws MojoExecutionException {
        getLog().info("Start deployment of ...");

        Cocoon cocoon = new Cocoon();
        cocoon.setExclusive(true);
        cocoon.setUrl("target/webapp/xyz");
        cocoon.setVersion("2.2");

        Block block = new Block();
        block.setAutoWire(true);
        block.setLocation("blah");

        Mount mount = new Mount();
        mount.setPath("xyz");
        block.setMount(mount);
        block.setUrn("blah");

        Deploy deploy = new Deploy();
        deploy.setCocoon(cocoon);
        deploy.addBlock(block);

        BlockDeployer blockDeployer =
            new BlockDeployer(new MavenArtifactProvider(this.artifactResolver, this.artifactFactory,
                                                        this.localRepository,
                                                        this.remoteArtifactRepositories,
                                                        this.metadataSource, this.getLog()),
                              new NullVariableResolver(), new MavenLoggingWrapper(this.getLog()));
        blockDeployer.deploy(deploy);
    }
}

/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.maven.deployer;

import java.io.File;
import java.io.IOException;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.war.WarArchiver;

/**
 * Create a packaged Cocoon web application (.war file) based on a block
 * deployment descriptor.
 * 
 * @goal deploy-war
 * @requiresProject true
 * @phase package
 * @description Create a packaged Cocoon web application (.war file) based on a
 *              block deployment descriptor.
 */
public class DeployWarMojo extends AbstractDeployMojo {

	/**
	 * The directory for the generated WAR.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;

	/**
	 * The name of the generated war.
	 * 
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 */
	private String warName;

	/**
	 * The Jar archiver.
	 * 
	 * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
	 * @required
	 */
	private WarArchiver warArchiver;

	/**
	 * The maven archive configuration to use.
	 * 
	 * @parameter
	 */
	private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

	public void execute() throws MojoExecutionException {

		File warFile = new File(outputDirectory, warName + ".war");
		this.deployBlocks();

		try {
			performPackaging(warFile);
		} catch (Exception e) {
			// TODO: improve error handling
			throw new MojoExecutionException("Error assembling WAR", e);
		}

	}

	/**
	 * Generates the webapp according to the <tt>mode</tt> attribute.
	 * 
	 * @param warFile
	 *            the target war file
	 * @throws IOException
	 * @throws ArchiverException
	 * @throws ManifestException
	 * @throws DependencyResolutionRequiredException
	 */
	private void performPackaging(File warFile) throws IOException,
			ArchiverException, ManifestException,
			DependencyResolutionRequiredException, MojoExecutionException {
		getLog().info("Generating war " + warFile.getAbsolutePath());

		MavenArchiver archiver = new MavenArchiver();

		archiver.setArchiver(warArchiver);

		archiver.setOutputFile(warFile);

		warArchiver.addDirectory(getWebappDirectory(), getIncludes(),
				getExcludes());

		warArchiver
				.setWebxml(new File(getWebappDirectory(), "WEB-INF/web.xml"));

		// create archive
		archiver.createArchive(getProject(), archive);

		getProject().getArtifact().setFile(warFile);
	}

}
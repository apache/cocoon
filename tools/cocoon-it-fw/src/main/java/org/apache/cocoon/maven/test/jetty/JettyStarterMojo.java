package org.apache.cocoon.maven.test.jetty;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Start all servers to run the Cocoon integration tests.
 *
 * @goal jetty-start
 */
public class JettyStarterMojo extends AbstractMojo {

    /**
     * The absolute path to the web application under test.
     *
     * @parameter
     * @required
     */
    private File webAppDirectory;

    /**
     * @parameter
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (this.skip) {
            this.getLog().info("Skip starting server environment.");
            return;
        }
        try {
            new JettyContainer().start("/", webAppDirectory.getAbsolutePath(), 8888);
        } catch (Exception e) {
            throw new MojoExecutionException("Can't start Jetty.", e);
        }
    }


}

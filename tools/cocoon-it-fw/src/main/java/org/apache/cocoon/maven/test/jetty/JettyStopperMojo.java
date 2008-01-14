package org.apache.cocoon.maven.test.jetty;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Stop all the JettyContainer instance.
 *
 * @goal jetty-stop
 */
public class JettyStopperMojo extends AbstractMojo {

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
            new JettyContainer().stop();
        } catch (Exception e) {
            this.getLog().error("Can't stop JettyContainer. ", e);
        }
    }
}

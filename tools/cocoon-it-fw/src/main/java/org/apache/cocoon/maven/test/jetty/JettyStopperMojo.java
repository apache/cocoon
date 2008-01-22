package org.apache.cocoon.maven.test.jetty;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Stop the Jetty instance.
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
        // flush information collected by Cobertura
        try {
            String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
            String methodName = "saveGlobalProjectData";
            Class saveClass = Class.forName(className);
            java.lang.reflect.Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
            saveMethod.invoke(null, new Object[0]);
        } catch (Throwable t) {
            this.getLog().debug("Error while flushing information collected by Cobertura.");
        }
    }
}

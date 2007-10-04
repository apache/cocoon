package org.apache.cocoon.maven.docscleaner;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This plugin removes all document which are not read from Daisy, the changes page, the index
 * page or the .htaccess file.
 *
 * This is recommended to run this goal before you run site:deploy in order to avoid publishing
 * any docs that contain snapshot information.
 *
 * @goal clean
 * @requiresProject true
 * @version $Id$
 */
public class DocsCleanerMojo extends AbstractMojo {

    /**
     * Directory containing the generated project sites and report distributions.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    protected File siteOutputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("OutputDirectory: " + siteOutputDirectory);
        File[] files = this.siteOutputDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if(deleteFile(f)) {
                this.getLog().debug("[delete] " + f.getAbsolutePath());
                System.out.println("Deleting: " + f.getAbsolutePath());
                if(f.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(f);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Can't delete directory " + f.getAbsolutePath());
                    }
                } else {
                    f.delete();
                }
            }
        }
    }

    private boolean deleteFile(File file) {
        String fileName = file.getName();

        // find out if it is a Daisy page -> don't delete them
        int pos = fileName.indexOf('_');
        if (pos > 0) {
        String documentId = fileName.substring(0, fileName.indexOf('_'));
            boolean isDaisyDocument = false;
            try {
                Integer.parseInt(documentId);
                isDaisyDocument = true;
            } catch (NumberFormatException nfe) {
                isDaisyDocument = false;
            }

            if (isDaisyDocument) {
                return false;
            }
        }

        if (".htaccess".equals(fileName)) {
            return false;
        }

        // is it a status report --> don't delete it
        if ("changes-report.html".equals(fileName)) {
            return false;
        }

        // is it the css directory --> don't delete it
        if ("css".equals(fileName)) {
            return false;
        }
        // is it the images directory --> don't delete it
        if ("images".equals(fileName)) {
            return false;
        }

        // is it the index page --> don't delete it
        if ("index.html".equals(fileName)) {
            return false;
        }

        return true;
    }

}

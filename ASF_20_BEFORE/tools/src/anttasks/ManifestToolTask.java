/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * Creates Manifest file with the all the JARs and modification dates
 * in the specified directory.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Revision: 1.1 $ $Date: 2003/03/09 00:11:45 $
 */

public final class ManifestToolTask extends Task {

    private String directory;
    private String manifest;

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public void execute() throws BuildException {
        if (this.manifest == null) {
            throw new BuildException("manifest attribute is required", location);
        }
        if (this.directory == null) {
            throw new BuildException("directory attribute is required", location);
        }

        try {
            // process recursive
            this.process(this.project.resolveFile(this.directory), this.manifest);
        } catch (IOException ioe) {
            throw new BuildException("IOException: " + ioe);
        }
    }

    /**
     * Scan recursive
     */
    private void process(final File directoryFile,
                         final String manifest)
    throws IOException, BuildException {

        System.out.println("Writing: " + manifest);
        FileWriter w = new FileWriter(this.project.resolveFile(manifest));
        w.write("Manifest-Version: 1.0\n");

        if (directoryFile.exists() && directoryFile.isDirectory() ) {
            w.write("Cocoon-Libs: ");

            final File[] files = directoryFile.listFiles();
            for(int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".jar")) {
                    w.write(files[i].getName());
                    w.write(" ");
                }
            }
            w.write("\n");

            for(int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".jar")) {
                    w.write("Cocoon-Lib-");
                    String s = files[i].getName().replace('.', '_');
                    w.write(s);
                    w.write(": ");
                    w.write(String.valueOf(files[i].lastModified()));
                    w.write("\n");
                }
            }

        }
        w.close();
    }
}

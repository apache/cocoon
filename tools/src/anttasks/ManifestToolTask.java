/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Creates Manifest file with the all the JARs and modification dates
 * in the specified directory.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/03/10 09:08:25 $
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

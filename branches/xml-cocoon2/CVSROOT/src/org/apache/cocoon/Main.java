/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;


/**
 * Command line entry point.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.4.1 $ $Date: 2000-08-23 22:44:26 $
 */

public class Main {

    public static void main(String[] args) throws Exception {

        String destDir  = Cocoon.DEFAULT_DEST_DIR;
        String confFile = Cocoon.DEFAULT_CONF_FILE;
        List targets = new ArrayList();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-h") || arg.equals("--help")) {
                printUsage();
                return;
            } else if (arg.equals("-v") || arg.equals("--version")) {
                printVersion();
                return;
            } else if (arg.equals("-d") || arg.equals("--destdir")) {
                try {
                    destDir = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("You must specify a destination dir when " +
                        "using the -d/--destdir argument");
                    return;
                }
            } else if (arg.equals("-c") || arg.equals("--conf")) {
                try {
                    confFile = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("You must specify a configuration file when " +
                        "using the -c/--conf argument");
                    return;
                }
            } else if (arg.equals("-l") || arg.equals("--logfile")) {
                try {
                    String logFile = args[i + 1];
                    i++;
                    PrintStream out = new PrintStream(new FileOutputStream(logFile));
                    System.setOut(out);
                    System.setErr(out);
                } catch (IOException e) {
                    System.out.println("Cannot write on the specified log file. " +
                        "Make sure the path exists and you have write permissions.");
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("You must specify a log file when " +
                        "using the -l/--logfile argument");
                    return;
                }
            } else if (arg.startsWith("-")) {
                // we don't have any more args to recognize!
                System.out.println("[fatal error] Unknown argument: " + arg + "\n");
                printUsage();
                return;
            } else {
                // if it's no other arg, it must be the target
                targets.add(arg);
            }

        }

        try {
            Main main = new Main(new Cocoon(getConfigurationFile(confFile)), getDestinationDir(destDir));
            main.processLinks(targets.iterator());
        } catch (Exception e) {
            System.out.println("[fatal error] Exception caught (" + e.getClass().getName() + "): " + e.getMessage() + "\n");
            printUsage();
        }
    }

    /**
     * Prints the usage of how to use this class to System.out
     */
    private static void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("------------------------------------------------------------------------ " + lSep);
        msg.append(Cocoon.NAME + " " + Cocoon.VERSION + lSep);
        msg.append("Copyright (c) " + Cocoon.YEAR + " Apache Software Foundation. All rights reserved." + lSep);
        msg.append("------------------------------------------------------------------------ " + lSep + lSep);
        msg.append("Usage: java org.apache.cocoon.Main [options] [targets]" + lSep + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -h/--help              print this message and exit" + lSep);
        msg.append("  -v/--version           print the version information and exit" + lSep);
        msg.append("  -l/--logfile <file>    use given file for log" + lSep);
        msg.append("  -c/--conf    <file>    use given file as configurations" + lSep);
        msg.append("  -d/--destdir <dir>     use given dir as destination" + lSep + lSep);
        msg.append("Note: if the configuration file is not specified, it will default to" + lSep);
        msg.append("'" + Cocoon.DEFAULT_CONF_FILE + "' in the current working directory, then in the user directory" + lSep);
        msg.append("and finally in the '/usr/local/etc/' directory before giving up." + lSep);
        System.out.println(msg.toString());
    }
    
    private static void printVersion() {
        System.out.println(Cocoon.VERSION);
    }

    private static File getConfigurationFile(String file) throws Exception {

        File f;

        // look for the indicated file
        if (file != null) {
            f = new File(file);
            if (f.canRead()) return f;
        }

        // look in the current working directory
        f = new File(Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        // then in the user directory
        f = new File(System.getProperty("user.dir") + File.separator + Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        // finally in the /usr/local/etc/ directory (for Unix systems).
        f = new File("/usr/local/etc/" + Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        throw new FileNotFoundException("The configuration file could not be found.");
    }

    private static File getDestinationDir(String dir) throws Exception {

        File d = new File(dir);

        if (!d.exists()) {
            if (!d.mkdirs()) {
                throw new IOException("Error creating destination directory '" + d + "'");
            }
        }

        if (!d.isDirectory()) {
            throw new IOException("'" + d + "' is not a directory.");
        }

        if (!(d.canRead() && d.canWrite())) {
            throw new IOException("Directory '" + d + "' is not readable/writable");
        }

        return d;
    }

    private Cocoon cocoon;
    private File destDir;

    /**
     * Creates the Main class
     */
    public Main(Cocoon cocoon, File destDir) {
        this.cocoon = cocoon;
        this.destDir = destDir;
    }

    /**
     * Process the given link and return the list of sublinks.
     */
    public Iterator processLink(String link) throws Exception {
System.out.println("[main] processing link: " + link);        
        // First process the given link and save it on disk
        FileSavingEnvironment fileEnv = new FileSavingEnvironment(link, destDir);
        cocoon.process(fileEnv);
        // Then process it again (with another view) to obtain the hyperlinks
        LinkSamplingEnvironment linkEnv = new LinkSamplingEnvironment(link);
        cocoon.process(linkEnv);
        return linkEnv.getLinks();
    }

    /**
     * Process the link list and recursively process them all.
     */
    public void processLinks(Iterator links) throws Exception {
        while (links.hasNext()) {
            String link = (String) links.next();
            this.processLinks(this.processLink(link));
        }
    }
}


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
import java.io.OutputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;


/**
 * Command line entry point.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.4.6 $ $Date: 2000-09-22 12:21:08 $
 */

public class Main {

    public static void main(String[] args) throws Exception {

        String destDir  = Cocoon.DEFAULT_DEST_DIR;
        String confFile = Cocoon.DEFAULT_CONF_FILE;
        String workDir  = Cocoon.DEFAULT_WORK_DIR;
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
            } else if (arg.equals("-w") || arg.equals("--workdir")) {
                try {
                    workDir = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("You must specify a destination dir when " +
                        "using the -w/--workdir argument");
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
            File dest = getDir(destDir, "destination");
            File work = getDir(workDir, "working");
            File conf = getConfigurationFile(confFile);
            File root = conf.getParentFile();
            Main main = new Main(new Cocoon(conf, null, work), conf, dest);
            System.out.println("[main] Starting...");
            main.process(targets);
            System.out.println("[main] Done.");
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
        msg.append("  -w/--workdir <dir>     use given dir as working directory" + lSep + lSep);
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

    private static File getDir(String dir, String type) throws Exception {

        File d = new File(dir);

        if (!d.exists()) {
            if (!d.mkdirs()) {
                throw new IOException("Error creating " + type + " directory '" + d + "'");
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

    // -----------------------------------------------------------------------
    
    private Cocoon cocoon;
    private File destDir;
    private File root;

    /**
     * Creates the Main class
     */
    public Main(Cocoon cocoon, File root, File destDir) {
        this.cocoon = cocoon;
        this.root = root;
        this.destDir = destDir;
    }

    /**
     * Process the URI list and process them all independently.
     */
    public void process(Collection uris) throws Exception {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            String uri = (String) i.next();
            System.out.println("[main] starting from: " + uri);
            this.processURI(uri);
        }
    }

    /**
     * Processes the given URI in a recursive way. The algorithm followed by
     * this method is the following:
     *
     * <ul>
     *  <li>the link view of the given URI is called and the resources linked
     *      to the requested one are obtained.</li>
     *  <li>for each link, this method is recursively called and returns
     *      the file used to save the resource on disk.</li>
     *  <li>after the complete list of links is translated, the link-translating
     *      view of the resource is called to obtain a link-translated version
     *      of the resource with the given link map</li>
     *  <li>the resource is saved on disk and the URI MIME type is checked for 
     *      consistency with the URI and, if the extention is inconsistent
     *      or absent, the file is renamed</li>
     *  <li>then the file name of the translated URI is returned</li>
     * </ul>
     */
    public File processURI(String uri) throws Exception {
        System.out.println("[main] processing: " + uri);
        
        Collection links = getLinks(uri);
        Map translatedLinks = new HashMap(links.size());
        Iterator i = links.iterator();
        while (i.hasNext()) {
            String link = (String) i.next();
            translatedLinks.put(link, processURI(link));
        }
        
        File outputFile = getFile(uri);
        FileOutputStream output = new FileOutputStream(getFile(uri));
        String type = getPage(uri, translatedLinks, output);
        output.close();
        
        if (!matchesExtension(uri, type)) {
            outputFile.renameTo(getFile(uri, type));
        }
        
        return outputFile;
    }        
    
    Collection getLinks(String uri) throws Exception {
        LinkSamplingEnvironment env = new LinkSamplingEnvironment(uri);
        cocoon.process(env);
        return env.getLinks();
    }

    String getPage(String uri, Map links, OutputStream stream) throws Exception {
        FileSavingEnvironment env = new FileSavingEnvironment(uri, root, links, stream);
        cocoon.process(env);
        return env.getContentType();
    }
    
    File getFile(String uri) {
        return new File(destDir, uri);
    }
    
    File getFile(String uri, String type) {
        return new File(destDir, uri + File.separator + getExtension(type));
    }
    
    boolean matchesExtension(String uri, String type) {
        int dotindex = uri.lastIndexOf('.');
        int slashindex = uri.indexOf('/', dotindex);
        if ((dotindex != -1) && (slashindex == -1)) {
            String ext = uri.substring(dotindex);
            return type.equals(getExtension(type));
        }
        return false;
    }
    
    String getExtension(String type) {
        if ("text/html".equals(type)) {
            return "html";
        } else if ("text/xml".equals(type)) {
            return "xml";
        } else if ("text/css".equals(type)) {
            return "css";
        } else if ("text/vnd.wap.wml".equals(type)) {
            return "wml";
        } else if ("image/jpg".equals(type)) {
            return "jpg";
        } else if ("image/png".equals(type)) {
            return "png";
        } else if ("image/gif".equals(type)) {
            return "gif";
        } else if ("image/svg-xml".equals(type)) {
            return "svg";
        } else if ("application/pdf".equals(type)) {
            return "pdf";
        } else if ("model/vrml".equals(type)) {
            return "wrl";
        } else if ("text/plain".equals(type)) {
            return "txt";
        } else if ("application/rtf".equals(type)) {
            return "rtf";
        } else if ("text/rtf".equals(type)) {
            return "rtf";
        } else if ("application/smil".equals(type)) {
            return "smil";
        } else if ("application/x-javascript".equals(type)) {
            return "js";
        } else if ("application/zip".equals(type)) {
            return "zip";
        } else if ("video/mpeg".equals(type)) {
            return "mpg";
        } else if ("video/quicktime".equals(type)) {
            return "mov";
        } else if ("audio/midi".equals(type)) {
            return "mid";
        } else if ("audio/mpeg".equals(type)) {
            return "mp3";
        } else {
            return "xxx";
        }
    }
}


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
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.JavaArchiveFilter;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;


/**
 * Command line entry point.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.4.11 $ $Date: 2000-10-02 11:07:26 $
 */

public class Main {

    public static void main(String[] args) throws Exception {

        String destDir = Cocoon.DEFAULT_DEST_DIR;
        String contextDir = Cocoon.DEFAULT_CONTEXT_DIR;
        String workDir = Cocoon.DEFAULT_WORK_DIR;
        List targets = new ArrayList();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-h") || arg.equals("--help")) {
                printUsage();
            } else if (arg.equals("-v") || arg.equals("--version")) {
                printVersion();
            } else if (arg.equals("-d") || arg.equals("--destDir")) {
                try {
                    destDir = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    error("Careful, you must specify a destination dir when " +
                        "using the -d/--destDir argument");
                }
            } else if (arg.equals("-w") || arg.equals("--workDir")) {
                try {
                    workDir = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    error("Careful, you must specify a destination dir when " +
                        "using the -w/--workDir argument");
                }
            } else if (arg.equals("-c") || arg.equals("--contextDir")) {
                try {
                    contextDir = args[i + 1];
                    i++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    error("Careful, you must specify a configuration file when " +
                        "using the -c/--contextDir argument");
                }
            } else if (arg.equals("-l") || arg.equals("--logFile")) {
                try {
                    String logFile = args[i + 1];
                    i++;
                    PrintStream out = new PrintStream(new FileOutputStream(logFile));
                    System.setOut(out);
                    System.setErr(out);
                } catch (IOException e) {
                    error("Cannot write on the specified log file. " +
                        "Please, make sure the path exists and you have write permissions.");
                } catch (ArrayIndexOutOfBoundsException e) {
                    error("Careful, you must specify a log file when " +
                        "using the -l/--logFile argument");
                }
            } else if (arg.startsWith("-")) {
                // we don't have any more args to recognize!
                error("Sorry, cannot recognize the argument: " + arg + "\n");
            } else {
                // if it's no other arg, it must be the starting URI
                targets.add(arg);
            }

        }

        if (targets.size() == 0) {
            error("Please, specify at least one starting URI.");
        }
        
        try {
            File dest = getDir(destDir, "destination");
            File work = getDir(workDir, "working");
            File context = getDir(contextDir, "context");
            File conf = getConfigurationFile(context);
            Main main = new Main(new Cocoon(conf, null, work.toString()), context, dest);
            log("Warming up...");
            log(" [Cocoon is compiling the sitemap, this might take a while]");
            main.warmup();
            log("...ready, let's go:");
            main.process(targets);
            log("Done");
        } catch (Exception e) {
            error("Exception caught (" + e.getClass().getName() + "): " + e.getMessage() + "\n");
            e.printStackTrace(System.err);
        }
    }

    private static void printVersion() {
        System.out.println(Cocoon.VERSION);
        System.exit(0);
    }

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
        msg.append("  -l/--logFile <file>    use given file for log" + lSep);
        msg.append("  -c/--contextDir <file> use given dir as context" + lSep);
        msg.append("  -d/--destDir <dir>     use given dir as destination" + lSep);
        msg.append("  -w/--workDir <dir>     use given dir as working directory" + lSep + lSep);
        msg.append("Note: the context directory defaults to '" + Cocoon.DEFAULT_CONTEXT_DIR + "'" + lSep);
        System.out.println(msg.toString());
        System.exit(1);
    }

    public static void log(String msg) {
        System.out.println("[log] " + msg);
    }

    public static void warning(String msg) {
        System.out.println("[warning] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[error] " + msg);
        System.exit(1);
    }
    
    private static File getConfigurationFile(File dir) throws Exception {

        File f = new File(dir, Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;
        
        f = new File(System.getProperty("user.dir") + File.separator + Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

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
    private File context;
    private Map attributes;
    private Map parameters;

    /**
     * Creates the Main class
     */
    public Main(Cocoon cocoon, File context, File destDir) {
        this.cocoon = cocoon;
        this.context = context;
        this.destDir = destDir;
        this.attributes = new HashMap();
        this.parameters = new HashMap();
    }

    /**
     * Warms up the engine by accessing the root.
     */
    public void warmup() throws Exception {
        cocoon.process(new LinkSamplingEnvironment("/", context, attributes, parameters));
    }

    /**
     * Process the URI list and process them all independently.
     */
    public void process(Collection uris) throws Exception {
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            String uri = (String) i.next();
            this.processURI(uri, uri, 0);
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
    public String processURI(String uri, String origUri, int level) throws Exception {
        log(leaf(level) + uri);

        Collection links = this.getLinks(uri);
        Map translatedLinks = new HashMap(links.size());
        Iterator i = links.iterator();
        while (i.hasNext()) {
            log(tree(level));
            String origLink = (String) i.next();
            String link = NetUtils.normalizeURI(NetUtils.adjustContext(uri, origLink));
            translatedLinks.put(link, this.processURI(link, origLink, level + 1));
        }
        
        String filename = mangle(uri);
        File file = IOUtils.createFile(destDir, filename);
        FileOutputStream output = new FileOutputStream(file);
        String type = getPage(uri, translatedLinks, output);
        output.close();

        String ext = NetUtils.getExtension(uri);
        String defaultExt = MIMEUtils.getDefaultExtension(type);
        
        if ((ext == null) || (!ext.equals(defaultExt))) {
            filename += defaultExt;
            origUri += defaultExt;
            File newFile = IOUtils.createFile(destDir, filename);
            file.renameTo(newFile);
        }

        log(tree(level));

        if (type == null) {
            log(leaf(level + 1) + "[broken link]--> " + filename);
            resourceUnavailable(file);
        } else {
            log(leaf(level + 1) + "[" + type + "]--> " + filename);
        }
        
        return mangle(origUri);
    }        

    void resourceUnavailable(File file) throws IOException {
        PrintStream out = new PrintStream(new FileOutputStream(file));
        out.println(
           "<html><head><title>Page Not Available</title></head>" +
           "<body><h1 align=\"center\">Page Not Available</h1>" +
           "<body><p align=\"center\">Generated by " + 
           Cocoon.COMPLETE_NAME +
           "</p></body></html>"
        );
        out.close();        
    }
    
    String mangle(String uri) {
        uri = uri.replace('"', '\'');
        return uri.replace('?','_');
    }
    
    String leaf(int level) {
        if (level == 0) return "";
        return tree(level - 2) + "+--";
    }

    String tree(int level) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= level; i++) {
            buffer.append("|  ");
        }
        return buffer.toString();
    }
    
    Collection getLinks(String uri) throws Exception {
        LinkSamplingEnvironment env = new LinkSamplingEnvironment(uri, context, attributes, parameters);
        cocoon.process(env);
        return env.getLinks();
    }

    String getPage(String uri, Map links, OutputStream stream) throws Exception {
        FileSavingEnvironment env = new FileSavingEnvironment(uri, context, attributes, parameters, links, stream);
        cocoon.process(env);
        return env.getContentType();
    }
}


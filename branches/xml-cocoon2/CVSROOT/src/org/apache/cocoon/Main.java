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

import java.net.MalformedURLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.avalon.util.cli.CLArgsParser;
import org.apache.avalon.util.cli.CLOption;
import org.apache.avalon.util.cli.CLOptionDescriptor;
import org.apache.avalon.util.cli.CLUtil;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.JavaArchiveFilter;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;

import org.apache.log.Logger;
import org.apache.log.LogKit;
import org.apache.log.Priority;
import org.apache.log.Category;
import org.apache.log.output.FileOutputLogTarget;
import org.apache.log.LogTarget;

/**
 * Command line entry point.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.4.16 $ $Date: 2000-12-07 17:10:30 $
 */

public class Main {

    protected static Logger log = null;

    protected static final int HELP_OPT =        'h';
    protected static final int VERSION_OPT =     'v';
    protected static final int LOG_URL_OPT =     'l';
    protected static final int LOG_LEVEL_OPT =   'u';
    protected static final int CONTEXT_DIR_OPT = 'c';
    protected static final int DEST_DIR_OPT =    'd';
    protected static final int WORK_DIR_OPT =    'w';

    protected static final CLOptionDescriptor [] options = new CLOptionDescriptor [] {
        new CLOptionDescriptor("help",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               HELP_OPT,
                               "print this message and exit"),
        new CLOptionDescriptor("version",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               VERSION_OPT,
                               "print the version information and exit"),
        new CLOptionDescriptor("logUrl",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               LOG_URL_OPT,
                               "use given file for log"),
        new CLOptionDescriptor("logLevel",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               LOG_LEVEL_OPT,
                               "choose the minimum log level for logging (DEBUG, INFO, WARN, ERROR, FATAL_ERROR)"),
        new CLOptionDescriptor("contextDir",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               CONTEXT_DIR_OPT,
                               "use given dir as context"),
        new CLOptionDescriptor("destDir",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               DEST_DIR_OPT,
                               "use given dir as destination"),
        new CLOptionDescriptor("workDir",
                               CLOptionDescriptor.ARGUMENT_OPTIONAL,
                               WORK_DIR_OPT,
                               "use given dir as working directory")
    };


    public static void main(String[] args) throws Exception {

        String destDir = Cocoon.DEFAULT_DEST_DIR;
        String contextDir = Cocoon.DEFAULT_CONTEXT_DIR;
        String workDir = Cocoon.DEFAULT_WORK_DIR;
        List targets = new ArrayList();
        CLArgsParser parser = new CLArgsParser(args, options);
        String logUrl = "logs/cocoon.log";
        String logLevel = "DEBUG";

        List clOptions = parser.getArguments();
        int size = clOptions.size();

        for (int i = 0; i < size; i++) {
            CLOption option = (CLOption) clOptions.get(i);

            switch (option.getId()) {
                case 0:
                    targets.add(option.getArgument());
                    break;

                case Main.HELP_OPT:
                    printUsage();
                    break;

                case Main.VERSION_OPT:
                    printVersion();
                    break;

                case Main.DEST_DIR_OPT:
                    destDir = option.getArgument();
                    break;

                case Main.WORK_DIR_OPT:
                    workDir = option.getArgument();
                    break;

                case Main.CONTEXT_DIR_OPT:
                    contextDir = option.getArgument();
                    break;

                case Main.LOG_URL_OPT:
                    logUrl = option.getArgument();
                    break;

                case Main.LOG_LEVEL_OPT:
                    logLevel = option.getArgument();
                    break;
            }
        }

        try {
            LogKit.setGlobalPriority(LogKit.getPriorityForName(logLevel));
            Category cocoonCategory = LogKit.createCategory("cocoon", LogKit.getPriorityForName(logLevel));

            log = LogKit.createLogger(cocoonCategory,new LogTarget[] {new FileOutputLogTarget(logUrl)});
        } catch (MalformedURLException mue) {
            String error = "Cannot write on the specified log file.  Please, make sure the path exists and you have write permissions.";
            LogKit.log(error, mue);
            System.out.println(error);
            System.exit(1);
        }

        if (destDir.equals("")) {
            String error = "Careful, you must specify a destination dir when using the -d/--destDir argument";
            log.fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (contextDir.equals("")) {
            String error = "Careful, you must specify a configuration file when using the -c/--contextDir argument";
            log.error(error);
            System.out.println(error);
            System.exit(1);
        }

        if (workDir.equals("")) {
            String error = "Careful, you must specify a destination dir when using the -w/--workDir argument";
            log.error(error);
            System.out.println(error);
            System.exit(1);
        }

        if (targets.size() == 0) {
            String error = "Please, specify at least one starting URI.";
            log.error(error);
            System.out.println(error);
            System.exit(1);
        }

        try {
            File dest = getDir(destDir, "destination");
            File work = getDir(workDir, "working");
            File context = getDir(contextDir, "context");
            File conf = getConfigurationFile(context);
            Main main = new Main(new Cocoon(conf.toURL(), null, work, context.getCanonicalPath()), context, dest);
            main.warmup();
            main.process(targets);
            log.info("Done");
        } catch (Exception e) {
            log.fatalError("Exception caught ", e);
            System.exit(1);
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
        msg.append(CLUtil.describeOptions(Main.options).toString());
        msg.append("Note: the context directory defaults to '" + Cocoon.DEFAULT_CONTEXT_DIR + "'" + lSep);
        System.out.println(msg.toString());
        System.exit(0);
    }

    private static File getConfigurationFile(File dir) throws Exception {

        log.debug("Trying configuration file at: " + Cocoon.DEFAULT_CONF_FILE);
        File f = new File(dir, Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        log.debug("Trying configuration file at: " + System.getProperty("user.dir") + File.separator + Cocoon.DEFAULT_CONF_FILE);
        f = new File(System.getProperty("user.dir") + File.separator + Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        log.debug("Trying configuration file at: /usr/local/etc/" + Cocoon.DEFAULT_CONF_FILE);
        f = new File("/usr/local/etc/" + Cocoon.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        log.error("Could not find the configuration file.");
        throw new FileNotFoundException("The configuration file could not be found.");
    }

    private static File getDir(String dir, String type) throws Exception {

        log.debug("Getting handle to " + type + " directory '" + dir + "'");
        File d = new File(dir);

        if (!d.exists()) {
            if (!d.mkdirs()) {
                log.error("Error creating " + type + " directory '" + d + "'");
                throw new IOException("Error creating " + type + " directory '" + d + "'");
            }
        }

        if (!d.isDirectory()) {
            log.error("'" + d + "' is not a directory.");
            throw new IOException("'" + d + "' is not a directory.");
        }

        if (!(d.canRead() && d.canWrite())) {
            log.error("Directory '" + d + "' is not readable/writable");
            throw new IOException("Directory '" + d + "' is not readable/writable");
        }

        return d;
    }

    // -----------------------------------------------------------------------

    private Cocoon cocoon;
    private File destDir;
    private File context;
    private Map attributes;

    /**
     * Creates the Main class
     */
    public Main(Cocoon cocoon, File context, File destDir) {
        this.cocoon = cocoon;
        this.context = context;
        this.destDir = destDir;
        this.attributes = new HashMap();
    }

    /**
     * Warms up the engine by accessing the root.
     */
    public void warmup() throws Exception {
        log.info("Warming up...");
        log.info(" [Cocoon might need to compile the sitemaps, this might take a while]");
        cocoon.process(new LinkSamplingEnvironment("/", context, attributes, null));
    }

    /**
     * Process the URI list and process them all independently.
     */
    public void process(Collection uris) throws Exception {
        log.info("...ready, let's go:");
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            this.processURI(NetUtils.normalize((String) i.next()), 0);
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
     *      consistency with the URI and, if the extension is inconsistent
     *      or absent, the file is renamed</li>
     *  <li>then the file name of the translated URI is returned</li>
     * </ul>
     */
    public String processURI(String uri, int level) throws Exception {
        log.info("Processing URI: " + leaf(level) + uri);

        Collection links = this.getLinks(uri);
        Map translatedLinks = new HashMap(links.size());
        Iterator i = links.iterator();
        while (i.hasNext()) {
            log.info(tree(level));
            String path = NetUtils.getPath(uri);
            String relativeLink = (String) i.next();
            String absoluteLink = NetUtils.normalize(NetUtils.absolutize(path, relativeLink));
            String translatedAbsoluteLink = this.processURI(absoluteLink, level + 1);
            String translatedRelativeLink = NetUtils.relativize(path, translatedAbsoluteLink);
            translatedLinks.put(relativeLink, translatedRelativeLink);
        }

        String filename = mangle(uri);
        File file = IOUtils.createFile(destDir, filename);
        FileOutputStream output = new FileOutputStream(file);
        String type = getPage(uri, translatedLinks, output);
        output.close();

        String ext = NetUtils.getExtension(filename);
        String defaultExt = MIMEUtils.getDefaultExtension(type);

        if ((ext == null) || (!ext.equals(defaultExt))) {
            filename += defaultExt;
            File newFile = IOUtils.createFile(destDir, filename);
            file.renameTo(newFile);
            file = newFile;
        }

        log.info(tree(level));

        if (type == null) {
            log.warn(leaf(level + 1) + "[broken link]--> " + filename);
            resourceUnavailable(file);
        } else {
            log.info(leaf(level + 1) + "[" + type + "]--> " + filename);
        }

        return filename;
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
        log.debug("mangle(\"" + uri + "\")");
        if (uri.charAt(uri.length() - 1) == '/') uri += Cocoon.INDEX_URI;
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        log.debug(uri);
        return uri;
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
        HashMap parameters = new HashMap();
        String deparameterizedURI = NetUtils.deparameterize(uri, parameters);
        LinkSamplingEnvironment env = new LinkSamplingEnvironment(deparameterizedURI, context, attributes, parameters);
        cocoon.process(env);
        return env.getLinks();
    }

    String getPage(String uri, Map links, OutputStream stream) throws Exception {
        HashMap parameters = new HashMap();
        String deparameterizedURI = NetUtils.deparameterize(uri, parameters);
        FileSavingEnvironment env = new FileSavingEnvironment(uri, context, attributes, parameters, links, stream);
        cocoon.process(env);
        return env.getContentType();
    }
}


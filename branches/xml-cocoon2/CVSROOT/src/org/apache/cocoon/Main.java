/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.avalon.context.DefaultContext;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.FileSavingEnvironment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.JavaArchiveFilter;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.cli.CLArgsParser;
import org.apache.excalibur.cli.CLOption;
import org.apache.excalibur.cli.CLOptionDescriptor;
import org.apache.excalibur.cli.CLUtil;
import org.apache.log.Category;
import org.apache.log.LogKit;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.output.DefaultOutputLogTarget;
import org.apache.log.output.FileOutputLogTarget;

/**
 * Command line entry point.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.4.31 $ $Date: 2001-04-25 17:04:54 $
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
    protected static final int XSP_OPT =         'x';

    protected static final CLOptionDescriptor [] options = new CLOptionDescriptor [] {
        new CLOptionDescriptor("help",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               HELP_OPT,
                               "print this message and exit"),
        new CLOptionDescriptor("version",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               VERSION_OPT,
                               "print the version information and exit"),
        new CLOptionDescriptor("logUrl",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               LOG_URL_OPT,
                               "use given file for log"),
        new CLOptionDescriptor("logLevel",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               LOG_LEVEL_OPT,
                               "choose the minimum log level for logging (DEBUG, INFO, WARN, ERROR, FATAL_ERROR)"),
        new CLOptionDescriptor("contextDir",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               CONTEXT_DIR_OPT,
                               "use given dir as context"),
        new CLOptionDescriptor("destDir",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               DEST_DIR_OPT,
                               "use given dir as destination"),
        new CLOptionDescriptor("workDir",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               WORK_DIR_OPT,
                               "use given dir as working directory"),
        new CLOptionDescriptor("xspOnly",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               XSP_OPT,
                               "generate java code for xsp files")
    };


    public static void main(String[] args) throws Exception {

        String destDir = Constants.DEFAULT_DEST_DIR;
        String contextDir = Constants.DEFAULT_CONTEXT_DIR;
        String workDir = Constants.DEFAULT_WORK_DIR;
        List targets = new ArrayList();
        CLArgsParser parser = new CLArgsParser(args, options);
        String logUrl = null;
        String logLevel = "DEBUG";
        boolean xspOnly = false;

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

                case Main.XSP_OPT:
                    xspOnly = true;
                    break;
            }
        }

        try {
            LogKit.setGlobalPriority(LogKit.getPriorityForName(logLevel));
            Category cocoonCategory = LogKit.createCategory("cocoon", LogKit.getPriorityForName(logLevel));
            if(logUrl == null)
                log = LogKit.createLogger(cocoonCategory,new LogTarget[] {new DefaultOutputLogTarget(System.out)});
            else
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

        if (targets.size() == 0 && xspOnly == false) {
            String error = "Please, specify at least one starting URI.";
            log.error(error);
            System.out.println(error);
            System.exit(1);
        }

        try {
            File dest = null;
            if(!xspOnly)
                dest = getDir(destDir, "destination");
            File work = getDir(workDir, "working");
            File context = getDir(contextDir, "context");
            File conf = getConfigurationFile(context);
            DefaultContext appContext = new DefaultContext();
            //appContext.put(Constants.CONTEXT_SERVLET_CONTEXT, contextDir);
            appContext.put(Constants.CONTEXT_ROOT_PATH, contextDir);
            appContext.put(Constants.CONTEXT_CLASS_LOADER, Main.class.getClassLoader());
            appContext.put(Constants.CONTEXT_CONFIG_URL, conf.toURL());
            appContext.put(Constants.CONTEXT_WORK_DIR, work);
            Cocoon c = new Cocoon();
            c.setLogger(log);
            c.contextualize(appContext);
            c.initialize();
            Main main = new Main(c, context, dest);
            main.warmup();
            if(main.process(targets, xspOnly)==0)
                main.recursivelyProcessXSP(context, context);
            c.dispose();
            log.info("Done");
        } catch (Exception e) {
            log.fatalError("Exception caught ", e);
            System.exit(1);
        }
    }

    private static void printVersion() {
        System.out.println(Constants.VERSION);
        System.exit(0);
    }

    private static void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("------------------------------------------------------------------------ ").append(lSep);
        msg.append(Constants.NAME).append(" ").append(Constants.VERSION).append(lSep);
        msg.append("Copyright (c) ").append(Constants.YEAR).append(" Apache Software Foundation. All rights reserved.").append(lSep);
        msg.append("------------------------------------------------------------------------ ").append(lSep).append(lSep);
        msg.append("Usage: java org.apache.cocoon.Main [options] [targets]").append(lSep).append(lSep);
        msg.append("Options: ").append(lSep);
        msg.append(CLUtil.describeOptions(Main.options).toString());
        msg.append("Note: the context directory defaults to '").append(Constants.DEFAULT_CONTEXT_DIR + "'").append(lSep);
        System.out.println(msg.toString());
        System.exit(0);
    }

    private static File getConfigurationFile(File dir) throws Exception {

        log.debug("Trying configuration file at: " + dir + File.separator + Constants.DEFAULT_CONF_FILE);
        File f = new File(dir, Constants.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        log.debug("Trying configuration file at: " + System.getProperty("user.dir") + File.separator + Constants.DEFAULT_CONF_FILE);
        f = new File(System.getProperty("user.dir") + File.separator + Constants.DEFAULT_CONF_FILE);
        if (f.canRead()) return f;

        log.debug("Trying configuration file at: /usr/local/etc/" + Constants.DEFAULT_CONF_FILE);
        f = new File("/usr/local/etc/" + Constants.DEFAULT_CONF_FILE);
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
        //cocoon.process(new LinkSamplingEnvironment("/", context, attributes, null));
        cocoon.generateSitemap(new LinkSamplingEnvironment("/", context, attributes, null));
    }

    /**
     * Process the URI list and process them all independently.
     */
    public int process(Collection uris, boolean xspOnly) throws Exception {
        int nCount = 0;
        log.info("...ready, let's go:");
        Iterator i = uris.iterator();
        while (i.hasNext()) {
            if(xspOnly)
                this.processXSP(NetUtils.normalize((String) i.next()));
            else
                this.processURI(NetUtils.normalize((String) i.next()), 0);
            nCount++;
        }
        return nCount;
    }

    /**
     * Recurse the directory hierarchy and process the XSP's.
     */
    public void recursivelyProcessXSP(File contextDir, File file) {
        if (file.isDirectory()) {
            String entries[] = file.list();
            for (int i = 0; i < entries.length; i++) {
                recursivelyProcessXSP(contextDir, new File(file, entries[i]));
            }
        } else if(file.getName().toLowerCase().endsWith(".xsp")) {
            try {
                this.processXSP(IOUtils.getContextFilePath(contextDir.getCanonicalPath(),file.getCanonicalPath()));
            } catch (Exception e){
                //Ignore for now.
            }
        }
    }

    public void processXSP(String uri) throws Exception {
        Environment env = new LinkSamplingEnvironment("/", context, attributes, null);
        cocoon.generateXSP(uri, env);
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
        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
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

    private void resourceUnavailable(File file) throws IOException {
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.println(
           "<html><head><title>Page Not Available</title></head>" +
           "<body><h1 align=\"center\">Page Not Available</h1>" +
           "<body><p align=\"center\">Generated by " +
           Constants.COMPLETE_NAME +
           "</p></body></html>"
        );
        out.close();
    }

    private String mangle(String uri) {
        log.debug("mangle(\"" + uri + "\")");
        if (uri.charAt(uri.length() - 1) == '/') uri += Constants.INDEX_URI;
        uri = uri.replace('"', '\'');
        uri = uri.replace('?', '_');
        log.debug(uri);
        return uri;
    }

    private String leaf(int level) {
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


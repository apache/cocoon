/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

import org.apache.cocoon.Constants;
import org.apache.cocoon.util.NetUtils;

import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.destination.FileDestination;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a> 
 * @version CVS $Id: Main.java,v 1.2 2003/03/18 15:23:30 nicolaken Exp $
 */
public class Main {

    protected static final String HELP_OPT =               "h";
    protected static final String VERSION_OPT =            "v";
    protected static final String VERBOSE_OPT =            "V";
    protected static final String LOG_KIT_OPT =            "k";
    protected static final String LOGGER_OPT =             "l";
    protected static final String LOG_LEVEL_OPT =          "u";
    protected static final String CONTEXT_DIR_OPT =        "c";
    protected static final String DEST_DIR_OPT =           "d";
    protected static final String WORK_DIR_OPT =           "w";
    protected static final String CONFIG_FILE_OPT =        "C";
    protected static final String BROKEN_LINK_FILE_OPT =   "b";
    protected static final String URI_FILE_OPT =           "f";
    protected static final String XCONF_OPT =              "x";
    protected static final String AGENT_OPT =              "a";
    protected static final String ACCEPT_OPT =             "p";
    protected static final String FOLLOW_LINKS_OPT =       "r";
    protected static final String PRECOMPILE_ONLY_OPT =    "P";
    protected static final String CONFIRM_EXTENSIONS_OPT = "e";
    protected static final String LOAD_CLASS_OPT =         "L";
    protected static final String DEFAULT_FILENAME_OPT =   "D";

    protected static final String HELP_LONG =               "help";
    protected static final String VERSION_LONG =            "version";
    protected static final String VERBOSE_LONG =            "verbose";
    protected static final String LOG_KIT_LONG =            "logKitconfig";
    protected static final String LOGGER_LONG =             "Logger";
    protected static final String LOG_LEVEL_LONG =          "logLevel";
    protected static final String CONTEXT_DIR_LONG =        "contextDir";
    protected static final String DEST_DIR_LONG =           "destDir";
    protected static final String WORK_DIR_LONG =           "workDir";
    protected static final String CONFIG_FILE_LONG =        "configFile";
    protected static final String BROKEN_LINK_FILE_LONG =   "brokenLinkFile";
    protected static final String URI_FILE_LONG =           "uriFile";
    protected static final String XCONF_LONG =              "xconf";
    protected static final String AGENT_LONG =              "userAgent";
    protected static final String ACCEPT_LONG =             "accept";
    protected static final String FOLLOW_LINKS_LONG =       "followLinks";
    protected static final String PRECOMPILE_ONLY_LONG =    "precompileOnly";
    protected static final String CONFIRM_EXTENSIONS_LONG = "confirmExtensions";
    protected static final String LOAD_CLASS_LONG =         "loadClass";
    protected static final String DEFAULT_FILENAME_LONG =   "defaultFilename";
    protected static final String URI_LONG =                "uri";

    private static final String NODE_ROOT = "cocoon";
    private static final String ATTR_VERBOSE = "verbose";

    private static final String NODE_LOGGING = "logging";
    private static final String ATTR_LOG_KIT = "log-kit";
    private static final String ATTR_LOG_LEVEL = "level";
    private static final String ATTR_LOGGER = "logger";

    private static final String NODE_CONTEXT_DIR = "context-dir";
    private static final String NODE_DEST_DIR = "dest-dir";
    private static final String NODE_WORK_DIR = "work-dir";
    private static final String NODE_CONFIG_FILE = "config-file";
    private static final String NODE_BROKEN_LINK_FILE = "broken-link-file";
    private static final String NODE_URI_FILE = "uri-file";

    private static final String NODE_AGENT = "user-agent";
    private static final String NODE_ACCEPT = "accept";

    private static final String ATTR_FOLLOW_LINKS = "follow-links";
    private static final String ATTR_PRECOMPILE_ONLY = "precompile-only";
    private static final String ATTR_CONFIRM_EXTENSIONS = "confirm-extensions";
    private static final String NODE_LOAD_CLASS = "load-class";
    private static final String NODE_DEFAULT_FILENAME = "default-filename";
    private static final String NODE_URI = "uri";

    private static Options options;

    private static boolean verbose = false;
    private static String logKit = null;
    private static String logger = null;
    private static String logLevel = "ERROR";
    private static String contextDir = Constants.DEFAULT_CONTEXT_DIR;
    private static String destDir = Constants.DEFAULT_DEST_DIR;
    private static String workDir = Constants.DEFAULT_WORK_DIR;
    private static String configFile = Constants.DEFAULT_CONF_FILE;
    private static String brokenLinkFile = null;
    private static String agentOptions = null;
    private static String acceptOptions = null;
    private static String defaultFilename = Constants.INDEX_URI;
    private static boolean precompileOnly = false;
    private static boolean followLinks = true;
    private static boolean confirmExtensions = true;
    private static List loadedClasses = new ArrayList();
    private static List targets = new ArrayList();

    private static void setOptions() {
        options = new Options();

        options.addOption(new Option(HELP_OPT,
                                     HELP_LONG,
                                     false,
                                     "print this message and exit"));

        options.addOption(new Option(VERSION_OPT,
                                     VERSION_LONG,
                                     false,
                                     "print the version information and exit"));

        options.addOption(new Option(VERBOSE_OPT,
                                     VERBOSE_LONG,
                                     false,
                                     "enable verbose messages to System.out"));

        options.addOption(new Option(LOG_KIT_OPT,
                                     LOG_KIT_LONG,
                                     true,
                                     "use given file for LogKit Management configuration"));

        options.addOption(new Option(LOGGER_OPT,
                                     LOGGER_LONG,
                                     true,
                                     "use given logger category as default logger for the Cocoon engine"));

        options.addOption(new Option(LOG_LEVEL_OPT,
                                     LOG_LEVEL_LONG,
                                     true,
                                     "choose the minimum log level for logging (DEBUG, INFO, WARN, ERROR, FATAL_ERROR) for startup logging"));

        options.addOption(new Option(CONTEXT_DIR_OPT,
                                     CONTEXT_DIR_LONG,
                                     true,
                                     "use given dir as context"));

        options.addOption(new Option(DEST_DIR_OPT,
                                     DEST_DIR_LONG,
                                     true,
                                     "use given dir as destination"));

        options.addOption(new Option(WORK_DIR_OPT,
                                     WORK_DIR_LONG,
                                     true,
                                     "use given dir as working directory"));

        options.addOption(new Option(CONFIG_FILE_OPT,
                                     CONFIG_FILE_LONG,
                                     true,
                                     "specify alternate location of the configuration"
                                     + " file (default is ${contextDir}/cocoon.xconf)"));

        options.addOption(new Option(BROKEN_LINK_FILE_OPT,
                                     BROKEN_LINK_FILE_LONG,
                                     true,
                                     "send a list of broken links to a file (one URI per line)"));

        options.addOption(new Option(URI_FILE_OPT,
                                     URI_FILE_LONG,
                                     true,
                                     "use a text file with uris to process (one URI per line)"));

        options.addOption(new Option(XCONF_OPT,
                                     XCONF_LONG,
                                     true,
                                     "specify a file containing XML configuration details"
                                     + " for the command line interface"));

        options.addOption(new Option(AGENT_OPT,
                                     AGENT_LONG,
                                     true,
                                     "use given string for user-agent header"));

        options.addOption(new Option(ACCEPT_OPT,
                                     ACCEPT_LONG,
                                     true,
                                     "use given string for accept header"));

        options.addOption(new Option(FOLLOW_LINKS_OPT,
                                     FOLLOW_LINKS_LONG,
                                     true,
                                     "process pages linked from starting page or not"
                                     + " (boolean argument is expected, default is true)"));

        options.addOption(new Option(PRECOMPILE_ONLY_OPT,
                                     PRECOMPILE_ONLY_LONG,
                                     true,
                                     "generate java code for xsp and xmap files"));

        options.addOption(new Option(CONFIRM_EXTENSIONS_OPT,
                                     CONFIRM_EXTENSIONS_LONG,
                                     true,
                                     "confirm that file extensions match mime-type of"
                                     + " pages and amend filename accordingly (default"
                                     + " is true)"));

        options.addOption(new Option(LOAD_CLASS_OPT,
                                     LOAD_CLASS_LONG,
                                     true,
                                     "specify a class to be loaded at startup (specifically"
                                     + " for use with JDBC). Can be used multiple times"));

        options.addOption(new Option(DEFAULT_FILENAME_OPT,
                                     DEFAULT_FILENAME_LONG,
                                     true,
                                     "specify a filename to be appended to a URI when the"
                                     + " URI refers to a directory"));
    }

    /**
     * The <code>main</code> method.
     *
     * @param args a <code>String[]</code> of arguments
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {

        long startTimeMillis = System.currentTimeMillis();

        Main.setOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse( options, args );

        if (line.hasOption(HELP_OPT)) {
             printUsage();
        }

        if (line.hasOption(VERSION_OPT)) {
             printVersion();
        }

        if (line.hasOption(XCONF_OPT)) {
            Main.processXConf(line.getOptionValue(XCONF_OPT));
        }
        if (line.hasOption(VERBOSE_OPT)) {
            verbose = true;
        }
        if (line.hasOption(PRECOMPILE_ONLY_OPT)) {
            precompileOnly=true;
        }
        destDir = line.getOptionValue(DEST_DIR_OPT, destDir);
        workDir = line.getOptionValue(WORK_DIR_OPT, workDir);
        contextDir = line.getOptionValue(CONTEXT_DIR_OPT, contextDir);
        configFile = line.getOptionValue(CONFIG_FILE_OPT, configFile);
        logKit = line.getOptionValue(LOG_KIT_OPT, logKit);
        logger = line.getOptionValue(LOGGER_OPT, logger);
        logLevel = line.getOptionValue(LOG_LEVEL_OPT, logLevel);
        agentOptions = line.getOptionValue(AGENT_OPT, agentOptions);
        acceptOptions = line.getOptionValue(ACCEPT_OPT, acceptOptions);
        defaultFilename = line.getOptionValue(DEFAULT_FILENAME_OPT, defaultFilename);
        brokenLinkFile = line.getOptionValue(BROKEN_LINK_FILE_OPT, brokenLinkFile);

        if (line.hasOption(URI_FILE_OPT)) {
            Main.processURIFile(line.getOptionValue(URI_FILE_OPT), targets);
        }
        if (line.hasOption(FOLLOW_LINKS_OPT)) {
            followLinks = "yes".equals(line.getOptionValue(FOLLOW_LINKS_OPT, "yes"))
                          || "true".equals(line.getOptionValue(FOLLOW_LINKS_OPT, "true"));
        }
        if (line.hasOption(CONFIRM_EXTENSIONS_OPT)) {
            confirmExtensions = "yes".equals(line.getOptionValue(CONFIRM_EXTENSIONS_OPT, "yes"))
                                || "true".equals(line.getOptionValue(CONFIRM_EXTENSIONS_OPT, "true"));
        }
        if (line.hasOption(LOAD_CLASS_OPT)){
            loadedClasses.add(Arrays.asList(line.getOptionValues(LOAD_CLASS_OPT)));
        }

        for (Iterator i = line.getArgList().iterator(); i.hasNext();) {
            targets.add(NetUtils.normalize((String) i.next()));
        }

        CocoonBean cocoon = new CocoonBean(workDir, contextDir, configFile);
        cocoon.setLogKit(logKit);
        cocoon.setLogger(logger);
        cocoon.setLogLevel(logLevel);
        
        if (loadedClasses.size()!=0) {
            cocoon.setLoadedClasses(loadedClasses);
        }

        if (agentOptions != null) {
            cocoon.setAgentOptions(agentOptions);
        }
        if (acceptOptions != null) {
            cocoon.setAcceptOptions(acceptOptions);
        }
        if (defaultFilename != null) {
            cocoon.setDefaultFilename(defaultFilename);
        }        
        cocoon.setBrokenLinkFile(brokenLinkFile);
        cocoon.setPrecompileOnly(precompileOnly);
        cocoon.setFollowLinks(followLinks);
        cocoon.setConfirmExtensions(confirmExtensions);
        cocoon.setVerbose(verbose);

        if (destDir.equals("")) {
            String error = "Careful, you must specify a destination dir when using the -d/--destDir argument";
            cocoon.getLogger().fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (cocoon.getContextDir().equals("")) {
            String error = "Careful, you must specify a configuration file when using the -c/--contextDir argument";
            cocoon.getLogger().fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (cocoon.getWorkDir().equals("")) {
            String error = "Careful, you must specify a destination dir when using the -w/--workDir argument";
            cocoon.getLogger().fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        if (targets.size() == 0 && !cocoon.isPrecompileOnly()) {
            String error = "Please, specify at least one starting URI.";
            cocoon.getLogger().fatalError(error);
            System.out.println(error);
            System.exit(1);
        }

        System.out.println(getProlog());
        
        cocoon.initialize();
        cocoon.warmup();
        cocoon.process(targets, new FileDestination(destDir));
        cocoon.dispose();

        long duration = System.currentTimeMillis() - startTimeMillis;
        System.out.println("Total time: " + (duration / 60000) + " minutes " + (duration % 60000)/1000 + " seconds");
        System.exit(0);
    }

    /**
     * <code>processURIFile</code> method.
     *
     * @param filename a <code>String</code> value
     * @param uris a <code>List</code> of URIs
     */
    public static void processURIFile(String filename, List uris) {
        try {
            BufferedReader uriFile = new BufferedReader(new FileReader(filename));
            boolean eof = false;

            while (!eof) {
                String uri = uriFile.readLine();

                if (null == uri) {
                    eof = true;
                } else {
                    uris.add(NetUtils.normalize(uri.trim()));
                }
            }

            uriFile.close();
        } catch (Exception e) {
            // ignore errors.
        }
    }

    /**
     * <code>processXConf</code> method. Reads in XML configuration from
     * specified xconf file.
     *
     * @param filename a <code>String</code> value
     */
    private static void processXConf(String filename) {

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            final Document conf = builder.parse(new File(filename).toURL().toExternalForm());

            Node root = conf.getDocumentElement();

            if (!NODE_ROOT.equals(root.getNodeName())) {
                throw new IllegalArgumentException("Expected root node of "+ NODE_ROOT);
            }

            verbose = Main.getBooleanAttributeValue(root, ATTR_VERBOSE, verbose);
            followLinks = Main.getBooleanAttributeValue(root, ATTR_FOLLOW_LINKS, followLinks);
            precompileOnly = Main.getBooleanAttributeValue(root, ATTR_PRECOMPILE_ONLY, precompileOnly);
            confirmExtensions = Main.getBooleanAttributeValue(root, ATTR_CONFIRM_EXTENSIONS, confirmExtensions);

            NodeList nodes = root.getChildNodes();

            for (int i=0; i<nodes.getLength();i++) {
                Node node = nodes.item(i);
                if (node.getNodeType()== Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    if (nodeName.equals(NODE_BROKEN_LINK_FILE)) {
                        brokenLinkFile = getNodeValue(node);

                    } else if (nodeName.equals(NODE_LOAD_CLASS)) {
                        loadedClasses.add(getNodeValue(node));

                    } else if (nodeName.equals(NODE_LOGGING)) {
                        parseLoggingNode(node);

                    } else if (nodeName.equals(NODE_CONTEXT_DIR)) {
                        contextDir = getNodeValue(node);

                    } else if (nodeName.equals(NODE_CONFIG_FILE)) {
                        configFile = getNodeValue(node);

                    } else if (nodeName.equals(NODE_DEST_DIR)) {
                        destDir = getNodeValue(node);

                    } else if (nodeName.equals(NODE_WORK_DIR)) {
                        workDir = getNodeValue(node);

                    } else if (nodeName.equals(NODE_AGENT)) {
                        agentOptions = getNodeValue(node);

                    } else if (nodeName.equals(NODE_ACCEPT)) {
                        acceptOptions = getNodeValue(node);

                    } else if (nodeName.equals(NODE_DEFAULT_FILENAME)) {
                        defaultFilename = getNodeValue(node);

                    } else if (nodeName.equals(NODE_URI)) {
                        targets.add(NetUtils.normalize(getNodeValue(node)));

                    } else if (nodeName.equals(NODE_URI_FILE)) {
                        Main.processURIFile(getNodeValue(node), targets);

                    } else {
                        throw new IllegalArgumentException("Unknown element: " + nodeName);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR: "+e.getMessage());
        }
    }

    private static void parseLoggingNode(Node node) throws IllegalArgumentException {
        logKit = Main.getAttributeValue(node, ATTR_LOG_KIT, logKit);
        logger = Main.getAttributeValue(node, ATTR_LOGGER, logger);
        logLevel = Main.getAttributeValue(node, ATTR_LOG_LEVEL, logLevel);

        NodeList nodes = node.getChildNodes();
        if (nodes.getLength()!=0) {
            throw new IllegalArgumentException("Unexpected children of "+NODE_LOGGING+" node");
        }
    }

    private static String getNodeValue(Node node) throws IllegalArgumentException {
        StringBuffer s = new StringBuffer();
        NodeList children = node.getChildNodes();
        boolean found = false;

        for (int i=0; i< children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                s.append(child.getNodeValue());
                found = true;
            } else {
                throw new IllegalArgumentException("Unexpected node:" + child.getLocalName());
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Expected value for " + node.getLocalName() + " node");
        }
        return s.toString();
    }

    private static String getAttributeValue(Node node, String attr, String defaultValue) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            if (attribute != null) {
                return attribute.getNodeValue();
            }
        }
        return defaultValue;
    }

    private static boolean getBooleanAttributeValue(Node node, String attr, boolean defaultValue) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);

            if (attribute != null) {
                String value = attribute.getNodeValue();
                return "yes".equals(value)
                        || "true".equals(value);
            }
        }
        return defaultValue;
    }

    /**
     * Print a description of the software before running
     */
    private static String getProlog() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("------------------------------------------------------------------------ ").append(lSep);
        msg.append(Constants.NAME).append(" ").append(Constants.VERSION).append(lSep);
        msg.append("Copyright (c) ").append(Constants.YEAR).append(" Apache Software Foundation. All rights reserved.").append(lSep);
        msg.append("------------------------------------------------------------------------ ").append(lSep).append(lSep);
        return msg.toString();
    }
         
    /**
     * Print the usage message and exit
     */
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("cocoon cli [options] [targets]",
                            getProlog().toString(),
                            options,
                            "Note: the context directory defaults to '"+ Constants.DEFAULT_CONTEXT_DIR + "'");
        System.exit(0);
    }
    
    /**
     * Print the version string and exit
     */
    private static void printVersion() {
        System.out.println(Constants.VERSION);
        System.exit(0);
    }    
}














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


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

import org.apache.cocoon.Constants;
import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.BeanListener;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: Main.java,v 1.11 2003/07/21 09:37:40 jefft Exp $
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
    private static final String NODE_URI_FILE = "uri-file";

    private static final String NODE_BROKEN_LINKS = "broken-links";
    private static final String ATTR_BROKEN_LINK_REPORT_TYPE = "type";
    private static final String ATTR_BROKEN_LINK_REPORT_FILE = "file";
    private static final String ATTR_BROKEN_LINK_GENERATE = "generate";
    private static final String ATTR_BROKEN_LINK_EXTENSION = "extension";

    private static final String NODE_AGENT = "user-agent";
    private static final String NODE_ACCEPT = "accept";

    private static final String ATTR_FOLLOW_LINKS = "follow-links";
    private static final String ATTR_PRECOMPILE_ONLY = "precompile-only";
    private static final String ATTR_CONFIRM_EXTENSIONS = "confirm-extensions";
    private static final String NODE_LOAD_CLASS = "load-class";
    private static final String NODE_DEFAULT_FILENAME = "default-filename";


    private static final String NODE_URI = "uri";
    private static final String ATTR_URI_TYPE = "type";
    private static final String ATTR_URI_SOURCEPREFIX = "src-prefix";
    private static final String ATTR_URI_SOURCEURI = "src";
    private static final String ATTR_URI_DESTURI = "dest";

    private static Options options;
    private static List brokenLinks = new ArrayList();
    private static String brokenLinkReportFile = null;
    private static String brokenLinkReportType = "text";

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
        CommandLine line = new PosixParser().parse( options, args );
        CLIListener listener = new Main.CLIListener();
        CocoonBean cocoon = new CocoonBean();
        cocoon.addListener(listener);

        if (line.hasOption(HELP_OPT)) {
             printUsage();
        } else if (line.hasOption(VERSION_OPT)) {
             printVersion();
        }

        String destDir = null;
        if (line.hasOption(DEST_DIR_OPT)) {
            destDir = line.getOptionValue(DEST_DIR_OPT);
        }

        if (line.hasOption(XCONF_OPT)) {
            // destDir from command line overrides one in xconf file
            destDir = Main.processXConf(cocoon, line.getOptionValue(XCONF_OPT), destDir);
        }
        if (line.hasOption(VERBOSE_OPT)) {
            cocoon.setVerbose(true);
        }
        if (line.hasOption(PRECOMPILE_ONLY_OPT)) {
            cocoon.setPrecompileOnly(true);
        }

        if (line.hasOption(WORK_DIR_OPT)) {
            cocoon.setWorkDir(line.getOptionValue(WORK_DIR_OPT));
        }
        if (line.hasOption(CONTEXT_DIR_OPT)) {
            cocoon.setContextDir(line.getOptionValue(CONTEXT_DIR_OPT));
        }
        if (line.hasOption(CONFIG_FILE_OPT)) {
            cocoon.setConfigFile(line.getOptionValue(CONFIG_FILE_OPT));
        }
        if (line.hasOption(LOG_KIT_OPT)) {
            cocoon.setLogKit(line.getOptionValue(LOG_KIT_OPT));
        }
        if (line.hasOption(LOGGER_OPT)) {
            cocoon.setLogger(line.getOptionValue(LOGGER_OPT));
        }
        if (line.hasOption(LOG_LEVEL_OPT)) {
            cocoon.setLogLevel(line.getOptionValue(LOG_LEVEL_OPT));
        }
        if (line.hasOption(AGENT_OPT)) {
            cocoon.setAgentOptions(line.getOptionValue(AGENT_OPT));
        }
        if (line.hasOption(ACCEPT_OPT)) {
            cocoon.setAcceptOptions(line.getOptionValue(ACCEPT_OPT));
        }
        if (line.hasOption(DEFAULT_FILENAME_OPT)) {
            cocoon.setDefaultFilename(line.getOptionValue(DEFAULT_FILENAME_OPT));
        }
        if (line.hasOption(BROKEN_LINK_FILE_OPT)) {
            brokenLinkReportFile = line.getOptionValue(BROKEN_LINK_FILE_OPT);
        }
        if (line.hasOption(URI_FILE_OPT)) {
            cocoon.addTargets(processURIFile(line.getOptionValue(URI_FILE_OPT)), destDir);
        }
        if (line.hasOption(FOLLOW_LINKS_OPT)) {
            cocoon.setFollowLinks(yesno(line.getOptionValue(FOLLOW_LINKS_OPT)));
        }
        if (line.hasOption(CONFIRM_EXTENSIONS_OPT)) {
            cocoon.setConfirmExtensions(yesno(line.getOptionValue(CONFIRM_EXTENSIONS_OPT, "yes")));
        }
        if (line.hasOption(LOAD_CLASS_OPT)){
            cocoon.addLoadedClasses(Arrays.asList(line.getOptionValues(LOAD_CLASS_OPT)));
        }

        cocoon.addTargets(line.getArgList(), destDir);

        System.out.println(getProlog());

        cocoon.initialize();
        cocoon.process();
        cocoon.dispose();

        listener.outputBrokenLinks();

        long duration = System.currentTimeMillis() - startTimeMillis;
        System.out.println("Total time: " + (duration / 60000) + " minutes " + (duration % 60000)/1000 + " seconds");

        int exitCode = (brokenLinks.size() == 0 ? 0 : 1);
        System.exit(exitCode);
    }

    private static boolean yesno(String in) {
        return "yes".equals(in) || "true".equals(in);
    }

    /**
     * <code>processURIFile</code> method.
     *
     * @param filename a <code>String</code> value
     * @return uris a <code>List</code> of URIs
     */
    public static List processURIFile(String filename) {
        List uris = new ArrayList();
        try {
            BufferedReader uriFile = new BufferedReader(new FileReader(filename));
            boolean eof = false;

            while (!eof) {
                String uri = uriFile.readLine();

                if (null == uri) {
                    eof = true;
                } else {
                    uris.add(uri.trim());
                }
            }

            uriFile.close();
        } catch (Exception e) {
            // ignore errors.
        }
        return uris;
    }

    /**
     * <code>processXConf</code> method. Reads in XML configuration from
     * specified xconf file.
     *
     * @param cocoon a <code>CocoonBean</code> that will be configured by the xconf file
     * @param filename a <code>String</code> value
     */
    private static String processXConf(CocoonBean cocoon, String filename, String destDir) {

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document conf = builder.parse(new File(filename).toURL().toExternalForm());

            Node root = conf.getDocumentElement();
            if (!NODE_ROOT.equals(root.getNodeName())) {
                throw new IllegalArgumentException("Expected root node of "+ NODE_ROOT);
            }

            if (Main.hasAttribute(root, ATTR_VERBOSE)) {
                cocoon.setVerbose(Main.getBooleanAttributeValue(root, ATTR_VERBOSE));
            }
            if (Main.hasAttribute(root, ATTR_FOLLOW_LINKS)) {
                cocoon.setFollowLinks(Main.getBooleanAttributeValue(root, ATTR_FOLLOW_LINKS));
            }
            if (Main.hasAttribute(root, ATTR_PRECOMPILE_ONLY)) {
                cocoon.setPrecompileOnly(Main.getBooleanAttributeValue(root, ATTR_PRECOMPILE_ONLY));
            }
            if (Main.hasAttribute(root, ATTR_CONFIRM_EXTENSIONS)) {
                cocoon.setConfirmExtensions(Main.getBooleanAttributeValue(root, ATTR_CONFIRM_EXTENSIONS));
            }

            if (destDir == null || destDir.length() == 0) {
                destDir = getNodeValue(root, NODE_DEST_DIR);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType()== Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    if (nodeName.equals(NODE_BROKEN_LINKS)) {
                        parseBrokenLinkNode(cocoon, node);

                    } else if (nodeName.equals(NODE_LOAD_CLASS)) {
                        cocoon.addLoadedClass(getNodeValue(node));

                    } else if (nodeName.equals(NODE_LOGGING)) {
                        parseLoggingNode(cocoon, node);

                    } else if (nodeName.equals(NODE_CONTEXT_DIR)) {
                        cocoon.setContextDir(getNodeValue(node));

                    } else if (nodeName.equals(NODE_CONFIG_FILE)) {
                        cocoon.setConfigFile(getNodeValue(node));

                    } else if (nodeName.equals(NODE_DEST_DIR)) {
                        // Ignore

                    } else if (nodeName.equals(NODE_WORK_DIR)) {
                        cocoon.setWorkDir(getNodeValue(node));

                    } else if (nodeName.equals(NODE_AGENT)) {
                        cocoon.setAgentOptions(getNodeValue(node));

                    } else if (nodeName.equals(NODE_ACCEPT)) {
                        cocoon.setAcceptOptions(getNodeValue(node));

                    } else if (nodeName.equals(NODE_DEFAULT_FILENAME)) {
                        cocoon.setDefaultFilename(getNodeValue(node));

                    } else if (nodeName.equals(NODE_URI)) {
                        Main.parseURINode(cocoon, node, destDir);

                    } else if (nodeName.equals(NODE_URI_FILE)) {
                        cocoon.addTargets(Main.processURIFile(getNodeValue(node)), destDir);

                    } else {
                        throw new IllegalArgumentException("Unknown element: <" + nodeName + ">");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return destDir;
    }

    private static void parseLoggingNode(CocoonBean cocoon, Node node) throws IllegalArgumentException {
        if (Main.hasAttribute(node, ATTR_LOG_KIT)) {
            cocoon.setLogKit(Main.getAttributeValue(node, ATTR_LOG_KIT));
        }
        if (Main.hasAttribute(node, ATTR_LOGGER)) {
            cocoon.setLogger(Main.getAttributeValue(node, ATTR_LOGGER));
        }
        if (Main.hasAttribute(node, ATTR_LOG_LEVEL)) {
            cocoon.setLogLevel(Main.getAttributeValue(node, ATTR_LOG_LEVEL));
        }
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength()!=0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_LOGGING + "> node");
        }
    }

    private static void parseBrokenLinkNode(CocoonBean cocoon, Node node) throws IllegalArgumentException {
        if (Main.hasAttribute(node, ATTR_BROKEN_LINK_REPORT_FILE)) {
            brokenLinkReportFile = Main.getAttributeValue(node, ATTR_BROKEN_LINK_REPORT_FILE);
        }
        if (Main.hasAttribute(node, ATTR_BROKEN_LINK_REPORT_TYPE)) {
            brokenLinkReportType = Main.getAttributeValue(node, ATTR_BROKEN_LINK_REPORT_TYPE);
        }
        if (Main.hasAttribute(node, ATTR_BROKEN_LINK_GENERATE)) {
        cocoon.setBrokenLinkGenerate(Main.getBooleanAttributeValue(node, ATTR_BROKEN_LINK_GENERATE));
        }
        if (Main.hasAttribute(node, ATTR_BROKEN_LINK_EXTENSION)) {
        cocoon.setBrokenLinkExtension(Main.getAttributeValue(node, ATTR_BROKEN_LINK_EXTENSION));
        }
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength()!=0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_BROKEN_LINKS + "> node");
        }
    }

    private static void parseURINode(CocoonBean cocoon, Node node, String destDir) throws IllegalArgumentException {
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength() != 0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_URI + "> node");
        }

        if (node.getAttributes().getLength() == 0) {
            cocoon.addTarget(getNodeValue(node), destDir);
        } else {
            String src = Main.getAttributeValue(node, ATTR_URI_SOURCEURI);

            String type = null;
            if (Main.hasAttribute(node, ATTR_URI_TYPE)) {
                type = Main.getAttributeValue(node, ATTR_URI_TYPE);
            }
            String root = null;
            if (Main.hasAttribute(node, ATTR_URI_SOURCEPREFIX)) {
                root = Main.getAttributeValue(node, ATTR_URI_SOURCEPREFIX);
            }
            String dest = null;
            if (Main.hasAttribute(node, ATTR_URI_DESTURI)) {
                dest = Main.getAttributeValue(node, ATTR_URI_DESTURI);
            }

            if (root != null && type != null & dest != null) {
                cocoon.addTarget(type, root, src, dest);
            } else if (root != null & dest != null) {
                cocoon.addTarget(root, src, dest);
            } else if (dest != null) {
                cocoon.addTarget(src, dest);
            } else {
                cocoon.addTarget(src, destDir);
            }
        }
    }

    private static String getNodeValue(Node root, String name) throws IllegalArgumentException {
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()== Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                if (nodeName.equals(name)) {
                    return getNodeValue(node);
                }
            }
        }
        return null;
    }

    private static String getNodeValue(Node node) throws IllegalArgumentException {
        StringBuffer s = new StringBuffer();
        NodeList children = node.getChildNodes();
        boolean found = false;

        for (int i = 0; i < children.getLength(); i++) {
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

    private static String getAttributeValue(Node node, String attr) throws IllegalArgumentException {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            if (attribute != null && attribute.getNodeValue() != null) {
                return attribute.getNodeValue();
            }
        }
        throw new IllegalArgumentException("Missing " + attr + " attribute on <" + node.getNodeName() + "> node");
    }

    private static boolean hasAttribute(Node node, String attr) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            return (attribute != null);
        }
        return false;
    }

    private static boolean getBooleanAttributeValue(Node node, String attr) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);

            if (attribute != null) {
                String value = attribute.getNodeValue();
                return "yes".equals(value)
                        || "true".equals(value);
            }
        }
        return false;
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
    public static class CLIListener implements BeanListener {
        public void pageGenerated(String uri, int linksInPage, int pagesRemaining) {
            if (linksInPage == -1) {
                this.print("* " + uri);
            } else {
                this.print("* ["+linksInPage + "] "+uri);
            }
        }
        public void messageGenerated(String msg) {
            this.print(msg);
        }

        public void warningGenerated(String uri, String warning) {
            this.print("Warning: "+warning + " when generating " + uri);
        }

        public void brokenLinkFound(String uri, String message) {
            this.print("X [0] "+uri+"\tBROKEN: "+message);
            brokenLinks.add(uri + "\t" + message);
        }

        public void outputBrokenLinks() {
            if (brokenLinkReportFile == null) {
                return;
            } else if ("text".equalsIgnoreCase(brokenLinkReportType)) {
                outputBrokenLinksAsText();
            } else if ("xml".equalsIgnoreCase(brokenLinkReportType)) {
                outputBrokenLinksAsXML();
            }
        }
        private void outputBrokenLinksAsText() {
            PrintWriter writer;
            try {
                writer =
                        new PrintWriter(
                                new FileWriter(new File(brokenLinkReportFile)),
                                true);
                for (Iterator i = brokenLinks.iterator(); i.hasNext();) {
                    writer.println((String) i.next());
                }
                writer.close();
            } catch (IOException ioe) {
                this.print("Broken link file does not exist: " + brokenLinkReportFile);
            }
        }
        private void outputBrokenLinksAsXML() {
            PrintWriter writer;
            try {
                writer =
                        new PrintWriter(
                                new FileWriter(new File(brokenLinkReportFile)),
                                true);
                writer.println("<broken-links>");
                for (Iterator i = brokenLinks.iterator(); i.hasNext();) {
                    String linkMsg = (String) i.next();
                    String uri = linkMsg.substring(0,linkMsg.indexOf('\t'));
                    String msg = linkMsg.substring(linkMsg.indexOf('\t')+1);
                    writer.println("  <link message=\"" + msg + "\">" + uri + "</link>");
                }
                writer.println("</broken-links>");
                writer.close();
            } catch (IOException ioe) {
                this.print("Could not create broken link file: " + brokenLinkReportFile);
            }
        }

        private void print(String message) {
            System.out.println(message);
        }
    }
}

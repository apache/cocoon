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

import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.helpers.OutputStreamListener;
import org.apache.cocoon.bean.helpers.BeanConfigurator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.w3c.dom.Document;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: Main.java,v 1.23 2003/10/13 12:08:10 upayavira Exp $
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
    protected static final String URI_GROUP_NAME_OPT =     "n";

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
    protected static final String URI_GROUP_NAME_LONG =     "uris";
    
    private static Options options;
    private static OutputStreamListener listener;

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
        options.addOption(new Option(URI_GROUP_NAME_OPT,
                                     URI_GROUP_NAME_LONG,
                                     true,
                                     "specify which <uris> element to process in the configuration"
                                     + " file specified with the -x parameter"));
    }

    /**
     * The <code>main</code> method.
     *
     * @param args a <code>String[]</code> of arguments
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {

        Main.setOptions();
        CommandLine line = new PosixParser().parse( options, args );
        listener = new OutputStreamListener(System.out);
        CocoonBean cocoon = new CocoonBean();
        cocoon.addListener(listener);

        if (line.hasOption(HELP_OPT)) {
             printUsage();
        } else if (line.hasOption(VERSION_OPT)) {
             printVersion();
        }

        String uriGroup = null;
        if (line.hasOption(URI_GROUP_NAME_OPT)) {
            uriGroup = line.getOptionValue(URI_GROUP_NAME_OPT);
        }
            
        String destDir = null;
        if (line.hasOption(XCONF_OPT)) {
            // destDir from command line overrides one in xconf file
            destDir = Main.processXConf(cocoon, line.getOptionValue(XCONF_OPT), destDir, uriGroup);
        }
        if (line.hasOption(DEST_DIR_OPT)) {
            destDir = line.getOptionValue(DEST_DIR_OPT);
        }

        if (line.hasOption(VERBOSE_OPT)) {
            cocoon.setVerbose(true);
        }
        if (line.hasOption(PRECOMPILE_ONLY_OPT)) {
            cocoon.setPrecompileOnly(true);
        }

        if (line.hasOption(WORK_DIR_OPT)) {
            String workDir = line.getOptionValue(WORK_DIR_OPT);
            if (workDir.equals("")) {
                listener.messageGenerated(
                    "Careful, you must specify a work dir when using the -w/--workDir argument");
                System.exit(1);
            } else {
                cocoon.setWorkDir(line.getOptionValue(WORK_DIR_OPT));
            }
        }
        if (line.hasOption(CONTEXT_DIR_OPT)) {
            String contextDir = line.getOptionValue(CONTEXT_DIR_OPT);
            if (contextDir.equals("")) {
                listener.messageGenerated(
                    "Careful, you must specify a configuration file when using the -c/--contextDir argument");
                System.exit(1);
            } else {  
                cocoon.setContextDir(contextDir);
            }
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
            listener.setReportFile(line.getOptionValue(BROKEN_LINK_FILE_OPT));
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
        if (line.hasOption(URI_FILE_OPT)) {
            cocoon.addTargets(BeanConfigurator.processURIFile(line.getOptionValue(URI_FILE_OPT)), destDir);
        }

        cocoon.addTargets(line.getArgList(), destDir);

        listener.messageGenerated(CocoonBean.getProlog());

        cocoon.initialize();
        cocoon.process();
        cocoon.dispose();

        listener.complete();


        int exitCode = (listener.isSuccessful() ? 0 : 1);
        System.exit(exitCode);
    }

    private static boolean yesno(String in) {
        return "yes".equals(in) || "true".equals(in);
    }

    private static String processXConf(CocoonBean cocoon, String filename, String destDir, String uriGroup) {

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document xconf = builder.parse(new File(filename).toURL().toExternalForm());
            return BeanConfigurator.configure(xconf, cocoon, destDir, uriGroup, listener);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return destDir;
        }

    }

    /**
     * Print the usage message and exit
     */
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("cocoon cli [options] [targets]",
                            CocoonBean.getProlog(),
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

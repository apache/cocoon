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

import org.apache.avalon.excalibur.cli.CLArgsParser;
import org.apache.avalon.excalibur.cli.CLOption;
import org.apache.avalon.excalibur.cli.CLOptionDescriptor;
import org.apache.avalon.excalibur.cli.CLUtil;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.NetUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.destination.FileDestination;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: Main.java,v 1.1 2003/03/09 00:08:36 pier Exp $
 */
public class Main {

    protected static final int HELP_OPT =         'h';
    protected static final int VERSION_OPT =      'v';
    protected static final int VERBOSE_OPT =      'V';
    protected static final int LOG_KIT_OPT =      'k';
    protected static final int LOGGER_OPT =       'l';
    protected static final int LOG_LEVEL_OPT =    'u';
    protected static final int CONTEXT_DIR_OPT =  'c';
    protected static final int DEST_DIR_OPT =     'd';
    protected static final int WORK_DIR_OPT =     'w';
    protected static final int AGENT_OPT =        'a';
    protected static final int ACCEPT_OPT =       'p';
    protected static final int URI_FILE =         'f';
    protected static final int FOLLOW_LINKS_OPT = 'r';
    protected static final int CONFIG_FILE =      'C';
    protected static final int BROKEN_LINK_FILE = 'b';
    protected static final int PRECOMPILE_OPT =   'P';

    protected static final CLOptionDescriptor [] OPTIONS = new CLOptionDescriptor [] {
        new CLOptionDescriptor("brokenLinkFile",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               BROKEN_LINK_FILE,
                               "send a list of broken links to a file (one URI per line)"),
        new CLOptionDescriptor("uriFile",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               URI_FILE,
                               "use a text file with uris to process (one URI per line)"),
        new CLOptionDescriptor("help",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               HELP_OPT,
                               "print this message and exit"),
        new CLOptionDescriptor("version",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               VERSION_OPT,
                               "print the version information and exit"),
        new CLOptionDescriptor("verbose",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               VERBOSE_OPT,
                               "enable verbose messages to System.out"),
        new CLOptionDescriptor("logKitconfig",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               LOG_KIT_OPT,
                               "use given file for LogKit Management configuration"),
        new CLOptionDescriptor("Logger",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               LOGGER_OPT,
                               "use given logger category as default logger for the Cocoon engine"),
        new CLOptionDescriptor("logLevel",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               LOG_LEVEL_OPT,
                               "choose the minimum log level for logging (DEBUG, INFO, WARN, ERROR, FATAL_ERROR) for startup logging"),
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
        new CLOptionDescriptor("precompileOnly",
                               CLOptionDescriptor.ARGUMENT_DISALLOWED,
                               PRECOMPILE_OPT,
                               "generate java code for xsp and xmap files"),
        new CLOptionDescriptor("userAgent",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               AGENT_OPT,
                               "use given string for user-agent header"),
        new CLOptionDescriptor("accept",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               ACCEPT_OPT,
                               "use given string for accept header"),
        new CLOptionDescriptor("followLinks",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               FOLLOW_LINKS_OPT,
                               "process pages linked from starting page or not"
                               + " (boolean argument is expected, default is true)"),
        new CLOptionDescriptor("configFile",
                               CLOptionDescriptor.ARGUMENT_REQUIRED,
                               CONFIG_FILE,
                               "specify alternate location of the configuration"
                               + " file (default is ${contextDir}/cocoon.xconf)")
    };

    /**
     * <code>processFile</code> method.
     *
     * @param filename a <code>String</code> value
     * @param uris a <code>List</code> of URIs
     */
    public static void processFile(String filename, List uris) {
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
     * The <code>main</code> method.
     *
     * @param args a <code>String[]</code> of arguments
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        String destDir = Constants.DEFAULT_DEST_DIR;
        String contextDir = Constants.DEFAULT_CONTEXT_DIR;
        String configFile = null;
        String brokenLinkFile = null;
        String workDir = Constants.DEFAULT_WORK_DIR;
        List targets = new ArrayList();
        String logKit = null;
        String logger = null;
        String logLevel = "ERROR";
        String agentOptions = null;
        String acceptOptions = null;
        boolean precompileOnly = false;
        boolean followLinks = true;
        boolean verbose = false;

        CLArgsParser parser = new CLArgsParser(args, OPTIONS);
        List clOptions = parser.getArguments();
        int size = clOptions.size();

        for (int i = 0; i < size; i++) {
            CLOption option = (CLOption) clOptions.get(i);

            switch (option.getId()) {
                case 0:
                    targets.add(NetUtils.normalize(option.getArgument()));
                    break;

                case Main.CONFIG_FILE:
                    configFile = option.getArgument();
                    break;

                case Main.HELP_OPT:
                    printUsage();
                    break;

                case Main.VERSION_OPT:
                    printVersion();
                    break;

                case Main.VERBOSE_OPT:
                    verbose = true;
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

                case Main.LOG_KIT_OPT:
                    logKit = option.getArgument();
                    break;

                case Main.LOGGER_OPT:
                    logger = option.getArgument();
                    break;

                case Main.LOG_LEVEL_OPT:
                    logLevel = option.getArgument();
                    break;

                case Main.PRECOMPILE_OPT:
                    precompileOnly = true;
                    break;

                case Main.AGENT_OPT:
                    agentOptions = option.getArgument();
                    break;

                case Main.ACCEPT_OPT:
                    acceptOptions = option.getArgument();
                    break;

                case Main.URI_FILE:
                    Main.processFile(option.getArgument(), targets);
                    break;

                case Main.FOLLOW_LINKS_OPT:
                    followLinks = "yes".equals(option.getArgument())
                        || "true".equals(option.getArgument());
                    break;

                case Main.BROKEN_LINK_FILE:
                    brokenLinkFile = option.getArgument();
                    break;
            }
        }

        CocoonBean cocoon = new CocoonBean(workDir, contextDir, configFile);
        cocoon.setLogKit(logKit);
        cocoon.setLogger(logger);
        cocoon.setLogLevel(logLevel);
        if (agentOptions != null) {
            cocoon.setAgentOptions(agentOptions);
        }
        if (acceptOptions != null) {
            cocoon.setAcceptOptions(acceptOptions);
        }
        cocoon.setBrokenLinkFile(brokenLinkFile);
        cocoon.setPrecompileOnly(precompileOnly);
        cocoon.setFollowLinks(followLinks);
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

        System.exit(0);
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
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append(getProlog());
        msg.append("Usage: java org.apache.cocoon.Main [options] [targets]").append(lSep).append(lSep);
        msg.append("Options: ").append(lSep);
        msg.append(CLUtil.describeOptions(Main.OPTIONS).toString());
        msg.append("Note: the context directory defaults to '").append(Constants.DEFAULT_CONTEXT_DIR + "'").append(lSep);
        System.out.println(msg.toString());
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

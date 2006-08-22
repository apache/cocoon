/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.bean;

import java.io.File;

import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.LinkSamplingEnvironment;
import org.apache.cocoon.util.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;


/**
 * This is simple Wrapper like CocoonWrapper and can only precompile all XSP in
 * the context-directory.
 *
 * @version $Id$ $Date: 2005/01/16 17:17:34 $
 */
public class XSPPrecompileWrapper extends CocoonWrapper {
    private SourceResolver sourceResolver;
    private static Options options;
    protected static final String HELP_OPT = "h";
    protected static final String LOG_KIT_OPT = "k";
    protected static final String CONTEXT_DIR_OPT = "c";
    protected static final String WORK_DIR_OPT = "w";
    protected static final String CONFIG_FILE_OPT = "C";
    protected static final String LOG_KIT_LONG = "logKitconfig";
    protected static final String CONTEXT_DIR_LONG = "contextDir";
    protected static final String WORK_DIR_LONG = "workDir";
    protected static final String HELP_LONG = "help";
    protected static final String CONFIG_FILE_LONG = "configFile";

    /**
     * Allow subclasses to recursively precompile XSPs.
     */
    public void precompile() throws Exception {
        recursivelyPrecompile(context, context);
    }

    /**
     * Recurse the directory hierarchy and process the XSP's.
     *
     * @param contextDir
     *            a <code>File</code> value for the context directory
     * @param file
     *            a <code>File</code> value for a single XSP file or a
     *            directory to scan recursively
     */
    private void recursivelyPrecompile(File contextDir, File file) throws Exception {
        if (file.isDirectory()) {
            String entries[] = file.list();
            for (int i = 0; i < entries.length; i++) {
                recursivelyPrecompile(contextDir, new File(file, entries[i]));
            }
        } else if (file.getName().toLowerCase().endsWith(".xsp")) {
            String contextFilePath = IOUtils.getContextFilePath(contextDir
                    .getCanonicalPath(), file.getCanonicalPath());
            this.processXSP(contextFilePath);
        }
    }

    /**
     * Process a single XSP file
     *
     * @param uri
     *            a <code>String</code> pointing to an xsp URI
     * @exception Exception
     *                if an error occurs
     */
    protected void processXSP(String uri) throws Exception {
        String markupLanguage = "xsp";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, null,
                null, null, cliContext, log);
        precompile(uri, env, markupLanguage, programmingLanguage);
    }

    /**
     * Process a single XMAP file
     *
     * @param uri
     *            a <code>String</code> pointing to an xmap URI
     * @exception Exception
     *                if an error occurs
     */
    protected void processXMAP(String uri) throws Exception {
        String markupLanguage = "sitemap";
        String programmingLanguage = "java";
        Environment env = new LinkSamplingEnvironment("/", context, null,
                null, null, cliContext, log);
        precompile(uri, env, markupLanguage, programmingLanguage);
    }

    /**
     * Process the given <code>Environment</code> to generate Java code for
     * specified XSP files.
     *
     * @param fileName
     *            a <code>String</code> value
     * @param environment
     *            an <code>Environment</code> value
     * @exception Exception
     *                if an error occurs
     */
    public void precompile(String fileName, Environment environment,
            String markupLanguage, String programmingLanguage) throws Exception {

        ProgramGenerator programGenerator = null;
        Source source = null;
        Object key = CocoonComponentManager.startProcessing(environment);
        CocoonComponentManager.enterEnvironment(environment,
                getComponentManager(), cocoon);
        try {
            if (log.isDebugEnabled()) {
                log.debug("XSP generation begin:" + fileName);
            }
            System.out.println("Compiling " + fileName);

            programGenerator = (ProgramGenerator) getComponentManager().lookup(
                    ProgramGenerator.ROLE);
            source = sourceResolver.resolveURI(fileName);
            CompiledComponent xsp = programGenerator.load(
                    getComponentManager(), source, markupLanguage,
                    programmingLanguage, environment);
            System.out.println("[XSP generated] " + xsp);
            if (log.isDebugEnabled()) {
                log.debug("XSP generation complete:" + xsp);

            }
        } finally {
            sourceResolver.release(source);
            getComponentManager().release(programGenerator);

            CocoonComponentManager.leaveEnvironment();
            CocoonComponentManager.endProcessing(environment, key);
        }
    }

    public static void main(String[] args) throws Exception {

        XSPPrecompileWrapper.setOptions();
        CommandLine line = new PosixParser().parse(options, args);
        XSPPrecompileWrapper wrapper = new XSPPrecompileWrapper();
        if (line.hasOption(HELP_OPT)) {
            printUsage();
        }

        if (line.hasOption(WORK_DIR_OPT)) {
            String workDir = line.getOptionValue(WORK_DIR_OPT);
            if (workDir.equals("")) {
                System.exit(1);
            } else {
                wrapper.setWorkDir(line.getOptionValue(WORK_DIR_OPT));
            }
        }

        if (line.hasOption(CONTEXT_DIR_OPT)) {
            String contextDir = line.getOptionValue(CONTEXT_DIR_OPT);
            if (contextDir.equals("")) {

                System.exit(1);
            } else {
                wrapper.setContextDir(contextDir);
            }
        }
        if (line.hasOption(LOG_KIT_OPT)) {
            wrapper.setLogKit(line.getOptionValue(LOG_KIT_OPT));
        }

        if (line.hasOption(CONFIG_FILE_OPT)) {
            wrapper.setConfigFile(line.getOptionValue(CONFIG_FILE_OPT));
        }
        wrapper.initialize();
        wrapper.precompile();
        wrapper.dispose();
        System.exit(0);
    }

    private static void setOptions() {
        options = new Options();

        options.addOption(new Option(LOG_KIT_OPT, LOG_KIT_LONG, true,
                "use given file for LogKit Management configuration"));

        options.addOption(new Option(CONTEXT_DIR_OPT, CONTEXT_DIR_LONG, true,
                "use given dir as context"));
        options.addOption(new Option(WORK_DIR_OPT, WORK_DIR_LONG, true,
                "use given dir as working directory"));

        options.addOption(new Option(HELP_OPT, HELP_LONG, false,
                "print this message and exit"));

        options.addOption(new Option(CONFIG_FILE_OPT, CONFIG_FILE_LONG, true,
                "specify alternate location of the configuration"
                        + " file (default is ${contextDir}/cocoon.xconf)"));
    }

    /**
     * Print the usage message and exit
     */
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp(
                "java org.apache.cocoon.bean.XSPPrecompileWrapper [options] ",

                options);
        System.exit(0);
    }

    public void initialize() throws Exception {
        super.initialize();
        sourceResolver = (SourceResolver) getComponentManager().lookup(SourceResolver.ROLE);
    }
}

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.generation;

// Cocoon imports

import org.apache.cocoon.Roles;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;

// Avalon imports

import org.apache.avalon.component.Component;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

// Java runtime imports

import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileNotFoundException;

// BSF imports

import com.ibm.bsf.BSFManager;
import com.ibm.bsf.util.IOUtils;
import com.ibm.bsf.BSFException;

// SAX imports

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The Scriptgenerator executes arbitraty scripts using the BSF framework
 * and additional interpreter (Rhino, Jython, etc.) as a Cocoon Generator
 *
 * @author <a href="mailto:jafoster@engmail.uwaterloo.ca">Jason Foster</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-04-20 20:50:07 $
 */
public class ScriptGenerator extends ComposerGenerator {

    public void configure(Configuration conf) throws ConfigurationException {
        // TODO: figure out what configure might be good for
        // void configure
    }

    public void generate() throws ProcessingException {
        Parser parser = null;
        try {
            // Figure out what file to open and do so

            getLogger().debug("processing file [" + this.source + "]");

            InputSource src = this.resolver.resolveEntity(null, this.source);
            String systemID = src.getSystemId();
            String fileName = systemID.substring("file:".length());

            getLogger().debug("file resolved to [" + systemID + "]");
            getLogger().debug("file name extracted as [" + fileName + "]");

            // TODO: why doesn't this work?
            // Reader in = src.getCharacterStream();

            Reader in = new FileReader(fileName);

            // Set up the BSF manager and register relevant helper "beans"

            BSFManager mgr = new BSFManager();
            StringBuffer output = new StringBuffer();

            mgr.registerBean("resolver", this.resolver);
            mgr.registerBean("source", this.source);
            mgr.registerBean("objectModel", this.objectModel);
            mgr.registerBean("parameters", this.parameters);
            mgr.registerBean("output", output);
            mgr.registerBean("logger", getLogger());

            getLogger().debug("BSFManager execution begining");

            // Execute the script

            mgr.exec(BSFManager.getLangFromFilename(fileName), fileName, 0, 0,
                    IOUtils.getStringFromReader(in));

            getLogger().debug("BSFManager execution complete");
            getLogger().debug("output = [" + output.toString() + "]");

            // Extract the XML string from the BSFManager and parse it

            InputSource xmlInput =
                    new InputSource(new StringReader(output.toString()));
            parser = (Parser)(this.manager.lookup(Roles.PARSER));
            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(xmlInput);
        } catch (FileNotFoundException e) {
            throw new ProcessingException(
                    "Exception in ScriptGenerator.generate()", e);
        } catch (BSFException e) {
            throw new ProcessingException(
                    "Exception in ScriptGenerator.generate()", e);
        } catch (Exception e) {
            throw new ProcessingException(
                    "Exception in ScriptGenerator.generate()", e);
        } finally {
            if (parser != null) this.manager.release((Component)parser);
        }
    }
}

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import org.apache.avalon.Component;
import org.apache.avalon.Poolable;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.net.URL;
import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.Properties;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

/**
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-12 12:30:34 $
 */
public class VelocityGenerator extends ServletGenerator implements Poolable {

    /** Flag for checking initialization */
    private static boolean initVelocity = false;

    /**
     * Generate XML data using Velocity template.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        Parser parser = null;
        try
        {
            parser = (Parser)(this.manager.lookup(Roles.PARSER));
            getLogger().debug("Processing File :" + super.source);

            /* first, we init the runtime engine.  Defaults are fine. */
            if(initVelocity == false)
            {
                initVelocity = true;

                String templatePath = ((org.apache.cocoon.environment.http.HttpContext)super.context).getRealPath("/templates");
                getLogger().debug("Templates Directory:" + templatePath);

                Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templatePath);
                Velocity.init();
            }

            /* lets make a Context and put data into it */
            VelocityContext context = new VelocityContext();

            context.put("name", "Velocity Generator");
            context.put("project", "Cocoon2");
            context.put("request", this.request);
            context.put("response", this.response);
            context.put("context", this.context);

            /* lets render a template */
            StringWriter w = new StringWriter();
            Velocity.mergeTemplate(super.source, context, w );
    
            InputSource xmlInput = 
                    new InputSource(new StringReader(w.toString()));
            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(xmlInput);
        } catch (IOException e){
            e.printStackTrace();
            getLogger().warn("VelocityGenerator.generate()", e);
            throw new ResourceNotFoundException("Could not get Resource for VelocityGenerator", e);
        } catch (SAXException e){
            e.printStackTrace();
            getLogger().error("VelocityGenerator.generate()", e);
            throw(e);
        } catch (org.apache.avalon.ComponentManagerException e){
            e.printStackTrace();
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()",e);
        } catch (Exception e){
            e.printStackTrace();
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in VelocityGenerator.generate()",e);
        } finally {
            if (parser != null) this.manager.release((Component) parser);
        }
    }
}

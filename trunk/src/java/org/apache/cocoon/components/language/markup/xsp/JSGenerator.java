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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Class representing interpreted XSP-generated
 * <code>ServerPagesGenerator</code> programs
 * written in Javascript language
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: JSGenerator.java,v 1.3 2003/12/06 21:22:09 cziegeler Exp $
 */
public class JSGenerator extends XSPGenerator
        implements Configurable, Initializable {

    /**
     * Javascript source file
     */
    private File file;

    private Scriptable global;

    // FIXME: Use Store to cache compiled scripts
    private Script script;
    private Exception compileError;


    public void configure(Configuration configuration) throws ConfigurationException {
        this.file = new File(configuration.getChild("file").getValue());

        Configuration[] dependencies = configuration.getChildren("dependency");
        this.dependencies = new File[dependencies.length];
        for (int i = 0; i < dependencies.length; i ++) {
            this.dependencies[i] = new File(dependencies[i].getValue());
        }
    }

    /**
     * Determines whether this generator's source files have changed
     *
     * @return Whether any of the files this generator depends on has changed
     * since it was created
     */
    public boolean modifiedSince(long date) {
        if (this.file.lastModified() < date) {
            return true;
        }

        for (int i = 0; i < dependencies.length; i++) {
            if (this.file.lastModified() < dependencies[i].lastModified()) {
                return true;
            }
        }

        return false;
    }

    public void initialize() throws Exception {
        Context context = Context.enter();
        try {
            global = new ImporterTopLevel(context);
            global.put("page", global, Context.toObject(this, global));
            global.put("logger", global, Context.toObject(getLogger(), global));
            global.put("xspAttr", global, Context.toObject(new AttributesImpl(), global));

            context.setOptimizationLevel(-1);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Compiling script " + file);
            }
            script = context.compileReader(global, new FileReader(file), file.toString(), 1, null);
        } catch (Exception e) {
            compileError = e;
        } finally {
            Context.exit();
        }
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        if (compileError != null) {
            throw new ProcessingException("Failed to compile script", compileError);
        }

        // add enter/exit here, too
        Context.enter();
        try {
            global.put("objectModel", global, Context.toObject(this.objectModel, global));
            global.put("request", global, Context.toObject(this.request, global));
            global.put("response", global, Context.toObject(this.response, global));
            global.put("context", global, Context.toObject(this.context, global));
            global.put("resolver", global, Context.toObject(this.resolver, global));
            global.put("parameters", global, Context.toObject(this.parameters, global));
        } catch (Exception e) {
            throw new ProcessingException("setup: Got exception", e);
        } finally {
            Context.exit();
        }
    }

    public void generate() throws IOException, ProcessingException {
        Context context = Context.enter();
        try {
            global.put("contentHandler", global, Context.toObject(this.contentHandler, global));

            context.setOptimizationLevel(-1);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing script " + file);
            }

            script.exec(context, global);
        } catch (JavaScriptException e) {
            throw new ProcessingException("generate: Got Javascript exception", e);
        } finally {
            Context.exit();
        }
    }

    public void recycle() {
        global.delete("contentHandler");

        global.delete("objectModel");
        global.delete("request");
        global.delete("response");
        global.delete("context");
        global.delete("resolver");
        global.delete("parameters");

        super.recycle();
    }

    public void dispose() {
        global.delete("page");
        global.delete("logger");
        global.delete("xspAttr");
        this.global = null;
        this.script = null;
        this.compileError = null;

        super.dispose();
    }

    // XSPRequestHelper

    public void getLocale() throws SAXException {
        XSPRequestHelper.getLocale(this.objectModel, this.contentHandler);
    }

    public Locale[] getLocalesAsArray() {
        return XSPRequestHelper.getLocales(this.objectModel);
    }

    public void getLocalesAsXML() throws SAXException {
        XSPRequestHelper.getLocale(this.objectModel, this.contentHandler);
    }

    public String getParameter(String name, String defaultValue) {
        return XSPRequestHelper.getParameter(this.objectModel, name, defaultValue);
    }

    public String getParameter(String name, String defaultValue,
                               String form_encoding, String container_encoding) {
        return XSPRequestHelper.getParameter(this.objectModel, name, defaultValue,
                form_encoding, container_encoding);
    }

    public void getParameterAsXML(String name, String defaultValue,
                                  String form_encoding, String container_encoding)
            throws SAXException {
        XSPRequestHelper.getParameter(this.objectModel, this.contentHandler, name, defaultValue,
                form_encoding, container_encoding);
    }

    public void getParameterValuesAsXML(String name, String form_encoding,
                                        String container_encoding)
            throws SAXException {
        XSPRequestHelper.getParameterValues(this.objectModel, this.contentHandler,
                name, form_encoding, container_encoding);
    }

    public String[] getParameterValues(String name,
                                       String form_encoding,
                                       String container_encoding) {
        return XSPRequestHelper.getParameterValues(this.objectModel,
                name, form_encoding, container_encoding);
    }

    public String[] getParameterNames() {
        return XSPRequestHelper.getParameterNames(this.objectModel);
    }

    public void getParameterNamesAsXML() throws SAXException {
        XSPRequestHelper.getParameterNames(this.objectModel, this.contentHandler);
    }

    public void getHeaderNamesAsXML() throws SAXException {
        XSPRequestHelper.getHeaderNames(this.objectModel, this.contentHandler);
    }

    public String[] getHeaderNames() {
        return XSPRequestHelper.getHeaderNames(this.objectModel);
    }

    public String[] getHeaders(String name) {
        return XSPRequestHelper.getHeaders(this.objectModel, name);
    }

    public void getHeadersAsXML(String name)
            throws SAXException {
        XSPRequestHelper.getHeaders(this.objectModel, name, this.contentHandler);
    }

    public Date getDateHeader(String name) {
        return XSPRequestHelper.getDateHeader(this.objectModel, name);
    }

    public String getDateHeader(String name, String format) {
        return XSPRequestHelper.getDateHeader(this.objectModel, name, format);
    }

    public void getAttributeNames(ContentHandler contentHandler)
            throws SAXException {
        XSPRequestHelper.getAttributeNames(this.objectModel, contentHandler);
    }

    public String[] getAttributeNames() {
        return XSPRequestHelper.getAttributeNames(this.objectModel);
    }

    public String getRequestedURL() {
        return XSPRequestHelper.getRequestedURL(this.objectModel);
    }

    // XSPResponseHelper

    public void responseGetLocale()
            throws SAXException {
        XSPResponseHelper.getLocale(this.response, this.contentHandler);
    }

    public void addDateHeader(String name, long date) {
        XSPResponseHelper.addDateHeader(this.response, name, date);
    }

    public void addDateHeader(String name, Date date) {
        XSPResponseHelper.addDateHeader(this.response, name, date);
    }

    public void addDateHeader(String name, String date) throws ParseException {
        XSPResponseHelper.addDateHeader(this.response, name, date);
    }

    public void addDateHeader(String name, String date, String format) throws ParseException {
        XSPResponseHelper.addDateHeader(this.response, name, date, format);
    }

    public void addDateHeader(String name, String date, DateFormat format) throws ParseException {
        XSPResponseHelper.addDateHeader(this.response, name, date, format);
    }

    public void setDateHeader(String name, long date) {
        XSPResponseHelper.setDateHeader(this.response, name, date);
    }

    public void setDateHeader(String name, Date date) {
        XSPResponseHelper.setDateHeader(this.response, name, date);
    }

    public void setDateHeader(String name, String date) throws ParseException {
        XSPResponseHelper.setDateHeader(this.response, name, date);
    }

    public void setDateHeader(String name, String date, String format) throws ParseException {
        XSPResponseHelper.setDateHeader(this.response, name, date, format);
    }

    public void setDateHeader(String name, String date, DateFormat format) throws ParseException {
        XSPResponseHelper.setDateHeader(this.response, name, date, format);
    }

    // XSPSessionHelper
    public Object getSessionAttribute(Session session, String name, Object defaultValue) {
        return XSPSessionHelper.getSessionAttribute(session, name, defaultValue);
    }

    public String[] getSessionAttributeNames(Session session) {
        Collection c = XSPSessionHelper.getSessionAttributeNames(session);
        return (String[])c.toArray(new String[c.size()]);
    }
}

/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version CVS $Id: JSGenerator.java,v 1.2 2004/03/05 13:02:47 bdelacretaz Exp $
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

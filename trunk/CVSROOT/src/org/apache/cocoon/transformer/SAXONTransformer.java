/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.cocoon.transformer;

import java.util.Dictionary;
import java.util.Enumeration;

import org.w3c.dom.Document;

import org.apache.cocoon.Defaults;
import org.apache.cocoon.logger.Logger;
import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.Director;
import org.apache.cocoon.framework.Status;
import org.apache.cocoon.framework.AbstractActor;
import org.apache.cocoon.store.Store;

import javax.xml.transform.Templates;
// could be imported because of naming conflicts
//import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;

// Saxon stuff
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.FeatureKeys;
import com.icl.saxon.Controller;
import com.icl.saxon.Builder;
import com.icl.saxon.trace.SimpleTraceListener;

/**
 * This class implements the transformer interface for the 
 * <a href="http://users.iclway.co.uk/mhkay/saxon/">SAXON XSLT processor</a> 
 * developed by Michael Kay.
 * This transformer is based on the TRaXTransformer by 
 * <a href="http:infozone-group.
 *
 * @author <a href="http://www.smb-tec.com/">SMB</a>
 * @version $Revision: 1.1 $Date: 2001/01/15 12:06:58 $
 */
public class SAXONTransformer extends AbstractActor implements Transformer, 
        Status, Configurable {

    private final static TransformerFactoryImpl _factory 
            = new TransformerFactoryImpl();

    private final static String PREFIX = "[SAXON]";
    
    private Store store;
    private Logger logger;


    public void init(Director director) {
        super.init(director);        

        this.store = (Store) director.getActor("store");
        this.logger = (Logger) director.getActor("logger");               
    }


    public void init(Configurations conf) {
        Configurations attributes = conf.getConfigurations("factory");
        
        Object value = attributes.get("timing");
        if (value != null) {
            _factory.setAttribute(FeatureKeys.TIMING, 
                new Boolean(value.toString()));            
        }    

        value = attributes.get("linenumbering");
        if (value != null) {
            _factory.setAttribute(FeatureKeys.LINE_NUMBERING, 
                new Boolean(value.toString()));            
        }    

        value = attributes.get("trace");
        if (value != null) {
            _factory.setAttribute(FeatureKeys.TRACE_LISTENER, 
                new SimpleTraceListener());            
        }    

        value = attributes.get("treeModel");
        if (value != null) {
            if (value.equals("TINY_TREE")) {
                _factory.setAttribute(FeatureKeys.TREE_MODEL, 
                    new Integer(Builder.TINY_TREE));
            } else {
                _factory.setAttribute(FeatureKeys.TREE_MODEL, 
                    new Integer(Builder.STANDARD_TREE));
            }            
        }    

        value = attributes.get("sourceParserClass");
        if (value != null) {
            _factory.setAttribute(FeatureKeys.SOURCE_PARSER_CLASS, 
                value.toString());
        }    

        value = attributes.get("styleParserClass");
        if (value != null) {
            _factory.setAttribute(FeatureKeys.STYLE_PARSER_CLASS, 
                value.toString());
        }    

        value = attributes.get("recoveryPolicy");
        if (value != null) {
            if (value.equals("RECOVER_SILENTLY")) {
                _factory.setAttribute(FeatureKeys.RECOVERY_POLICY, 
                    new Integer(Controller.RECOVER_SILENTLY));
            } else if (value.equals("RECOVER_WITH_WARNINGS")) {
                _factory.setAttribute(FeatureKeys.RECOVERY_POLICY, 
                    new Integer(Controller.RECOVER_WITH_WARNINGS));
            } else {
                _factory.setAttribute(FeatureKeys.RECOVERY_POLICY, 
                    new Integer(Controller.DO_NOT_RECOVER));
            }            
        }    
    }
        
    /** 
     * @param in The initial document.
     * @param inBase Path to current document, received by the producer.
     * @param sheet The current stylesheet, received by the 
     *       XSLTProcessor.getStylesheet().
     * @param sheetBase Path to the stylesheet, e.g. a File.toString() 
     *       or URL.toString(), @see XSLTProcessor.getResource()
     * @param out Empty document, that should also be returned.
     * @param params Some useful parameters in a hashtable.
     * @return The document, passed as fifth parameter 'out'.
     */
    public Document transform(Document in, String inBase, Document sheet,
            String sheetBase, Document out, Dictionary params) 
            throws Exception {

        javax.xml.transform.Transformer transformer = getTransformer(sheet,
                sheetBase);

        transformer.clearParameters();
        
        for (Enumeration enum = params.keys(); enum.hasMoreElements(); ) {
            Object name = enum.nextElement();
            Object value = params.get(name);
            transformer.setParameter(name.toString() , value);
        }

        DOMSource i = new DOMSource(in, inBase);
        DOMResult o = new DOMResult(out);

        logger.log(this, "transform(): transforming " + inBase, Logger.DEBUG);
        
        transformer.transform(i, o);

        return out;
    }

    public String getStatus() {
        StringBuffer ret = new StringBuffer("<strong>SAXON XSLT Transformer</strong> by ");
        ret.append("<a href=\"http://www.infozone-group.org\">infozone group</a><br>"); 
        ret.append("Attributes of ").append(_factory.getClass().getName()).append(":<br>");
        ret.append("<li><b>timing</b>: ").append(_factory.getAttribute(FeatureKeys.TIMING)).append("</li>");
        ret.append("<li><b>trace listener</b>: ");
        Object value = _factory.getAttribute(FeatureKeys.TRACE_LISTENER);
        if (value == null) {
            ret.append("NONE");
        } else {
            ret.append(value.getClass().getName());            
        }        
        ret.append("</li><li><b>linenumbering</b>: ").append(_factory.getAttribute(FeatureKeys.LINE_NUMBERING));
        ret.append("</li><li><b>sourceParserClass</b>: ");
        value =  _factory.getAttribute(FeatureKeys.SOURCE_PARSER_CLASS);
        if (value == null) {
            ret.append("com.icl.saxon.aelfred.SAXDriver");
        } else {
            ret.append(value);
        }        
        ret.append("</li><li><b>styleParserClass</b>: "); 
        value =  _factory.getAttribute(FeatureKeys.STYLE_PARSER_CLASS);
        if (value == null) {
            ret.append("com.icl.saxon.aelfred.SAXDriver");
        } else {
            ret.append(value);
        }        
        ret.append("</li><li><b>treeModel</b>: ");
        value = _factory.getAttribute(FeatureKeys.TREE_MODEL);
        switch (((Integer)value).intValue()) {
        case Builder.TINY_TREE:
            ret.append("TINY_TREE");
            break;
        case Builder.STANDARD_TREE:
            ret.append("STANDARD_TREE");
            break;
        default: 
            ret.append("UNSPECIFIED");
        }    
        ret.append("</li><li><b>recoveryPolicy</b>: ");
        value = _factory.getAttribute(FeatureKeys.RECOVERY_POLICY);
        switch (((Integer)value).intValue()) {
        case Controller.RECOVER_WITH_WARNINGS:
            ret.append("RECOVER_WITH_WARNINGS");
            break;
        case Controller.RECOVER_SILENTLY:
            ret.append("RECOVER_SILENTLY");
            break;
        case Controller.DO_NOT_RECOVER:
            ret.append("DO_NOT_RECOVER");
            break;
        default: 
            ret.append("UNSPECIFIED");
        }    
        ret.append("</li>");        
        return ret.toString();
    }


    /**
     * Implements the cache based on a simple HashMap with the 
     * sheet as key.
     *
     * @param sheet The stylesheet Node for the Transformer.
     * @param sheetBase The URI for the actual stylesheet.
     * @return A Transformer for the specified sheetBase.
     * @throws javax.xml.transform.TransformerConfigurationException
     */
    private synchronized javax.xml.transform.Transformer 
            getTransformer(Document sheet, String sheetBase) throws Exception {

        if (sheet == null) {
            return _factory.newTransformer();
        }    

        if (store.containsKey(sheet)) {

            return ((Templates)store.get(sheet)).newTransformer();
        }           

        logger.log(this, "Loading transformer for stylesheet: " 
                + sheetBase, Logger.DEBUG);

        // remove an old version of the document
        Object old = store.get(PREFIX + sheetBase);

        if (old == null) {
            store.hold(PREFIX + sheetBase, sheet);
        } else if (old != sheet) {
            store.hold(PREFIX + sheetBase, sheet);
            store.remove(old);
        }          
        
        DOMSource source = (sheetBase == null)
                ?new DOMSource(sheet)
                :new DOMSource(sheet, sheetBase);     

        Templates templates = _factory.newTemplates(source);

        store.hold(sheet, templates);     

        return templates.newTransformer();  
    }    
}

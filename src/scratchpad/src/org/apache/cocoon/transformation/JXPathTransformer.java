/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.Generator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.Variables;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
/**
 * JXPath Transformer
 *
 * <p>
 *  Transformer implementation using Apache JXPath
 * </p>
 * <p>
 *  Provides a tag library and embedded XPath expression substitution
 *  to access data sent by the Cocoon flow layer
 * </p>
 *
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:coliver@apache.org">Christopher Oliver</a>
 */
public class JXPathTransformer
extends AbstractSAXTransformer implements Initializable, Generator {

    private static final JXPathContextFactory 
        jxpathContextFactory = JXPathContextFactory.newInstance();

    public static final String JXPATH_NAMESPACE_URI  = 
	"http://cocoon.apache.org/transformation/jxpath/1.0";
    public static final String JXPATH_FOR_EACH         = "for-each";
    public static final String JXPATH_CHOOSE         = "choose";
    public static final String JXPATH_WHEN         = "when";
    public static final String JXPATH_OTHERWISE         = "otherwise";
    public static final String JXPATH_VALUEOF        = "value-of";
    public static final String JXPATH_VALUEOF_SELECT = "select";
    public static final String JXPATH_CONTINUATION   = "continuation";
    public static final String JXPATH_CONTINUATION_SELECT = "select";
    public static final String JXPATH_IF             = "if";
    public static final String JXPATH_IF_TEST           = "test";
    public static final String JXPATH_WHEN_TEST           = "test";

    // web contination
    private WebContinuation kont;

    // TBD: Don't really need stacks for these in current implementation of for-each
    private Stack foreachStack = new Stack(); 
    // Stack of JXPathContext's 
    private Stack contextStack;

    //
    // Contains a stack of Boolean values:
    // Each time we enter a <choose> we push TRUE on this stack:
    // which indicates that the <otherwise> clause should be executed.
    // When we enter a <when> clause whose test condition is true, this value is 
    // popped and FALSE is pushed in its place. The test condition is then also pushed
    // on the stack, and popped when we reach </when>, where it is checked
    // to see if ignoreEventsCount should be updated
    // 
    private Stack chooseStack;

    // 
    // Marker set when we enter <choose>: used to validate that <when> and
    // <otherwise> are always directly nested in <choose>
    // 
    //
    private boolean inChoose;

    // Contains a stack of Boolean values:
    // Each time we enter an <if> we push the test condition on this stack and
    // pop it when we reach </if>, where it is checked to see if ignoreEventsCount
    // should be updated
    private Stack ifStack;


    // Run as a generator for debugging: to get line numbers in error messages

    private Source inputSource;

    public void generate()
        throws IOException, SAXException, ProcessingException {
	try {
            this.resolver.toSAX(this.inputSource, this);
        } catch (SAXException e) {
            final Exception cause = e.getException();
            if( cause != null ) {
                if ( cause instanceof ProcessingException )
                    throw (ProcessingException)cause;
                if ( cause instanceof IOException )
                    throw (IOException)cause;
                if ( cause instanceof SAXException )
                    throw (SAXException)cause;
                throw new ProcessingException("Could not read resource "
                                              + this.inputSource.getURI(), cause);
            }
            throw e;
        }
    }


    /**
     * Initialize this transformer.
     *
     * @exception Exception if an error occurs
     */
    public void initialize()
        throws Exception {
        namespaceURI = JXPATH_NAMESPACE_URI;
    }

    /**
     * Setup this transformer
     *
     * @param resolver a {@link SourceResolver} instance
     * @param objectModel the objectModel
     * @param src <code>src</code> parameter
     * @param parameters optional parameters
     * @exception ProcessingException if an error occurs
     * @exception SAXException if an error occurs
     * @exception IOException if an error occurs
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);
	if (src != null) {
	    try {
		this.inputSource = resolver.resolveURI(src);
	    } catch (SourceException se) {
		throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
	    }
	}
        // setup the jxpath transformer for this thread
        // FIX ME: When we decide proper way to pass "bean" and "kont"
        Object bean = ((Environment)resolver).getAttribute("bean-dict");
        kont = (WebContinuation)((Environment)resolver).getAttribute("kont");
        foreachStack = new Stack();
        contextStack = new Stack();
        chooseStack = new Stack();
        ifStack = new Stack();
        inChoose = false;
        pushContext(bean);
    }

    /**
     * Hack? Accept JXPath expr with or without enclosing {}
     */

    String getExpr(String inStr) {
        try {
	    inStr = inStr.trim();
	    if (inStr.length() == 0 || inStr.charAt(0) != '{') {
		return inStr;
	    }
            StringReader in = new StringReader(inStr);
            int ch;
            StringBuffer expr = new StringBuffer();
	    in.read(); // '{'
            while ((ch = in.read()) != -1) {
                char c = (char)ch;
		if (c == '}') {
		    break;
		} else if (c == '\\') {
		    ch = in.read();
		    if (ch == -1) {
			expr.append('\\');
		    } else {
			expr.append((char)ch);
		    }
		} else {
		    expr.append(c);
		}
	    } 
	    return expr.toString();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
	return inStr;
    }

    /**
     * Substitute the values of XPath expr's (contained in {}) within attribute values
     * TBD: how should escaping of {} be done? (uses backslash at the moment)
     */

    private void substitute(Reader in, Writer out) throws Exception {
        int ch;
        StringBuffer expr = new StringBuffer();
        boolean inExpr = false;
        while ((ch = in.read()) != -1) {
            char c = (char)ch;
            if (inExpr) {
                if (c == '}') {
                    String str = expr.toString();
                    expr.setLength(0);
                    str = String.valueOf(getValue(str));
                    out.write(str);
                    inExpr = false;
                } else if (c == '\\') {
                    ch = in.read();
                    if (ch == -1) {
                        expr.append('\\');
                    } else {
                        expr.append((char)ch);
                    }
                } else {
                    expr.append(c);
                }
            } else {
                if (c == '\\') {
                    ch = in.read();
                    if (ch == -1) {
                        out.write('\\');
                    } else {
                        out.write((char)ch);
                    }
                } else {
                    if (c == '{') {
                        ch = in.read();
                        if (ch != -1) {
                            expr.append((char)ch);
                            inExpr = true;
                            continue;
                        }
                        out.write('{');
                    }
                    if (ch != -1) {
                        out.write((char)ch);
                    }
                }
            }
        }
    }

    public void startElement(String uri, String name, 
                             String raw, Attributes attr) 
        throws SAXException {
        if (ignoreEventsCount == 0 && foreachStack.size() == 0) {
            if (!uri.equals(JXPATH_NAMESPACE_URI)) {
                // substitute xpath expressions contained in {}
                AttributesImpl impl = new AttributesImpl(attr);
                for (int i = 0, len = impl.getLength(); i < len; i++) {
                    String value = impl.getValue(i);
                    StringReader reader = new StringReader(value);
                    StringWriter writer = new StringWriter();
                    try {
                        substitute(reader, writer);
                    } catch (Exception exc) {
                        throw new SAXException(exc.getMessage(), exc);
                    }
                    impl.setValue(i, writer.toString());
                }
                attr = impl;
            }
        }
        super.startElement(uri, name, raw, attr);
    }

    /**
     * Entry method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @param attr an <code>Attributes</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void startTransformingElement(String uri, String name, 
                                         String raw, Attributes attr) 
        throws ProcessingException ,IOException, SAXException {
        if (foreachStack.size() > 0) {
            // just record the SAX event
            ignoreHooksCount++;
            super.startElement(uri, name, raw, attr);
            ignoreHooksCount--;
            return;
        }
        if (JXPATH_VALUEOF.equals(name)) {
            if (ignoreEventsCount == 0) {
                doValueOf(attr);
            }
        } else if (JXPATH_CONTINUATION.equals(name)) {
            if (ignoreEventsCount == 0) {
                doContinuation(attr);
            } 
        } else if (JXPATH_IF.equals(name)) {
            doIf(attr);
        } else if (JXPATH_FOR_EACH.equals(name)) {
            doForEach(attr);
        } else if (JXPATH_CHOOSE.equals(name)) {
            inChoose = true;
            doChoose(attr);
            return;
        } else if (JXPATH_WHEN.equals(name)) {
            if (!inChoose) {
                throw new ProcessingException("<when> must be contained in <choose>");
            }
            doWhen(attr);
        } else if (JXPATH_OTHERWISE.equals(name)) {
            if (!inChoose) {
                throw new ProcessingException("<otherwise> must be contained in <choose>");
            }
            doOtherwise(attr);
        } else {
            throw new ProcessingException("unknown jxpath element: " + name);
        }
        inChoose = false;
    }

    /**
     * Exit method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void endTransformingElement(String uri, String name, String raw) 
        throws ProcessingException, IOException, SAXException {
        if (JXPATH_FOR_EACH.equals(name)) {
            finishForEach();
        } else if (foreachStack.size() > 0) {
            // just record the SAX event
            ignoreHooksCount++;
            super.endElement(uri, name, raw);
            ignoreHooksCount--;
        } else {
            if (JXPATH_VALUEOF.equals(name) ||
                JXPATH_CONTINUATION.equals(name)) {
                return; // do nothing
            } else if (JXPATH_IF.equals(name)) {
                finishIf();
            } else if (JXPATH_CHOOSE.equals(name)) {
                finishChoose();
                inChoose = false;
            } else if (JXPATH_WHEN.equals(name)) {
                finishWhen();
                inChoose = true;
            } else if (JXPATH_OTHERWISE.equals(name)) {
                finishOtherwise();
                inChoose = true;
            }
        }
    }

    private JXPathContext getContext() {
        return (JXPathContext)contextStack.peek();
    }

    private void pushContext(Object contextObject) {
        JXPathContext ctx = 
            jxpathContextFactory.newContext(null, contextObject);
        // Make web continuation available as an XPath variable 
        // you would typically access its id:
        // 
        //   <form action="kont/{$continuation/id}" ...
        //
        // and you can get previous continuations like this:
        //
        //  <form action="kont/{getContinuation($continuation, 1)/id}" ...
        //
        ctx.setVariables(new Variables() {

                public boolean isDeclaredVariable(String varName) {
                    return varName.equals("continuation");
                }

                public Object getVariable(String varName) {
                    if (varName.equals("continuation")) {
                        return kont;
                    }
		    return null;
                }

                public void declareVariable(String varName, Object value) {
                }

                public void undeclareVariable(String varName) {
                }
            });
        contextStack.push(ctx);
    }

    private void popContext() {
        contextStack.pop();
    }

    /**
     * Helper method for obtaining the value of a particular variable.
     *
     * @param variable variable name
     * @return variable value as an <code>Object</code>
     */
    private Object getValue(final String variable) {
        return getContext().getValue(variable);
    }

    /**
     * Helper method to process a &lt;jxpath:value-of select="."&gt; tag
     *
     * @param a an {@link Attributes} instance
     * @exception SAXException if a SAX error occurs
     * @exception ProcessingException if a processing error occurs
     */
    private void doValueOf(final Attributes a)
        throws SAXException, ProcessingException {

        final String select = a.getValue(JXPATH_VALUEOF_SELECT);

        if (null != select) {
            Object value = getValue(getExpr(select));
            if (value == null) {
                value = "";
            }
            sendTextEvent(value.toString());
        } else {
            throw new ProcessingException("jxpath: " + JXPATH_VALUEOF + " specified without a "+JXPATH_VALUEOF_SELECT+" attribute");
        }
    }

    /**
     * Helper method to process a &lt;jxpath:continuation select=""/&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doContinuation(final Attributes a)
        throws SAXException {

        final String level = a.getValue(JXPATH_CONTINUATION_SELECT);

        final String id = (level != null)
            ? kont.getContinuation(Integer.decode(level).intValue()).getId()
            : kont.getContinuation(0).getId();

        sendTextEvent(id);
    }

    /**
     * Helper method to process a &lt;jxpath:if test="..."&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doIf(final Attributes a)
        throws SAXException {

        // handle nested jxpath:if statements, if ignoreEventsCount is > 0, then
        // we are processing a nested jxpath:if statement for which the parent
        // jxpath:if test resulted in a false (ie. disallow subelements) result.

        if (ignoreEventsCount > 0) {
            ifStack.push(Boolean.FALSE);
            ++ignoreEventsCount;
            return;
        }

        // get the test variable
        String test = a.getValue(JXPATH_IF_TEST);

        final Object value = 
            (test == null) ? Boolean.FALSE : getValue("boolean("+getExpr(test)+")");
        final boolean isTrueBoolean =
            value instanceof Boolean && ((Boolean)value).booleanValue() == true;
        ifStack.push(isTrueBoolean ? Boolean.TRUE : Boolean.FALSE);
        if (isTrueBoolean) {
            // do nothing, allow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jxpath:if results in allowing subelements");
            }
        } else {
            // disallow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jxpath:if results in disallowing subelements");
            }
            ++ignoreEventsCount;
        }
    }


    /**
     * Helper method to process a &lt;jxpath:if test="..."&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doForEach(final Attributes a)
        throws SAXException {
        if (ignoreEventsCount == 0) {
            startRecording();
            // get the test variable
            String variable = getExpr(a.getValue(JXPATH_VALUEOF_SELECT));
            Iterator iter = 
                JXPathContext.compile(variable).iteratePointers(getContext());
            foreachStack.push(iter);
        }
    }

    private void finishForEach()
        throws SAXException {
        if (ignoreEventsCount == 0) {
            DocumentFragment frag = endRecording();
            Iterator iter = (Iterator)foreachStack.pop();
            while (iter.hasNext()) {
                Pointer ptr = (Pointer)iter.next();
                pushContext(ptr.getNode());
                sendEvents(frag);
                popContext();
            }
        }
    }

    private void doChoose(final Attributes a) {
        // do otherwise by default unless ignoreEventsCount > 0
        chooseStack.push(ignoreEventsCount > 0 ? Boolean.FALSE : Boolean.TRUE);
    }

    private void doWhen(final Attributes a) throws SAXException {
        if (ignoreEventsCount > 0) {
            ++ignoreEventsCount;
            chooseStack.push(Boolean.FALSE); // my test
            return;
        }
        Boolean otherwise = (Boolean)chooseStack.peek();
        if (!otherwise.booleanValue()) {
            ++ignoreEventsCount;
            chooseStack.push(Boolean.FALSE); // my test
            return;
        }
        // get the test variable
        String test = a.getValue(JXPATH_WHEN_TEST);
        final Object value = 
            (test == null) ? Boolean.FALSE : getValue("boolean("+getExpr(test)+")");
        final boolean isTrueBoolean =
            value instanceof Boolean && ((Boolean)value).booleanValue() == true;
        if (isTrueBoolean) {
            chooseStack.pop(); // otherwise
            chooseStack.push(Boolean.FALSE); // done
        } else {
            ++ignoreEventsCount;
        }
        chooseStack.push(isTrueBoolean ? Boolean.TRUE : Boolean.FALSE);

    }

    /**
     * Helper method to process a &lt;/jxpath:if&gt; element.
     *
     * @exception SAXException if an error occurs
     */
    private void finishIf()
        throws SAXException {
        Boolean didIf = (Boolean)ifStack.pop();
        if (!didIf.booleanValue()) {
            --ignoreEventsCount;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("jxpath:if closed");
        }
    }


    private void doOtherwise(final Attributes a) {
        Boolean otherwise = (Boolean)chooseStack.peek();
        if (!otherwise.booleanValue()) {
            ++ignoreEventsCount;
        }
    }

    private void finishChoose() {
        chooseStack.pop();
    }

    private void finishWhen() {
        Boolean when = (Boolean)chooseStack.pop();
        if (!when.booleanValue()) {
            --ignoreEventsCount;
        }
    }

    private void finishOtherwise() {
        Boolean otherwise = (Boolean)chooseStack.peek();
        if (!otherwise.booleanValue()) {
            // we skipped this otherwise
            --ignoreEventsCount;
        }

    }

    /**
     * Release all held resources.
     */
    public void recycle() {
        super.recycle();
        kont = null;
        contextStack = null;
        foreachStack = null;
        chooseStack = null;
        ifStack = null;
    }
}

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
package org.apache.cocoon.components.validation.schematron;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A helper class which builds a SchematronSchema instance object
 * from a DOM source.
 *
 * @author Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @author Michael Ratliff, mratliff@collegenet.com <mratliff@collegenet.com>, May 2002
 * @version CVS $Id: SchematronFactory.java,v 1.3 2003/07/03 09:26:03 cziegeler Exp $
 */
public class SchematronFactory extends SchemaFactory {

    /**
     * The schema name space prefix used in the schema document.
     */
    private String schemaPrefix_;

    /**
     * The default schema name space prefix.
     */
    private String defaultSchemaPrefix_ = "sch";

    /**
     * Private logger.
     */
    private Logger logger = setupLogger();

    // 
    // Constructors
    // 

    /**
     * Initialize logger.
     */
    protected Logger setupLogger() {
        Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("XmlForm");

        logger.setPriority(Priority.ERROR);
        return logger;
    }

    /**
     * Builds a new Schema instance from
     * the given XML InputSource.
     *
     * @param schemaSrc
     *        the Schema document XML InputSource
     */
    public Schema compileSchema(InputSource schemaSrc)
      throws InstantiationException {
        SchematronSchema schema = null;

        try {
            // load Schema file into a DOM document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbld = dbf.newDocumentBuilder();
            Document document = dbld.parse(schemaSrc);

            schema = buildSchema(document);
        } catch (Exception e) {
            logger.error("!!! Failed loading Schematron schema", e);
            throw new CascadingRuntimeException(" !!! Failed loading Schematron schema",
                                                e);
        }
        return schema;
    } // build

    /**
     * Build Schematron schema object from a DOM document.
     *
     * @param doc DOM document containing the schema
     */
    protected SchematronSchema buildSchema(Document doc) {
        SchematronSchema schema = new SchematronSchema();

        doc.getNamespaceURI();
        doc.getPrefix();

        // Initialize the JXPath context
        Element root = doc.createElement("root");
        Element schemaElement = doc.getDocumentElement();

        schemaPrefix_ = schemaElement.getPrefix();
        root.appendChild(schemaElement);
        JXPathContext jxpContext = JXPathContext.newContext(root);

        jxpContext.setLenient(false);

        // Bind sch:schema element

        // schema title
        String title = (String) jxpContext.getValue("/schema/title",
                                                    String.class);

        schema.setTitle(title);
        logger.debug("Schema title: "+schema.getTitle());

        bindPatterns(schema, jxpContext);
        bindPhases(schema, jxpContext);

        return schema;
    }

    /**
     * Populates the patterns elements from the dom tree.
     *
     * @param schema the schema instance
     * @param jxpContext
     */
    protected void bindPatterns(SchematronSchema schema,
                                JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // schema patterns
        int ptCount = ((Integer) jxpContext.getValue("count(/schema/pattern)",
                          Integer.class)).intValue();

        logger.debug("\nNumber of patterns:  "+ptCount);
        for (int i = 1; i<=ptCount; i++) {
            logger.debug("Pattern# :  "+i);
            Pattern pattern = new Pattern();
            String ptprefix = "/schema/pattern["+i+"]";

            String name = (String) jxpContext.getValue(ptprefix+"/@name",
                                                       String.class);

            pattern.setName(name);
            logger.debug("Pattern name :  "+pattern.getName());

            String id = (String) jxpContext.getValue(ptprefix+"/@id",
                                                     String.class);

            pattern.setId(id);
            logger.debug("Pattern id :  "+pattern.getId());

            bindRules(pattern, ptprefix, jxpContext);

            schema.addPattern(pattern);
        }
    }

    /**
     * Populates the rules elements for a pattern
     * from the dom tree.
     *
     * @param pattern
     * @param pathPrefix pattern path prefix
     * @param jxpContext JXPathContext
     */
    protected void bindRules(Pattern pattern, String pathPrefix,
                             JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // schema rules
        int ruleCount = ((Integer) jxpContext.getValue("count("+pathPrefix+
                            "/rule)", Integer.class)).intValue();

        logger.debug("\nNumber of rules:  "+ruleCount);
        for (int i = 1; i<=ruleCount; i++) {
            logger.debug("Rule# :  "+i);
            Rule rule = new Rule();
            String rulePrefix = pathPrefix+"/rule["+i+"]";

            String context = (String) jxpContext.getValue(rulePrefix+
                                 "/@context", String.class);

            rule.setContext(context);
            logger.debug("Rule context :  "+rule.getContext());

            bindAsserts(rule, rulePrefix, jxpContext);

            // Patch to make reports work in schematron
            // Note change to name of bindRerports [sic] function
            bindReports(rule, rulePrefix, jxpContext);

            pattern.addRule(rule);
        }
    }

    /**
     * Populates the assert elements for a rule
     * from the dom tree.
     *
     * @param rule
     * @param pathPrefix rule path prefix
     * @param jxpContext JXPathContext
     */
    protected void bindAsserts(Rule rule, String pathPrefix,
                               JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // schema reports
        int elementCount = ((Integer) jxpContext.getValue("count("+pathPrefix+
                               "/assert)", Integer.class)).intValue();

        logger.debug("\nNumber of asserts:  "+elementCount);
        for (int i = 1; i<=elementCount; i++) {
            logger.debug("Assert# :  "+i);
            Assert assertion = new Assert();
            String assertPrefix = pathPrefix+"/assert["+i+"]";

            String test = (String) jxpContext.getValue(assertPrefix+"/@test",
                                                       String.class);

            assertion.setTest(test);
            logger.debug("Assert test :  "+assertion.getTest());

            // since diagnostics is a non-mandatory element
            // we will try to get its value in a lenient mode
            jxpContext.setLenient(true);
            String diagnostics = (String) jxpContext.getValue(assertPrefix+
                                     "/@diagnostics", String.class);

            assertion.setDiagnostics(diagnostics);
            logger.debug("Assert diagnostics :  "+assertion.getDiagnostics());
            jxpContext.setLenient(false);

            // now read the report message
            // TODO: The current implementation does not
            // read xml tags used within the assert message.
            // Solution is to use JXPath NodePointer to get
            // to the DOM node and then convert it to a String.
            // e.g.
            // NodePointer nptr = (NodePointer) jxpContext.locateValue( assertPrefix );
            // Node msgNode = (Node) nptr.getNodeValue();
            // convery DOMNode to String

            String message = (String) jxpContext.getValue(assertPrefix,
                                 String.class);

            assertion.setMessage(message);
            logger.debug("Assert message :  "+assertion.getMessage());

            rule.addAssert(assertion);
        }
    }

    /**
     * Populates the report elements for a rule
     * from the dom tree.
     *
     * @param rule
     * @param pathPrefix rule path prefix
     * @param jxpContext JXPathContext
     */
    protected void bindReports(Rule rule, String pathPrefix,
                               JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // schema reports
        int elementCount = ((Integer) jxpContext.getValue("count("+pathPrefix+
                               "/report)", Integer.class)).intValue();

        logger.debug("\nNumber of reports:  "+elementCount);
        for (int i = 1; i<=elementCount; i++) {
            logger.debug("Report# :  "+i);
            Report report = new Report();
            String assertPrefix = pathPrefix+"/report["+i+"]";

            String test = (String) jxpContext.getValue(assertPrefix+"/@test",
                                                       String.class);

            report.setTest(test);
            logger.debug("Report test :  "+report.getTest());

            // since diagnostics is a non-mandatory element
            // we will try to get its value in a lenient mode
            jxpContext.setLenient(true);
            String diagnostics = (String) jxpContext.getValue(assertPrefix+
                                     "/@diagnostics", String.class);

            report.setDiagnostics(diagnostics);
            logger.debug("Report diagnostics :  "+report.getDiagnostics());
            jxpContext.setLenient(false);

            String message = (String) jxpContext.getValue(assertPrefix,
                                 String.class);

            report.setMessage(message);
            logger.debug("Report message :  "+report.getMessage());

            rule.addReport(report);
        }
    }

    /**
     * Populates the phases elements from the dom tree.
     *
     * @param schema the schema instance
     * @param jxpContext
     */
    protected void bindPhases(SchematronSchema schema,
                              JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // schema phases
        int phaseCount = ((Integer) jxpContext.getValue("count(/schema/phase)",
                             Integer.class)).intValue();

        logger.debug("\nNumber of phases:  "+phaseCount);

        for (int i = 1; i<=phaseCount; i++) {
            logger.debug("phase# :  "+i);
            Phase phase = new Phase();
            String phprefix = "/schema/phase["+i+"]";

            String id = (String) jxpContext.getValue(phprefix+"/@id",
                                                     String.class);

            phase.setId(id);
            logger.debug("phase id :  "+phase.getId());

            bindPhaseActivePatterns(phase, phprefix, jxpContext);

            schema.addPhase(phase);
        }
    }

    protected void bindPhaseActivePatterns(Phase phase, String pathPrefix,
                                           JXPathContext jxpContext) {
        // ensure that mandatory elements which are not found
        // will result in Exception
        jxpContext.setLenient(false);

        // phase active patterns
        int elementCount = ((Integer) jxpContext.getValue("count("+pathPrefix+
                               "/active)", Integer.class)).intValue();

        logger.debug("Number of active patterns:  "+elementCount);
        for (int i = 1; i<=elementCount; i++) {
            logger.debug("active pattern # :  "+i);
            ActivePattern activePattern = new ActivePattern();
            String assertPrefix = pathPrefix+"/active["+i+"]";

            String pt = (String) jxpContext.getValue(assertPrefix+
                                                     "/@pattern", String.class);

            activePattern.setPattern(pt);
            logger.debug("Phase active pattern :  "+
                         activePattern.getPattern());

            phase.addActive(activePattern);
        }
    }

    /**
     * Replace all occurances of sch: with the actual Schema prefix used in the document.
     *
     * TODO: fix this implementaion. There are problems with DOM.
     * Returns null instead of the actual namespace prefix (e.g. "sch") as expected.
     *
     * @param path       
     *
     */
    protected String fixns(String path) {
        // Ironicly, at the time I am writing this
        // JDK 1.4 is offering String.replaceAll(regex, str)
        // I don't use it however for backward compatibility
        StringBuffer strbuf = new StringBuffer(path);
        int i = 0;
        int j = 0;
        String dprefix = defaultSchemaPrefix_+":";
        int dplen = dprefix.length();

        while ((j = path.indexOf(dprefix, i))>=0) {
            strbuf.append(path.substring(i, j));
            strbuf.append(schemaPrefix_);
            strbuf.append(':');
            i = j+dplen;
        }
        strbuf.append(path.substring(i));
        return strbuf.toString();
    }
}

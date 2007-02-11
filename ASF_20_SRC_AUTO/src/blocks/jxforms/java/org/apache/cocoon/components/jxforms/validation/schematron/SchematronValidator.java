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
package org.apache.cocoon.components.jxforms.validation.schematron;

import org.apache.cocoon.components.jxforms.validation.Validator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An object representing a single Schematron schema, used to validate
 * multiple XML instances.
 *
 * This implementation can validate JavaBeans and DOM documents.
 * It is based exclusively on the JXPath library from the Jakarta Commons project.
 * See http://jakarta.apache.org/commons/
 *
 * @author Ivelin Ivanov, ivelin@acm.org, ivelin@iname.com
 * @version CVS $Id: SchematronValidator.java,v 1.3 2004/03/05 13:01:58 bdelacretaz Exp $
 */
public class SchematronValidator implements Validator {

    /** 
     * The schema instance for this Validator.
     * It is initialized once when a new Validator instance
     * is created and used multiple times for validating
     * different JavaBeans/DOM objects against the schema
     */
    private SchematronSchema schema_;

    /**
     * Lookup map, with phase id keys.
     * Used for efficiency when validating by phase
     */
    private Map phaseMap_ = new HashMap();

    /**
     * Schematron Phase property.
     */
    private String phaseProperty_ = null;

    /**
     * Private logger.
     */
    private Logger logger = setupLogger();

    // 
    // Constructors
    // 

    /**
     * Constructs a new Validator object for a given Schematron schema.
     *
     * @param schema
     *        The Schematron schema
     */
    public SchematronValidator(SchematronSchema schema) {
        schema_ = schema;
        preparePhaseMap();
    }

    // 
    // helper methods for the constructors
    // 

    /**
     * Initialize logger.
     */
    protected Logger setupLogger() {
        Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("XmlForm");

        logger.setPriority(Priority.ERROR);
        return logger;
    }

    protected void preparePhaseMap() {
        Map patternMap = new HashMap();

        Iterator ptiter = schema_.getPattern().iterator();

        while (ptiter.hasNext()) {
            Pattern pattern = (Pattern) ptiter.next();

            patternMap.put(pattern.getId(), pattern);
        }

        Iterator phiter = schema_.getPhase().iterator();

        while (phiter.hasNext()) {
            Phase phase = (Phase) phiter.next();
            List activePatterns = new ArrayList();

            phaseMap_.put(phase.getId(), activePatterns);

            Iterator activeIter = phase.getActive().iterator();

            while (activeIter.hasNext()) {
                ActivePattern active = (ActivePattern) activeIter.next();

                activePatterns.add(patternMap.get(active.getPattern()));
            }
        }

    }

    // 
    // public methods
    // 

    /**
     * Performs validation of the passed JavaBean or DOM object.
     *
     * This method tries to find the "phase" attribute
     * and runs the active patterns for the phase.
     * If phase not found, the method will try to match all patterns
     *
     *
     * @param jbean The JavaBean or DOM object to be validated.
     *
     * @return A Result object which represents the result
     *         of the validation.
     */
    public List validate(Object jbean) {
        List patterns = null;

        if (phaseProperty_!=null) {
            patterns = getPatternsForPhase(phaseProperty_);
            logger.debug(" Validating for phase: "+phaseProperty_);
        } else {
            patterns = schema_.getPattern();
            logger.debug(" Validating all patterns. No phase provided ");
        }

        ValidationResult vres = new ValidationResult();

        if (patterns!=null) {
            // create the JXPathContext
            // which will be used to validate each rule
            JXPathContext jxpContext = JXPathContext.newContext(jbean);

            Iterator iter = patterns.iterator();

            while (iter.hasNext()) {
                Pattern resultPattern = evalPattern(jxpContext,
                                                    (Pattern) iter.next());

                // if the resultPattern is null,
                // then it passed successfully
                if (resultPattern!=null) {
                    vres.addPattern(resultPattern);
                }
            }
        }

        return vres.toList();
    }

    /**
     * Return the list of patterns listed
     * as <active/> elements of <phase/>.
     *
     * @param phase name of the phase
     * @return List of patterns
     */
    protected List getPatternsForPhase(String phase) {
        return (List) phaseMap_.get(phase);
    }

    /**
     * Returns pattern with rules which failed during validation.
     * The context attribute of each rule in the result pattern
     * contains the exact location of the failed element
     * unlike the context attribute of the original pattern which
     * is an XSLT production pattern.
     *
     * @param jxpContext The JXPathContext being validated.
     * @param pattern The production schema pattern to be evaluated.
     * @return Pattern with rules wich failed during validation.
     */
    protected Pattern evalPattern(JXPathContext jxpContext, Pattern pattern) {
        // copy attributes
        Pattern resultPattern = new Pattern();

        resultPattern.setName(pattern.getName());
        resultPattern.setId(pattern.getId());

        // evaluate rules
        Iterator iter = pattern.getRule().iterator();

        while (iter.hasNext()) {
            List failedRules = evalRule(jxpContext, (Rule) iter.next());

            // if there were failed rules
            // add them to the list of other failed rules
            if (failedRules.size()>0) {
                failedRules.addAll(resultPattern.getRule());
                resultPattern.setRule(failedRules);
            }
        }

        // if there are no failed rules return null
        if (resultPattern.getRule().size()==0) {
            return null;
        } else {
            return resultPattern;
        }
    }

    /**
     * Returns rules with asserts or reports which failed during validation.
     * The context attribute of each rule in the result pattern
     * contains the exact location of the failed element
     * unlike the context attribute of the original pattern which
     * is an XSLT production pattern.
     *
     * @param jxpContext The JXPath context being validated.
     * @param rule The original pattern rule to be evaluated.
     * @return Pattern with rules wich failed during validation.
     */
    protected List evalRule(JXPathContext jxpContext, Rule rule) {
        List failedRules = new ArrayList();

        Iterator pointerIter = jxpContext.iteratePointers(rule.getContext());

        while (pointerIter.hasNext()) {
            Pointer ptr = (Pointer) pointerIter.next();

            // prepare result Rule
            Rule nextFailedRule = new Rule();

            nextFailedRule.setContext(ptr.asPath());

            // switch to the context of the rule
            JXPathContext localJxpContext = jxpContext.getRelativeContext(ptr);

            // evaluate asserts
            Iterator assertIter = rule.getAssert().iterator();

            while (assertIter.hasNext()) {
                Assert assertion = (Assert) assertIter.next();
                // if an assert test fails, then it should be added
                // to the result
                boolean passed = evalTest(localJxpContext,
                                          assertion.getTest());

                if ( !passed) {
                    nextFailedRule.addAssert(assertion);
                }
            }

            // evaluate reports
            Iterator reportIter = rule.getReport().iterator();

            while (reportIter.hasNext()) {
                Report report = (Report) reportIter.next();
                // if a report test passes, then it should be added
                // to the result
                boolean passed = evalTest(localJxpContext, report.getTest());

                if (passed) {
                    nextFailedRule.addReport(report);
                }
            }

            // if the nextFailedRule is non empty,
            // then add it to the list of failed rules
            if ((nextFailedRule.getAssert().size()>0) ||
                (nextFailedRule.getReport().size()>0)) {
                failedRules.add(nextFailedRule);
            }
        }

        return failedRules;
    }

    /**
     * Test an XPath expression in a context.
     *
     * @param jxpContext The JXPath context being validated
     * @param test       
     * @return boolean result of evaluation
     */
    protected boolean evalTest(JXPathContext jxpContext, String test) {
        Boolean passed = (Boolean) jxpContext.getValue(test, Boolean.class);

        return passed.booleanValue();
    }

    /**
     * @param property Name.
     * @return The property value.
     * @throws IllegalArgumentException When the property is not supported.
     */
    public Object getProperty(String property)
      throws IllegalArgumentException {
        if (property.equals(Validator.PROPERTY_PHASE)) {
            return phaseProperty_;
        } else {
            throw new IllegalArgumentException(" Property "+property+
                                               " is not supported");
        }
    }

    /**
     * @param property Name.
     * @param value Property value.
     * @throws IllegalArgumentException When the property is not supported
     */
    public void setProperty(String property,
                            Object value) throws IllegalArgumentException {
        if (property.equals(Validator.PROPERTY_PHASE) &&
            ((value==null) || (value instanceof String))) {
            phaseProperty_ = (String) value;
        } else {
            throw new IllegalArgumentException(" Property "+property+
                                               " is not supported or value is invalid");
        }
    }
}

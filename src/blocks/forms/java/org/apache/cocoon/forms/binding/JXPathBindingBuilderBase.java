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
package org.apache.cocoon.forms.binding;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract base class enabling logging and supporting the intrepretation of
 * common configuration settings on all specific implementations of
 * {@link org.apache.cocoon.forms.binding.JXPathBindingBase}.
 *
 * Common supported configurations: {@link #getCommonAttributes(Element)}
 * <ul>
 * <li>Attribute direction="load|save|both": defaults to 'both'</li>
 * <li>Attribute lenient="true|false|[undefined]": defaults to [undefined]
 *     which means: "continue in same leniency-mode as parent" </li>
 * </ul>
 *
 * @version CVS $Id: JXPathBindingBuilderBase.java,v 1.3 2004/04/01 12:59:57 mpo Exp $
 */
public abstract class JXPathBindingBuilderBase implements LogEnabled {

    private Logger logger;

    /**
     * Receives the Avalon logger to use.
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
        if (logger.isDebugEnabled()) {
            logger.debug("JXPathBindingBuilderBase got logger...");
        }
    }

    /**
     * Makes the logger available to the subclasses.
     * @return Logger
     */
    protected Logger getLogger() {
        return this.logger;
    }

    /**
     * Builds a configured binding object based on the configuration as
     * described in the bindingElement.  The BuilderMap can be used to
     * find appropriate builders for possible subBinders.
     *
     * @param bindingElm
     * @param assistant
     * @return JXPathBindingBase
     */
    public abstract JXPathBindingBase buildBinding(
        Element bindingElm,
        JXPathBindingManager.Assistant assistant) throws BindingException;

    /**
     * Helper method for interpreting the common attributes which are supported
     * on each of the Bindings.  These are
     * <br>
     * <code>@direction</code> can hold one of the following values:
     * <ol><li><code>'load'</code>: This binding will only load.</li>
     * <li><code>'save'</code>: This binding will only save.</li>
     * <li><code>'both'</code>: This binding will perform both operations.</li>
     * </ol>
     * <br>
     * <code>@lenient</code> can either be:
     * <ol><li><code>'true'</code>: This binding will set the jxpath context to
     * be lenient towards the usage of inexisting paths on the back-end model.</li>
     * <li><code>'false'</code>: This binding will set the jxpath context to be
     * strict and throwing exceptions for the usage of inexisting paths on the
     * back-end model.</li>
     * <li><code>(unset)</code>: This binding will not change the leniency behaviour
     * on the jxpath this binding receives from his parent binding.</li>
     * </ol>
     * @param bindingElm
     * @return an instance of CommonAttributes
     * @throws BindingException
     */
     static CommonAttributes getCommonAttributes(Element bindingElm) throws BindingException {
        try {
            //TODO: should we eventually remove this?
            //throw an error if people are still using the old-style @read-only or @readonly
            if (DomHelper.getAttributeAsBoolean(bindingElm, "readonly", false)) {
                throw new BindingException("Error in binding file " + DomHelper.getLocation(bindingElm)
                        + "\nThe usage of the attribute @readonly has been deprecated in favour of @direction.");
            }
            if (DomHelper.getAttributeAsBoolean(bindingElm, "read-only", false)) {
                throw new BindingException("Error in binding file " + DomHelper.getLocation(bindingElm)
                        + "\nThe usage of the attribute @read-only has been deprecated in favour of @direction.");
            }

            String direction = DomHelper.getAttribute(bindingElm, "direction", "both");

            String leniency = DomHelper.getAttribute(bindingElm, "lenient", null);

            return new CommonAttributes(direction, leniency);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
     }

     /**
      * CommonAttributes is a simple helper class for holding the distinct data
      * member fields indicating the activity of the sepearate load and save
      * actions of a given binding.
      */
     static class CommonAttributes{
        /**
         * Flag which controls whether a binding is active during loading.
         */
        final boolean loadEnabled;
        /**
         * Flag which controls whether a binding is active during saving.
         */
        final boolean saveEnabled;
        final Boolean leniency;

        final static CommonAttributes DEFAULT = new CommonAttributes(true, true, null);

        CommonAttributes(String direction, String leniency){
            this(isLoadEnabled(direction), isSaveEnabled(direction), decideLeniency(leniency));
        }

        CommonAttributes(boolean loadEnabled, boolean saveEnabled, Boolean leniency){
            this.loadEnabled = loadEnabled;
            this.saveEnabled = saveEnabled;
            this.leniency = leniency;
        }

        /**
         * Interpretes the value of the direction attribute into activity of the load action.
         * @param direction
         * @return true if direction is either set to "both" or "load"
         */
        private static boolean isLoadEnabled(String direction) {
            return "both".equals(direction) || "load".equals(direction);
        }

        /**
         * Interpretes the value of the direction attribute into activity of the save action.
         * @param direction value of the @direction attribute
         * @return true if direction is either set to "both" or "save"
         */
        private static boolean isSaveEnabled(String direction) {
            return "both".equals(direction) || "save".equals(direction);
        }


        /**
         * Interpretes the value of the lenient attribute into a Boolean object
         * allowing three-state logic (true/false/unset)
         * @param leniency value of the @lenient attribute
         * @return null if the leniency parameter is String, otherwise the
         */
        private static Boolean decideLeniency(String leniency) {
            if (leniency == null) {
                return null;
            }
            return new Boolean(leniency);
        }
    }
}

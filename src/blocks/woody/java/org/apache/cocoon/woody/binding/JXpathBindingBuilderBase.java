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
package org.apache.cocoon.woody.binding;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract base class enabling logging and supporting the intrepretation of 
 * common configuration settings on all specific implementations of
 * {@see org.apache.cocoon.woody.binding.JXPathBindingBase}.
 * 
 * Common supported configurations:
 * <ul>
 * <li>Attribute direction="load|save|both": {@see #getDirectionAttributes(Element)}</li>
 * </ul>
 */
public abstract class JXpathBindingBuilderBase implements LogEnabled {

    private Logger logger;

    /**
     * Receives the Avalon logger to use.
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
        logger.debug("JXpathBindingBuilderBase got logger...");
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
            //TODO: should we evetually remove this?
            //throw an error if people are still using the old-style @read-only
            if (DomHelper.getAttributeAsBoolean(bindingElm, "read-only", false)) {
                throw new BindingException("Error in binding file " + DomHelper.getLocation(bindingElm) 
                        + "\nThe usage of the attribute @read-only has been depricated in favour of @direction.");
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
        final boolean loadEnabled;
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
            if (leniency == null) return null;
            return new Boolean(leniency);
        }
        
    }
}

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
package org.apache.cocoon.selection;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * In a &lt;map:handle-errors>, selects depending on the exception that caused the error.
 * The configuration of this selector allows to map exception class names to symbolic names
 * that are used in the &lt;map:when> alternatives.
 * <p>
 * Example configuration :
 * <pre>
 *   &lt;map:selector type="error" src="....ExceptionSelector">
 *     &lt;exception class="org.xml.sax.SAXException" name="sax" unroll="true"/>
 *     &lt;exception name="not-found" class="org.apache.cocoon.ResourceNotFoundException"/>
 *     &lt;exception class="org.apache.cocoon.ProcessingException" unroll="true"/>
 *     &lt;exception name="denied" class="java.security.SecurityException"/>
 *     &lt;exception name="denied" class="my.comp.auth.AuthenticationFailure"/>
 *   &lt;/map:selector>
 * </pre>
 * This example shows several features :
 * <li>the "class" is the class name of the exception (which can be any <code>Throwable</code>),</li>
 * <li>an exception can be given a name, which is used in the &lt;map:when> tests,</li>
 * <li>an exception can be unrolled, meaning we try to get its cause and then consider this cause for
 *     the exception name</li>
 * Note that both "name" and "unroll" can be specified. In that case, we first try to unroll the exception,
 * and if none of the causes has a name, then the "name" attribute is considered.
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @since 2.1
 * @version CVS $Id: ExceptionSelector.java,v 1.8 2003/12/29 15:24:35 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=Selector
 * @x-avalon.lifestyle type=singleton
 */
public class ExceptionSelector extends AbstractSwitchSelector implements Configurable {

	/** Exception classes */
    private Class[] clazz;
    
    /** Associated symbolic names (can be null) */
    private String[] name;
    
    /** Do we want to unroll them ? */
    private boolean[] unroll;

    public void configure(Configuration conf) throws ConfigurationException {

        Configuration[] children = conf.getChildren("exception");

        this.clazz = new Class[children.length];
        this.name = new String[children.length];
        this.unroll = new boolean[children.length];

        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];

            String childClassName = child.getAttribute("class");
            Class childClass = null;
            try {
                childClass = ClassUtils.loadClass(childClassName);
            }
            catch (Exception e) {
                throw new ConfigurationException("Cannot load class '" + childClassName + "' at " + child.getLocation());
            }
            
            // Check that this class is not hidden by a more general class already declared
            for (int j = 0; j < i; j++) {
                if (this.clazz[j].isAssignableFrom(childClass)) {
                    throw new ConfigurationException("Class '" + this.clazz[j].getName() + "' hides its subclass '" +
                    	childClassName + "' at " + child.getLocation());
                }
            }

			this.clazz[i] = childClass;
            this.name[i] = child.getAttribute("name", null);
            this.unroll[i] = child.getAttributeAsBoolean("unroll", false);

            if (this.name[i] == null && !this.unroll[i]) {
                throw new ConfigurationException("Must specify one of 'name' or 'unroll' at " + child.getLocation());
            }
        }
    }

    /**
     * Compute the exception type, given the configuration and the exception stored in the object model.
     * 
     * @see ObjectModelHelper#getThrowable(java.util.Map)
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        // Get the name of the exception
        Throwable thr = ObjectModelHelper.getThrowable(objectModel);
        if (thr == null) {
            throw new IllegalStateException("No exception in object model. ExceptionSelector can only be used in <map:handle-errors>");
        }

        return find(thr);
    }

    private FindResult find(Throwable thr) {
        // Now find the proper name
        for (int i = 0; i < this.clazz.length; i++) {
            if (this.clazz[i].isInstance(thr)) {

                // If exception needs to be unrolled, and it has a cause,
                // return the cause name, if not null (recursively)
                if (this.unroll[i]) {
                    Throwable cause = ExceptionUtils.getCause(thr);
                    if (cause != null) {
                        FindResult result = find(cause);
                        if (result != null) {
                            return result;
                        }
                    }
                }

                // Not unrolled
                return new FindResult(this.name[i], thr);
            }
        }

        // Not found
        return null;
    }

    public boolean select(String expression, Object selectorContext) {
        if ( selectorContext == null ) {
            return false;
        }
        // Just compare the expression with the previously found name
		boolean result = expression.equals(((FindResult)selectorContext).getName());
		
		if (result) {
			if (getLogger().isDebugEnabled())
				getLogger().debug("select succesfull for condition " + selectorContext.toString());						
		}
        
		return result; 
    }
    
    class FindResult {
    	private String name;
    	private Throwable throwable;
    	
    	public FindResult(String name, Throwable throwable) {
    		this.name = name;
    		this.throwable = throwable;
    	}
    	
		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Throwable getThrowable() {
			return this.throwable;
		}

		public void setThrowable(Throwable throwable) {
			this.throwable = throwable;
		}
		
		public String toString() {
			return this.name;
		}
    }

}

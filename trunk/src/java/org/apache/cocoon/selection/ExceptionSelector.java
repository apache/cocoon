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
 * @version CVS $Id: ExceptionSelector.java,v 1.9 2004/03/08 14:03:29 cziegeler Exp $
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

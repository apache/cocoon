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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

/**
 * <p>The {@link AbstractRegexpSelector} abstract class defines a simple selector
 * operating over configured regular-expression patterns.</p> 
 *
 * <p>Configuration of an {@link AbstractRegexpSelector} is quite simple: first of
 * all the patterns used for selections must be configured:</p>
 * 
 * <pre>
 * &lt;map:components&gt;
 *   ...
 *   &lt;map:selectors default="..."&gt;
 *     &lt;map:selector name="..." src="org.apache.cocoon.selection...."&gt;
 *       &lt;pattern name="empty"&gt;^$&lt;/pattern&gt;
 *       &lt;pattern name="number"&gt;^[0-9]+$&lt;/pattern&gt;
 *       &lt;pattern name="string"&gt;^.+$&lt;/pattern&gt;
 *     &lt;/map:selector&gt;
 *  &lt;/map:selectors&gt;
 * &lt;/map:components&gt;
 * </pre>
 * 
 * <p>Then, each configured pattern can be referenced in the pipelines section of
 * the sitemap:</p>
 * 
 * <pre>
 * &lt;map:pipelines&gt;
 *   ...
 *   &lt;map:match ...&gt;
 *     ...
 *     &lt;map:select type="browser"&gt;
 *       &lt;map:when test="empty"&gt;...&lt;/map:when&gt;
 *       &lt;map:when test="number"&gt;...&lt;/map:when&gt;
 *       &lt;map:when test="string"&gt;...&lt;/map:when&gt;
 *       &lt;map:otherwise&gt;...&lt;/map:otherwise&gt;
 *     &lt;/map:select&gt;
 *     ...
 *   &lt;/map:match&gt;
 *   ...
 * &lt;/map:pipelines&gt;
 * </pre>
 *
 * @version CVS $Revision: 1.1 $
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 */
public abstract class AbstractRegexpSelector extends AbstractSwitchSelector
implements Configurable, ThreadSafe {

    /** <p>A {@link Map} of regular expression programs by name.</p> */
    protected Map patterns = new HashMap();

    /**
     * <p>Create a new {@link AbstractRegexpSelector} instance.</p>
     */
    protected AbstractRegexpSelector() {
        super();
    }

    /**
     * <p>Select a pipeline fragment based on a previously configured pattern.</p>
     * 
     * @param patternName the name of the configured pattern.
     * @param selectorContext the string to be matched by the named pattern.
     * @return <b>true</b> if the contexts is matched by the configured pattern.
     */
    public boolean select(String patternName, Object selectorContext) {

        /* Check that the context selection returned something */
        if (selectorContext == null) return(false);

        /* Check that we actually have a configured pattern */
        REProgram pattern = (REProgram) this.patterns.get(patternName);
        if (pattern == null) {
            if (this.getLogger().isWarnEnabled()) {
                this.getLogger().warn("The specified pattern name \"" + patternName
                                      + "\" was not configured in this instance");
            }
            return(false);
        }

        /* Pattern matching */
        return(new RE(pattern).match(selectorContext.toString()));
    }

    /**
     * <p>Configure this instance parsing all regular expression patterns.</p>
     * 
     * @param configuration the {@link Configuration} instance where configured
     *                      patterns are defined.
     * @throws ConfigurationException if one of the regular-expression to configure
     *                                could not be compiled.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        Configuration patterns[] = configuration.getChildren("pattern");
        for (int x = 0; x < patterns.length; x++) {
            String name = patterns[x].getAttribute("name");
            String pattern = patterns[x].getValue();
            this.patterns.put(name, this.compile(pattern));
        }
    }

    /**
     * <p>Compile the pattern in a {@link REProgram}.</p>
     * 
     * @param pattern the regular expression pattern in a textual format.
     * @return a compiled regular expression pattern.
     * @throws ConfigurationException in the pattern could not be compiled. 
     */
    protected REProgram compile(String pattern)
    throws ConfigurationException {
        if (pattern == null) {
            throw new ConfigurationException("Null pattern");
        }

        if (pattern.length() == 0) {
            pattern = "^$";
            if (this.getLogger().isWarnEnabled()) {
                this.getLogger().warn("The empty pattern string was rewritten to "
                                      + "'^$' to match for empty strings.  If you "
                                      + "intended to match all strings, please "
                                      + "change your pattern to '.*'");
            }
        }

        try {
            RECompiler compiler = new RECompiler();
            REProgram program = compiler.compile(pattern);
            return program;
        } catch (RESyntaxException rse) {
            getLogger().debug("Failed to compile the pattern '" + pattern + "'", rse);
            throw new ConfigurationException(rse.getMessage(), rse);
        }
    }
}

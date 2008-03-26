/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.flow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * This is a base class for all interpreters compiling the scripts
 *
 * @version $Id$
 */
public abstract class CompilingInterpreter extends AbstractInterpreter {

    /**
     * A source resolver
     */
    protected SourceResolver sourceresolver;

    /**
     * Mapping of String objects (source uri's) to ScriptSourceEntry's
     */
    protected final Map compiledScripts = new HashMap();

    
    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.sourceresolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        for (Iterator i = this.compiledScripts.values().iterator(); i.hasNext(); ) {
            ScriptSourceEntry entry = (ScriptSourceEntry) i.next();
            this.sourceresolver.release(entry.getSource());
        }
        this.compiledScripts.clear();

        if (this.manager != null) {
            this.manager.release(this.sourceresolver);
            this.sourceresolver = null;
        }

        super.dispose();
    }

    /**
     * TODO - This is a little bit strange, the interpreter calls
     * {@link ScriptSourceEntry#compile} which in turn calls this
     * compileScript mthod on the interpreter. I think we need
     * more refactoring here.
     */
    protected abstract Script compileScript(Context context,
                                            Scriptable scope,
                                            Source source) throws Exception;

    protected class ScriptSourceEntry {
        private final Source source;
        private Script script;
        private long compileTime;

        public ScriptSourceEntry(Source source) {
            this.source = source;
        }

        public Source getSource() {
            return source;
        }

        public Script getScript() {
            return script;
        }

        public long getCompileTime() {
            return compileTime;
        }

        public void compile(Context context, Scriptable scope)
        throws Exception {
            // If not first compile() call, refresh the source.
            if (script != null) {
                source.refresh();
            }
            // Compile script if this is first compile() call or source was modified.
            if (script == null || compileTime < source.getLastModified()) {
                script = CompilingInterpreter.this.compileScript(context, scope, source);
                compileTime = source.getLastModified();
            }
        }
    }
}

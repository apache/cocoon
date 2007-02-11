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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.impl.PartSource;
import org.apache.cocoon.environment.Redirector;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.TraversableSource;

/**
 * The CopySourceAction copies the content of it's "src" attribute to its "dest" parameter.
 * The destination must of course resolve to a <code>WriteableSource</code>
 * <p>
 * Example :
 * <pre>
 *   &lt;map:act type="copy-source" src="cocoon://pipeline.xml"&gt;
 *     &lt;map:parameter name="dest" value="context://WEB-INF/data/file.xml"/&gt;
 *     .../...
 *   &lt;/map:act&gt;
 *</pre>
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id$
 */
public class CopySourceAction extends ServiceableAction implements ThreadSafe {

    private SourceResolver resolver;

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }

    public Map act(Redirector redirector, org.apache.cocoon.environment.SourceResolver oldResolver, Map objectModel, String source, Parameters par)
        throws Exception {

        // Get source and destination Sources
        Source src = resolver.resolveURI(source);
        Source dest = resolver.resolveURI(par.getParameter("dest"));

        // Check that dest is writeable
        if (! (dest instanceof ModifiableSource)) {
            throw new IllegalArgumentException("Non-writeable URI : " + dest.getURI());
        }

        if (dest instanceof TraversableSource) {
            TraversableSource trDest = (TraversableSource) dest;
            if (trDest.isCollection()) {
                if (src instanceof TraversableSource) {
                    dest = trDest.getChild(((TraversableSource)src).getName());
                } else if (src instanceof PartSource){
                    // FIXME : quick hack to store "upload://xxx" sources into directories
                    // it would be better for the PartSource to be Traversable, or to
                    // create a new "NamedSource" interface
                    dest = trDest.getChild(((PartSource)src).getPart().getFileName());
                }
            }
        }
        // And transfer all content.
        try {
            SourceUtil.copy(src, dest);
        } finally {
            resolver.release(src);
            resolver.release(dest);
        }
        // Success !
        return EMPTY_MAP;
    }
}

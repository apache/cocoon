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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.Collections;
import java.util.Iterator;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDescription;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.FrameLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Adds a cinclude tag for a FrameLayout's source to the resulting stream.
 * 
 * <h2>Example XML:</h2>
 * <pre>
 *  &lt;xy:z src="coplet://copletID"/&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.FrameLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>aspect-name</th><td></td><td></td><td>String</td><td><code>"frame"</code></td></tr>
 * <tr><th>store</th><td></td><td>req</td><td>String</td><td><code>null</code></td></tr>
 * </tbody></table>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: FrameAspect.java,v 1.8 2004/04/25 20:09:34 haul Exp $
 */
public class FrameAspect extends AbstractCIncludeAspect {

    public void toSAX(RendererAspectContext context, Layout layout, PortalService service, ContentHandler handler)
    throws SAXException {
        PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();

        if (!(layout instanceof FrameLayout)) {
            throw new SAXException("Wrong layout type, FrameLayout expected: " + layout.getClass().getName());
        }

        String source = (String)layout.getAspectData(config.aspectName);
        if (source == null) {
            source = ((FrameLayout) layout).getSource();
        }
        
        this.createCInclude(source, handler);
    }

    protected class PreparedConfiguration {
        public String aspectName;
        public String store;
        
        public void takeValues(PreparedConfiguration from) {
            this.aspectName = from.aspectName;
            this.store = from.store;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.aspectName = configuration.getParameter("aspect-name", "frame");
        pc.store = configuration.getParameter("store");
        return pc;
    }

    /**
     * Return the aspects required for this renderer
     * @return An iterator for the aspect descriptions or null.
     */
    public Iterator getAspectDescriptions(Object configuration) {
        PreparedConfiguration pc = (PreparedConfiguration)configuration;
        
        DefaultAspectDescription desc = new DefaultAspectDescription();
        desc.setName(pc.aspectName);
        desc.setClassName("java.lang.String");
        desc.setPersistence(pc.store);
        desc.setAutoCreate(false);
        
        return Collections.singletonList(desc).iterator();
    }
}

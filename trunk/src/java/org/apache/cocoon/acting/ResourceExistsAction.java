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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;

/**
 * This action simply checks to see if a given resource exists. It checks
 * whether the specified in the src attribute source exists or not.
 * The action returns empty <code>Map</code> if it exists, null otherwise.
 * <p>Instead of src attribute, source can be specified using
 * parameter named 'url' (this is old syntax).
 * <p>
 * <b>Note:</b> {@link org.apache.cocoon.selection.ResourceExistsSelector}
 * should be preferred to this component, as the semantics of a Selector better
 * match the supplied functionality.
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version CVS $Id: ResourceExistsAction.java,v 1.7 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="Action"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="resource-exists"
 */
public class ResourceExistsAction extends AbstractAction {

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception {
        String resourceURI = parameters.getParameter("url", src);
        Source source = null;
        try {
            source = resolver.resolveURI(resourceURI);
            if (source.exists()) {
                return EMPTY_MAP;
            }
        } catch (SourceNotFoundException e) {
            // Do not log
        } catch (Exception e) {
            getLogger().warn("Exception resolving resource " + resourceURI, e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
        }
        return null;
    }
}

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
package org.apache.cocoon.components.pipeline;

import org.apache.avalon.framework.component.ComponentSelector;

/**
 * A <code>ComponentSelector</code> for output components used by a {@link ProcessingPipeline}.
 * This selector is able to associate a MIME type to a hint.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: OutputComponentSelector.java,v 1.2 2004/03/05 13:02:50 bdelacretaz Exp $
 */

public interface OutputComponentSelector extends ComponentSelector {

    /**
     * Get the MIME type for a given hint.
     *
     * @param hint the component hint
     * @return the MIME type for this hint, or <code>null</code>.
     */
    String getMimeTypeForHint(Object hint);
}

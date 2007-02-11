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
package org.apache.cocoon.reading;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;

/**
 * The <code>ComposerReader</code> will allow any <code>Reader</code>
 * that extends this to access SitemapComponents.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * 
 * @deprecated Use the ServiceableReader instead
 * @version CVS $Id: ComposerReader.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */
public abstract class ComposerReader extends AbstractReader
    implements Composable {

    /** The component manager instance */
    protected ComponentManager manager;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(final ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }
}

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
package org.apache.cocoon.components.source.impl;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.DeferredValidity;

/**
 * Implementation of a {@link Source} that gets its content
 * by invoking a pipeline.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SourceDeferredValidity.java,v 1.3 2004/06/16 14:29:31 vgritsenko Exp $
 */
public final class SourceDeferredValidity implements DeferredValidity {

    private Source source;

    public SourceDeferredValidity(Source source) {
        this.source = source;
    }

    /**
     * @see org.apache.excalibur.source.impl.validity.DeferredValidity#getValidity()
     */
    public SourceValidity getValidity() {
        return this.source.getValidity();
    }
}

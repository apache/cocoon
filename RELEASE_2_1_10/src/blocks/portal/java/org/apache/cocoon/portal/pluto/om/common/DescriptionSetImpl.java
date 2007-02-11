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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Iterator;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class DescriptionSetImpl 
    extends AbstractSupportSet 
    implements DescriptionSet, java.io.Serializable, Support {

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.DescriptionSet#get(java.util.Locale)
     */
    public Description get(Locale locale) {        
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Description desc = (Description)iterator.next();
            if (desc.getLocale().equals(locale)) {
                return desc;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postBuild(java.lang.Object)
     */
    public void postBuild(Object parameter) throws Exception {
        // nothing to do 
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postLoad(java.lang.Object)
     */
    public void postLoad(Object parameter) throws Exception {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ((DescriptionImpl)iterator.next()).postLoad(parameter);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postStore(java.lang.Object)
     */
    public void postStore(Object parameter) throws Exception {
        // nothing to do 
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#preBuild(java.lang.Object)
     */
    public void preBuild(Object parameter) throws Exception {
        // nothing to do 
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#preStore(java.lang.Object)
     */
    public void preStore(Object parameter) throws Exception {
        // nothing to do 
    }

}

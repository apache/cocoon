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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.pluto.om.portlet.ContentType;
import org.apache.pluto.om.portlet.ContentTypeSet;
import org.apache.pluto.util.StringUtils;

/**
 *
 * @version $Id$
 */
public class ContentTypeSetImpl extends AbstractSupportSet
implements ContentTypeSet, java.io.Serializable {

    // special content type that represents the union of all supported markups
    private ContentType anyContentType;

    /**
     * @see org.apache.pluto.om.portlet.ContentTypeSet#get(java.lang.String)
     */
    public ContentType get(String contentType) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            ContentType _contentType = (ContentType)iterator.next();
            if (_contentType.getContentType().equals(contentType)) {
                return _contentType;
            }
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postLoad(java.lang.Object)
     */
    public void postLoad(Object parameter) throws Exception {
    	super.postLoad(parameter);

    	Collection allPortletModes = new ArrayList();
    	Iterator contentTypes = this.iterator();
		while (contentTypes.hasNext()){
			ContentType aContentType = (ContentType)contentTypes.next();
			Iterator portletModes = aContentType.getPortletModes();
			
			while(portletModes.hasNext()) {
				Object portletMode = portletModes.next();
				if(!allPortletModes.contains(portletMode)) {
					allPortletModes.add(portletMode);	 
				}
			}
		}

		ContentTypeImpl _anyContentType = new ContentTypeImpl();
		_anyContentType.setPortletModes(allPortletModes);
		anyContentType = _anyContentType;
	}

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": ");
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buffer.append(((ContentTypeImpl)iterator.next()).toString(indent+2));
        }
        return buffer.toString();
    }    

	public boolean supportsPortletMode(javax.portlet.PortletMode portletMode) {
		return anyContentType.supportsPortletMode(portletMode);
	}
}

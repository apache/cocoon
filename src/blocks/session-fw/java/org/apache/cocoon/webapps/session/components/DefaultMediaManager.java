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
package org.apache.cocoon.webapps.session.components;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.webapps.session.MediaManager;

/**
 * This is the default implementation for the media manager
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultMediaManager.java,v 1.3 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public final class DefaultMediaManager
extends AbstractLogEnabled
implements MediaManager, Configurable, ThreadSafe, Contextualizable, Component {

    /** The media Types */
    protected PreparedMediaType[] allMediaTypes;
    
    /** The default media type (usually this is html) */
    protected String      defaultMediaType;
    
    /** All media type names */
    protected String[]    mediaTypeNames;

    /** The Context */
    protected Context context;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration myConfiguration)
    throws ConfigurationException {
        // no sync required
        Configuration mediaConf = myConfiguration.getChild("mediatypes", false);
        if (mediaConf == null) {
            // default configuration
            this.defaultMediaType = "html";
        } else {
            this.defaultMediaType = mediaConf.getAttribute("default", "html");
        }
        this.mediaTypeNames = new String[1];
        this.mediaTypeNames[0] = this.defaultMediaType;
        boolean found;
        int     i;
        String  name;

        Configuration[] childs = mediaConf.getChildren("media");
        PreparedMediaType[] array = new PreparedMediaType[0];
        PreparedMediaType[] copy;
        Configuration current;
        if (childs != null) {
            for(int x = 0; x < childs.length; x++) {
                current = childs[x];
                copy = new PreparedMediaType[array.length + 1];
                System.arraycopy(array, 0, copy, 0, array.length);
                array = copy;
                name = current.getAttribute("name");
                array[array.length-1] = new PreparedMediaType(name, current.getAttribute("useragent"));
                found = false;
                i = 0;
                while ( i < this.mediaTypeNames.length && found == false) {
                    found = this.mediaTypeNames[i].equals(name);
                    i++;
                }
                if (found == false) {
                    String[] newStrings = new String[this.mediaTypeNames.length + 1];
                    System.arraycopy(this.mediaTypeNames, 0, newStrings, 0, this.mediaTypeNames.length);
                    newStrings[newStrings.length-1] = name;
                    this.mediaTypeNames = newStrings;
                }
            }
        }
        this.allMediaTypes = array;
    }

    /**
     * Test if the media of the current request is the given value
     */
    public boolean testMedia(String value) {
        // synchronized
        boolean result = false;

        Request request = ContextHelper.getRequest(this.context);
        
        String useragent = request.getHeader("User-Agent");
        PreparedMediaType theMedia = null;
        int i, l;
        i = 0;
        l = this.allMediaTypes.length;
        while (i < l && theMedia == null) {
            if (useragent.indexOf(this.allMediaTypes[i].useragent) == -1) {
                i++;
            } else {
                theMedia = this.allMediaTypes[i];
            }
        }
        if (theMedia != null) {
            result = theMedia.name.equals(value);
        } else {
            result = this.defaultMediaType.equals(value);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.session.MediaManager#getMediaTypes()
     */
    public String[] getMediaTypes() {
        // synchronized
        return this.mediaTypeNames;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.webapps.session.MediaManager#getMediaType()
     */
    public String getMediaType() {
        // synchronized
        Request request = ContextHelper.getRequest( this.context );
        // get the media of the current request
        String useragent = request.getHeader("User-Agent");
        PreparedMediaType media = null;
        if (useragent != null) {
            int i, l;
            i = 0;
            l = this.allMediaTypes.length;
            while (i < l && media == null) {
                if (useragent.indexOf(this.allMediaTypes[i].useragent) == -1) {
                    i++;
                } else {
                    media = this.allMediaTypes[i];
                }
            }
        }
        return (media == null ? this.defaultMediaType : media.name);
    }

}


/**
 * This class stores the media type configuration
 */
final class PreparedMediaType {

    String name;
    String useragent;

    PreparedMediaType(String name, String useragent) {
        this.name = name;
        this.useragent = useragent;
    }
}

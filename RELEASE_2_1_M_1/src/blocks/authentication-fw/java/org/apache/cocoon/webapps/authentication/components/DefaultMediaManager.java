/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.authentication.components;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.RequestLifecycleComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.authentication.MediaManager;
import org.xml.sax.SAXException;

/**
 * This is the basis authentication component.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultMediaManager.java,v 1.1 2003/04/21 19:26:14 cziegeler Exp $
*/
public final class DefaultMediaManager
extends AbstractLogEnabled
implements MediaManager, Configurable, RequestLifecycleComponent, Recyclable {

    /** The media Types */
    private PreparedMediaType[] allMediaTypes;
    
    /** The default media type (usually this is html) */
    private String      defaultMediaType;
    
    /** All media type names */
    private String[]    mediaTypeNames;

    /** tThe current media type */
    private String mediaType;

    /** The current request */
    private Request request;
    
    /**
     * Configurable interface.
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
     * Get the current media type
     */
    public void setup(SourceResolver resolver, Map objectModel)
    throws ProcessingException, SAXException, IOException {
        this.request = ObjectModelHelper.getRequest( objectModel );
        // get the media of the current request
        String useragent = this.request.getHeader("User-Agent");
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
        this.mediaType = (media == null ? this.defaultMediaType : media.name);
    }

    /**
     * Test if the media of the current request is the given value
     */
    public boolean testMedia(String value) {
        // synchronized
        boolean result = false;

        String useragent = this.request.getHeader("User-Agent");
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

    /**
     * Get all media type names
     */
    public String[] getMediaTypes() {
        // synchronized
        return this.mediaTypeNames;
    }

    /**
     * Return the current media type
     */
    public String getMediaType() {
        // synchronized
        return this.mediaType;
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.request = null;
        this.mediaType = null;
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

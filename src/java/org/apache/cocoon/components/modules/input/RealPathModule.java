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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * RealPathModule provides a real filesystem path for a virtual
 * context-relative path.  If this mapping cannot be performed (e.g. Cocoon is
 * running in a .war file), <code>null</code> will be returned.
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: RealPathModule.java,v 1.3 2004/03/08 13:58:30 cziegeler Exp $
 */

/*
 * Note: the primary use for this is to support external code that wants a
 * filesystem path.  For example, The FOP 0.20.x serializer doesn't like
 * relative image paths, and doesn't understand Cocoon URLs (context:, cocoon:
 * etc).  So we pass the *2fo.xsl stylesheet a real filesystem path to where we
 * keep our images:
 *
 * <map:transform src="skins/{forrest:skin}/xslt/fo/document2fo.xsl">
 *    <map:parameter name="basedir" value="{realpath:resources}/"/>
 * </map:transform>
 *
 * And then prepend this to all image paths:
 *  ...
 *  <xsl:param name="basedir" select="''"/>
 *  ...
 *  <xsl:template match="img">
 *      <xsl:variable name="imgpath" select="concat($basedir, @src)"/>
 *      <fo:external-graphic src="{$imgpath}" ...
 *      ...
 *  </xsl:template>
 */
public class RealPathModule extends AbstractInputModule implements ThreadSafe {

    final static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("realPath");
        returnNames = tmp;
    }

    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException {

        String uri = ObjectModelHelper.getContext(objectModel).getRealPath(name);
        int lastCharPos = uri.length() - 1;
        if (uri.charAt(lastCharPos) == '\\') {
            uri = uri.substring(0, lastCharPos);
        }
        return uri;
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) throws ConfigurationException {

        return RequestURIModule.returnNames.iterator();
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

            List values = new LinkedList();
            values.add( this.getAttribute(name, modeConf, objectModel) );

            return values.toArray();
    }
}

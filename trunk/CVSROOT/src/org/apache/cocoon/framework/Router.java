/*>$File$ -- $Id: Router.java,v 1.2 1999-11-09 02:21:58 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.framework;

import java.io.*;
import java.util.*;

/**
 * This class implements a basic Router implementation that is used
 * by those classes that must assign the execution of a particular
 * pluggable instance depending on some "type reaction".
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 1999-11-09 02:21:58 $
 */

public abstract class Router extends AbstractActor implements Configurable, Status {

    protected Hashtable objects;
    protected String defaultType;

	public void init(Configurations conf) {
        Factory factory = (Factory) director.getActor("factory");
        this.defaultType = (String) conf.get("default");
        this.objects = new Hashtable();
        
        Configurations types = conf.getConfigurations("type");
        Enumeration e = types.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
            String name = (String) types.get(type);
            objects.put(type, factory.create(name, conf));
        }
	}
    
	public String getStatus() {
        StringBuffer buffer = new StringBuffer();
        if (defaultType != null) buffer.append("<li><b>Default type</b> = " + defaultType);
        Enumeration e = objects.keys();
        while (e.hasMoreElements()) {
            String type = (String) e.nextElement();
	        Object o = objects.get(type);
            buffer.append("<li><b>" + type + "</b>");
            if (o instanceof Status) {
                buffer.append(": " + ((Status) o).getStatus());
            }
            buffer.append("</li>");
        }
        return buffer.toString();
	}
}
/*-- $Id: URLHandlerFactory.java,v 1.1 2000-01-03 01:39:00 stefano Exp $ --

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

package org.apache.cocoon;

import java.io.*;
import java.net.*;

/**
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */
 
public class URLHandlerFactory implements URLStreamHandlerFactory {
    
    private String home;
    
    public URLHandlerFactory(String home) {
        if (home == null) {
            this.home = System.getProperty("user.dir").replace('\\','/');
        } else {
            this.home = home;
        }
    }
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        try {
            if (protocol.equalsIgnoreCase("cocoon"))
                return new CocoonStreamHandler();
            else if (protocol.equalsIgnoreCase("resource"))
                return new ResourceStreamHandler();
            else return null;
        } catch (Exception e) {
            return null;
        }
    }

    private class CocoonStreamHandler extends URLStreamHandler {
        public URLConnection openConnection(URL u) throws IOException {
            if (u == null) return null;
            String file = u.getFile();
            for (int x = 0; x < file.length(); x++) {
                if (file.charAt(x) != '/') {
                    file = file.substring(x);
                    break;
                }
            }
            
            return new URL("file:///" + home + file).openConnection();
        }
    }

    private class ResourceStreamHandler extends URLStreamHandler {
        public URLConnection openConnection(URL u) throws IOException {
            String file = u.getHost() + u.getFile();
            URL x = ClassLoader.getSystemResource(file);
            if (x == null) throw new IOException("Resource not found: " + file);
            else return x.openConnection();
        }
    }
}
    
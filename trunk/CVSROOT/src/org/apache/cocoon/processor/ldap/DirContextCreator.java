/*
 * Copyright (c) 1999 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software and design ideas developed by the Java
 *    Apache Project (http://java.apache.org/)."
 *
 * 4. The names "Cocoon", "Cocoon Servlet" and "Java Apache Project" must
 *    not be used to endorse or promote products derived from this software
 *    without prior written permission.
 *
 * 5. Products derived from this software may not be called "Cocoon"
 *    nor may "Cocoon" and "Java Apache Project" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software and design ideas developed by the Java
 *    Apache Project (http://java.apache.org/)."
*
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Project. For more information
 * on the Java Apache Project please see <http://java.apache.org/>.
 */

package org.apache.cocoon.processor.ldap;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

/**
 * Utility methods for this processor.<br>
 * adapted from Donald Ball's SQLProcessor code.
 * @author <a href="mailto:jmbirchfield@proteus-technologies.com">James Birchfield</a>
 * @version 1.0 
 */

public class DirContextCreator {

    protected String initializer;
    protected String ldapserverurl;

	protected Properties env;

    public DirContextCreator(Properties props) {
        this.initializer = props.getProperty("initializer");
        this.ldapserverurl = props.getProperty("ldap-serverurl");
		env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, initializer);
		env.put(Context.PROVIDER_URL, ldapserverurl);
    }

    public DirContext getDirContext() throws Exception {
		return new InitialDirContext(env);
    }

}

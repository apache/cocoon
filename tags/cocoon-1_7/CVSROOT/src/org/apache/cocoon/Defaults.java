/*-- $Id: Defaults.java,v 1.10 2000-02-14 00:59:17 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

/**
 * The Cocoon strings.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.10 $ $Date: 2000-02-14 00:59:17 $
 */

public interface Defaults {

    public static final String NAME = "Cocoon";
    public static final String VERSION = "@version@";
    public static final String YEAR = "@year@";

    public static final String INIT_ARG = "properties";
    public static final String PROPERTIES = "cocoon.properties";
    public static final String INTERNAL_PROPERTIES = "org/apache/cocoon/" + PROPERTIES;

    public static final String HOME = "document.root";
    public static final String SHOW_STATUS = "selfservlet.enabled";
    public static final String STATUS_URL = "selfservlet.uri";
    public static final String STATUS_URL_DEFAULT = "/Cocoon.xml";
    public static final String ERROR_INTERNALLY = "handle.errors.internally";

    public static final String PARSER_PROP = "parser";
    public static final String PARSER_DEFAULT = "org.apache.cocoon.parser.XercesParser";
    public static final String TRANSFORMER_PROP = "transformer";
    public static final String TRANSFORMER_DEFAULT = "org.apache.cocoon.transformer.XalanTransformer";
    public static final String CACHE_PROP = "cache";
    public static final String CACHE_DEFAULT = "org.apache.cocoon.cache.CocoonCache";
    public static final String STORE_PROP = "store";
    public static final String STORE_DEFAULT = "org.apache.cocoon.store.CocoonStore";

    public static final String PRODUCER_PROP = "producer";
    public static final String PROCESSOR_PROP = "processor";
    public static final String FORMATTER_PROP = "formatter";
    public static final String BROWSERS_PROP = "browser";
    public static final String INTERPRETER_PROP = "interpreter";

    public static final String COCOON_PROCESS_PI = "cocoon-process";
    public static final String COCOON_FORMAT_PI = "cocoon-format";
    public static final String STYLESHEET_PI = "xml-stylesheet";

    public static final String DEFAULT_BROWSER = "default";

    public static final int LOOPS = 10;

    public static final boolean VERBOSE = true;
}
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
package org.apache.cocoon.webapps.session;

/**
 * The <code>Constants</code> used throughout the core of the session management.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionConstants.java,v 1.4 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface SessionConstants {

    /** The namespace used by the session transformers */
    String SESSION_NAMESPACE_URI = "http://apache.org/cocoon/session/1.0";

    /** Reserved Context: Request context */
    String REQUEST_CONTEXT = "request";

    /** Reserved Context: Temp */
    String TEMPORARY_CONTEXT = "temporary";

    /** The request parameter name for the form handling */
    String SESSION_FORM_PARAMETER = "sessionform";
}

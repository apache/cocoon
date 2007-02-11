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
package org.apache.cocoon.components.url;

import org.apache.avalon.framework.component.Component;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @deprecated by the new source resolving of avalon excalibur
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: URLFactory.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public interface URLFactory extends Component {

    String ROLE = "org.apache.cocoon.components.url.URLFactory";
    /**
     * Get an URL
     */
    URL getURL(String location) throws MalformedURLException;
    URL getURL(URL base, String location) throws MalformedURLException;
}

/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.configuration;

import java.util.Properties;

/**
 * This is an interface for custom components delivering properties to
 * configure Cocoon.
 * This component must be setup as a Spring bean in the root application
 * context.
 *
 * @version $Id$
 * @since 2.2
 */
public interface PropertyProvider {

    String ROLE = PropertyProvider.class.getName();

    Properties getProperties(Settings settings, String runningMode, String path);
}

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
package org.apache.cocoon.xml;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Logging entity resolver to assist in caching.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Id: LoggingEntityResolver.java,v 1.2 2004/03/08 14:04:00 cziegeler Exp $
 */
public class LoggingEntityResolver extends AbstractLogEnabled implements EntityResolver {

  protected EntityResolver resolver;
  protected Set dependencies;

  public LoggingEntityResolver(EntityResolver resolver) {
    this.resolver = resolver;
    dependencies = new HashSet();
  }

  public InputSource resolveEntity(String public_id, String system_id) throws SAXException,IOException {
    InputSource input_source = resolver.resolveEntity(public_id,system_id);
    dependencies.add(input_source);
    getLogger().debug("Dependency: "+input_source.getSystemId());
    return input_source;
  }

  public Set getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

}

package org.apache.cocoon.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.log.Logger;
import org.apache.avalon.logger.AbstractLoggable;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Logging entity resolver to assist in caching.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version CVS $Revision
 */
public class LoggingEntityResolver extends AbstractLoggable implements EntityResolver {

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

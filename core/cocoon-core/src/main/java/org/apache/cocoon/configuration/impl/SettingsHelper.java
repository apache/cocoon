/* 
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.cocoon.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.Settings;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * Helper class creating a settings object
 *
 * @version $Id$
 * @since 2.2
 */
public class SettingsHelper {

    /** Parameter map for the context protocol. */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /**
     * Read all property files from the given directory and apply them to the settings.
     */
    public static void readProperties(String          directoryName,
                                      Settings        s,
                                      Properties      properties,
                                      SourceResolver  resolver,
                                      Logger          logger) {
        Source directory = null;
        try {
            directory = resolver.resolveURI(directoryName, null, CONTEXT_PARAMETERS);
            if (directory.exists() && directory instanceof TraversableSource) {
                final List propertyUris = new ArrayList();
                final Iterator c = ((TraversableSource) directory).getChildren().iterator();
                while (c.hasNext()) {
                    final Source src = (Source) c.next();
                    if ( src.getURI().endsWith(".properties") ) {
                        propertyUris.add(src);
                    }
                }
                // sort
                Collections.sort(propertyUris, getSourceComparator());
                // now process
                final Iterator i = propertyUris.iterator();
                while ( i.hasNext() ) {
                    final Source src = (Source)i.next();
                    final InputStream propsIS = src.getInputStream();
                    logger.info("Reading settings from '" + src.getURI() + "'.");
                    properties.load(propsIS);
                    propsIS.close();
                }
            }
        } catch (IOException ignore) {
            logger.info("Unable to read properties from directory '" + directoryName + "' - Continuing initialization.");
            logger.debug("Unable to read properties from directory '" + directoryName + "'.", ignore);
        } finally {
            resolver.release(directory);
        }
    }

    /**
     * Return a source comparator
     */
    public static Comparator getSourceComparator() {
        return new SourceComparator();
    }

    protected final static class SourceComparator implements Comparator {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            if ( !(o1 instanceof Source) || !(o2 instanceof Source)) {
                return 0;
            }
            return ((Source)o1).getURI().compareTo(((Source)o2).getURI());
        }
    }
}

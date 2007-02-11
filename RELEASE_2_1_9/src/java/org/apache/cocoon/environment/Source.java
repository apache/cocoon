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
package org.apache.cocoon.environment;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Description of a source. This interface provides a simple interface
 * for accessing a source of data. The source of data is assumed to
 * <b>not change</b> during the lifetime of the Source object. If you
 * have a data source that can change its content and you want it to
 * reflect in Cocoon, use a {@link ModifiableSource} object instead.
 *
 * @deprecated Use the {@link org.apache.excalibur.source.Source} interface instead
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: Source.java,v 1.2 2004/03/05 13:02:54 bdelacretaz Exp $
 */

public interface Source extends Recyclable, XMLizable {
  /**
   * Get the last modification date of the source or 0 if it
   * is not possible to determine the date.
   */
  long getLastModified();

  /**
   * Get the content length of the source or -1 if it
   * is not possible to determine the length.
   */
  long getContentLength();

  /**
   * Return an <code>InputStream</code> object to read from the source.
   */
  InputStream getInputStream()
    throws ProcessingException, IOException;

  /**
   * Return an <code>InputSource</code> object to read the XML
   * content.
   *
   * @return an <code>InputSource</code> value
   * @exception ProcessingException if an error occurs
   * @exception IOException if an error occurs
   */
  InputSource getInputSource()
    throws ProcessingException, IOException;

  /**
   * Return the unique identifer for this source
   */
  String getSystemId();
}

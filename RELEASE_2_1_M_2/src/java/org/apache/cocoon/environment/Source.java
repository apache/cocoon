/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
 * @version CVS $Id: Source.java,v 1.1 2003/03/12 07:38:42 cziegeler Exp $
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

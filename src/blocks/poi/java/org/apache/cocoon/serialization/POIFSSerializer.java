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
package org.apache.cocoon.serialization;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.impl.poi.POIFSElementProcessor;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.xml.sax.SAXException;

/**
 *  An extension of ElementProcessorSerializer with extensions for dealing with
 *  the POIFS filesystem This is an abstract class. Concrete extensions need to
 *  implement the following methods:
 *  <ul>
 *    <li> String getMimeType()</li>
 *    <li> void doLocalPreEndDocument()</li>
 *    <li> void doLocalPostEndDocument()</li>
 *    <li> ElementProcessorFactory getElementProcessorFactory()</li>
 *    </li>
 *  </ul>
 *
 * @author   Marc Johnson (marc_johnson27591@hotmail.com)
 * @author   Nicola Ken Barozzi (nicolaken@apache.org)
 * @version CVS $Id: POIFSSerializer.java,v 1.3 2004/01/31 08:50:45 antonio Exp $
 */
public abstract class POIFSSerializer extends ElementProcessorSerializer
{
  private POIFSFileSystem _filesystem;

  /**
   *  Constructor
   */

  public POIFSSerializer() {
    super();
    _filesystem = new POIFSFileSystem();
  }

  /*
   *  ********** START implementation of ContentHandler **********
   */
  /**
   *  Receive notification of the end of a document.
   *
   *@exception  SAXException  if there is an error writing the document to the
   *      output stream
   */

  public void endDocument() throws SAXException {
    doLocalPreEndDocument();

    OutputStream stream = getOutputStream();

    if ( stream != null ) {
      try {
        _filesystem.writeFilesystem( stream );
      } catch ( IOException e ) {
        throw SAXExceptionFactory(
          "could not process endDocument event", e );
      }
    } else {
      throw SAXExceptionFactory(
        "no outputstream for writing the document!!" );
    }
    doLocalPostEndDocument();
  }

  /**
   *  Provide access to the filesystem for extending classes
   *
   *@return    the filesystem
   */

  protected POIFSFileSystem getFilesystem() {
    return _filesystem;
  }

  /**
   *  Extending classes should do whatever they need to do prior to writing the
   *  filesystem out
   */

  protected abstract void doLocalPreEndDocument();

  /**
   *  Extending classes should do whatever they need to do after writing the
   *  filesystem out
   */

  protected abstract void doLocalPostEndDocument();

  /**
   *  perform pre-initialization on an element processor
   *
   *@param  processor         the element processor to be iniitialized
   *@exception  SAXException  on errors
   */

  protected void doPreInitialization(ElementProcessor processor)
      throws SAXException {
    try {
      ((POIFSElementProcessor)processor).setFilesystem(_filesystem);
    } catch (ClassCastException e) {
      throw SAXExceptionFactory( "could not pre-initialize processor", e );
    }
  }

  /*
   *  **********  END  implementation of ContentHandler **********
   */
}
// end public abstract class POIFSSerializer

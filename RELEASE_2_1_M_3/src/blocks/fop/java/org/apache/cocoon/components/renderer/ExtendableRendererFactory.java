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
package org.apache.cocoon.components.renderer;

import org.apache.fop.render.Renderer;
import org.apache.fop.render.pcl.PCLRenderer;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.render.ps.PSRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * An extendable FOP Renderer factory.
 * When given a MIME type, find a Renderer which supports that MIME
 * type. This factory is extendable as new <code>Renderer</code>s can
 * be added at runtime.
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: ExtendableRendererFactory.java,v 1.1 2003/03/09 00:03:50 pier Exp $
 */
public class ExtendableRendererFactory implements RendererFactory {

  protected static Map renderers = new HashMap();

  protected final static RendererFactory singleton = new ExtendableRendererFactory();

  private ExtendableRendererFactory() {
    // Add the default renderers which come with Apache FOP.
    addRenderer("application/pdf", PDFRenderer.class);
    addRenderer("application/postscript", PSRenderer.class);
    addRenderer("application/vnd.hp-PCL", PCLRenderer.class);
  }

  /**
   * Get a reference to this Renderer Factory.
   */
  public final static RendererFactory getRendererFactoryImplementation() {
    return singleton;
  }

  /**
   * Create a renderer for a specified MIME type.
   * @param mimeType The MIME type of the destination format
   * @return A suitable renderer, or <code>null</code> if one cannot be found
   */
  public Renderer createRenderer(String mimeType) {
    Class rendererClass = (Class)renderers.get(mimeType);
    if (rendererClass == null) {
      return null;
    } else {
      try {
        return (Renderer)rendererClass.newInstance();
      } catch (Exception ex) {
        return null;
      }
    }
  }

  /**
   * Add a mapping from the specified MIME type to a renderer.
   * Note: The renderer must have a no-argument constructor.
   * @param mimeType The MIME type of the Renderer
   * @param rendererClass The <code>Class</code> object for the Renderer.
   */
  public void addRenderer(String mimeType, Class rendererClass) {
    renderers.put(mimeType, rendererClass);
  }

  /**
   * Remove the mapping from a specified MIME type.
   * @param mimeType The MIME type to remove from the mapping.
   */
  public void removeRenderer(String mimeType) {
    renderers.remove(mimeType);
  }
}

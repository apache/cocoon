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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * Abstract base class for inspectors that can calculate 
 * the size of an image of a particular type.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractImageSourceInspector
    extends AbstractLogEnabled
    implements SourceInspector {

    /** 
     * The namespace uri of the properties exposed by this SourceInspector.
     * <p>
     * The value is <code>http://apache.org/cocoon/inspector/image/1.0</code>.
     * </p>
     */
    public static final String PROPERTY_NS = "http://apache.org/cocoon/inspector/image/1.0";
    
    /**
     * <code>width</code> property name.
     */
    public static final String IMAGE_WIDTH_PROPERTY_NAME = "width";
    
    /**
     * <code>height</code> property name.
     */
    public static final String IMAGE_HEIGHT_PROPERTY_NAME = "height";
    
    private static final String[] HANDLED_PROPERTIES = new String[] {
        PROPERTY_NS + "#" + IMAGE_HEIGHT_PROPERTY_NAME,
        PROPERTY_NS + "#" + IMAGE_WIDTH_PROPERTY_NAME
    };
    
    
    public AbstractImageSourceInspector() {
    }
    
    public final SourceProperty getSourceProperty(Source source, String namespace, String name)
        throws SourceException {

        if (handlesProperty(namespace,name) && isImageMimeType(source) && isImageFileType(source)) {
            if (name.equals(IMAGE_WIDTH_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, 
                                          String.valueOf(getImageSize(source)[0]));
            if (name.equals(IMAGE_HEIGHT_PROPERTY_NAME))
                return new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME,
                                          String.valueOf(getImageSize(source)[1]));
        }
        return null;
    }
    
    public final SourceProperty[] getSourceProperties(Source source) throws SourceException {
        if (isImageMimeType(source) && isImageFileType(source)) {
            int[] size = getImageSize(source);
            return new SourceProperty[] {
                new SourceProperty(PROPERTY_NS, IMAGE_WIDTH_PROPERTY_NAME, String.valueOf(size[0])),
                new SourceProperty(PROPERTY_NS, IMAGE_HEIGHT_PROPERTY_NAME, String.valueOf(size[1]))
            };
        }
        return null;
    }
    
    public final String[] getHandledPropertyTypes() {
        return HANDLED_PROPERTIES;
    }
    
    /**
     * Check whether this inspector handles properties of the given kind.
     */
    private final boolean handlesProperty(String namespace, String name) {
        return namespace.equals(PROPERTY_NS) && 
                   (name.equals(IMAGE_WIDTH_PROPERTY_NAME) || 
                   name.equals(IMAGE_HEIGHT_PROPERTY_NAME));
    }
    
    /**
     * Checks whether the mime mapping yields the type this inspector
     * handles.
     * 
     * @param source  the Source to test
     */
    protected abstract boolean isImageMimeType(Source source);
    
    /**
     * Inspects the input stream to verify this is in fact a file
     * of the type that this inspector handles.
     * 
     * @param source  the Source to test
     */
    protected abstract boolean isImageFileType(Source source) throws SourceException;
    
    /**
     * Calculate the width and the height of the image represented by source.
     * 
     * @param source  the Source to inspect.
     * @return  array carrying the calculated width parameter
     * in its 0 index, the height under index 1.
     */
    protected abstract int[] getImageSize(Source source) throws SourceException;

}

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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

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
    
    private static final SourceValidity VALIDITY = new NOPValidity();
    
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
    public final boolean handlesProperty(String namespace, String name) {
        return namespace.equals(PROPERTY_NS) && 
                   (name.equals(IMAGE_WIDTH_PROPERTY_NAME) || 
                   name.equals(IMAGE_HEIGHT_PROPERTY_NAME));
    }
    
    /**
     * Returns NOPValidity
     */
    public SourceValidity getValidity(Source source) {
        return VALIDITY;
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

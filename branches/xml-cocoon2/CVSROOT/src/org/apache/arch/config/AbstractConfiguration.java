/*****************************************************************************
 * Copyright (C) $YEAR$ The Apache Software Foundation. All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch.config;

/**
 * This is an abstract implementation that deals with 
 * methods that can be abstracted away from underlying implementations.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:47 $
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */

public abstract class AbstractConfiguration implements Configuration {
    
    public int getValueAsInt() {
        return Integer.parseInt(getValue());
    }

    public long getValueAsLong() {
        return Long.parseLong(getValue());
    }

    public float getValueAsFloat() {
        return Float.valueOf(getValue()).floatValue();
    }

    public boolean getValueAsBoolean() {
        return "true".equals(getValue().toLowerCase());
    }
    
    public int getAttributeAsInt(String name){
        return Integer.parseInt(getAttribute(name));
    }

    public long getAttributeAsLong(String name) {
        return Long.parseLong(getAttribute(name));
    }

    public float getAttributeAsFloat(String name) {
        return Float.valueOf(getAttribute(name)).floatValue();
    }

    public boolean getAttributeAsBoolean(String name) {
        return "true".equals(getAttribute(name).toLowerCase());
    }
}
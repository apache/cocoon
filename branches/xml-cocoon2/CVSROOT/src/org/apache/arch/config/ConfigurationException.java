/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.config;

/**
 * Thrown when a <code>Configurable</code> component cannot be configured
 * properly.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:33:03 $
 */
public class ConfigurationException extends Exception {

    /** The current configuration */
    private Configuration configuration=null;

    /**
     * Construct a new <code>ConfigurationException</code> instance.
     *
     * @param message The detail message for this exception (mandatory).
     * @param conf The configuration element.
     */
    public ConfigurationException(String message, Configuration conf) {
        super(message);
        this.configuration=conf;
    }
    
    /**
     * Return the <code>Configuration</code> element associated with this
     * <code>ConfigurationException</code> or <b>null</b>.
     */
    public Configuration getConfiguration() {
        return(this.configuration);
    }
    
    /**
     * Return this <code>ConfigurationException</code> (if possible with
     * location information).
     */
    public String getMessage() {
        String location=null;
        if (this.configuration!=null) location=this.configuration.getLocation();
        if (location==null) return(super.getMessage());
        else return(super.getMessage()+" "+location);
    }
}

/*
 * Copyright 2003 BASF IT Services
 * All rights reserved.
 * 
 * Created on 14.12.2003
 */
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * @author <a href="mailto:christian.haul@basf-it-services.com">Christian Haul</a>
 * @version CVS $Id: ModuleHolder.java,v 1.1 2004/02/15 19:08:53 haul Exp $
 */
public class ModuleHolder {

    public String name = null;
    public InputModule input = null;
    public Configuration config = null;

    public ModuleHolder() {
        super();
    }

    public ModuleHolder(String name, Configuration config) {
        this();
        this.name = name;
        this.config = config;
    }
    
    public ModuleHolder(String name, Configuration config, InputModule input) {
        this(name, config);
        this.input = input;
    }

}

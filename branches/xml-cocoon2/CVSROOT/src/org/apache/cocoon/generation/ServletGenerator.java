/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.util.Map;

import javax.servlet.ServletContext; 
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.utils.Parameters;

import org.xml.sax.EntityResolver;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-09-02 21:12:36 $
 */
public abstract class ServletGenerator extends ComposerGenerator
implements Composer {

    protected HttpServletRequest request=null;
    protected HttpServletResponse response=null;
    protected ServletContext context=null;

    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par) {
      super.setup(resolver, objectModel, src, par);
      this.request = (HttpServletRequest) objectModel.get("request");
      this.response = (HttpServletResponse) objectModel.get("response");
      this.context = (ServletContext) objectModel.get("context");
    }
}

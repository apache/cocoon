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
package org.apache.cocoon.components.source;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.SourceResolverImpl;

/**
 * This is the default implementation of the {@link SourceResolver} for
 * Cocoon.
 * @since 2.2
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonSourceResolver.java,v 1.1 2004/05/25 07:28:26 cziegeler Exp $
*/
public class CocoonSourceResolver 
extends SourceResolverImpl
implements SourceResolver {

    /** A (optional) custom source resolver */
    protected org.apache.excalibur.source.SourceResolver customResolver;
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public Source resolveURI(String location, String baseURI, Map parameters)
    throws MalformedURLException, IOException, SourceException {
        if ( baseURI == null ) {
            final Processor processor = EnvironmentHelper.getCurrentProcessor();
            if ( processor != null ) {
                baseURI = processor.getContext();
            }
        }
        if ( this.customResolver != null ) {
            return this.customResolver.resolveURI(location, baseURI, parameters);
        } else {
            return super.resolveURI(location, baseURI, parameters);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public Source resolveURI(String location)
    throws MalformedURLException, IOException, SourceException {
        return this.resolveURI(location, null, null);
    }

    /** 
     * Obtain a reference to the SourceResolver with "/Cocoon" hint
     * 
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        if ( manager.hasService(org.apache.excalibur.source.SourceResolver.ROLE+"/Cocoon")) {
            this.customResolver = (org.apache.excalibur.source.SourceResolver)
               manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE+"/Cocoon");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.m_manager != null ) {
            this.m_manager.release( this.customResolver );
            this.customResolver = null;
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        if ( this.customResolver != null ) {
            this.customResolver.release( source );
        } else {
            super.release(source);
        }
    }

}

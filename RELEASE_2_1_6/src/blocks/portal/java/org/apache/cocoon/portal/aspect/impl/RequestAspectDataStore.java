/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.aspect.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;

/**
 * An aspect data store is a component that manages aspect data objects.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: RequestAspectDataStore.java,v 1.7 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class RequestAspectDataStore 
    extends TemporaryAspectDataStore
    implements Parameterizable {
    
    protected String requestParameterName;
    
    public void setAspectData(Aspectalizable owner, String aspectName, Object data) {
        super.setAspectData(owner, aspectName, data);

        // create persistence
        ChangeAspectDataEvent e;
        if ( owner instanceof CopletInstanceData) {
            e = new ChangeCopletInstanceAspectDataEvent((CopletInstanceData)owner, aspectName, data);
        } else {
            e = new ChangeAspectDataEvent( owner, aspectName, data );
        }
        if ( this.requestParameterName != null ) {
            e.setRequestParameterName( this.requestParameterName );
        }
        LinkService service = null;
        try {
            service = (LinkService)this.manager.lookup(LinkService.ROLE);
            service.addEventToLink( e );
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup link service.", ce);
        } finally {
            this.manager.release( service );
        }
        
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters pars) throws ParameterException {
        requestParameterName = pars.getParameter("parameter-name", null);
    }

}

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.cron.ConfigurableCronJob;
import org.apache.cocoon.components.cron.CronJob;
import org.apache.cocoon.components.cron.JobScheduler;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

/**
 * Default implementation of the refresher
 * 
 * <component class="org.apache.cocoon.components.source.impl.RefresherImpl" role="org.apache.cocoon.components.source.impl.Refresher"/>
 * 
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RefresherImpl.java,v 1.1 2003/09/04 12:42:34 cziegeler Exp $
 */
public class RefresherImpl 
    extends AbstractLogEnabled
    implements Parameterizable, ThreadSafe, Refresher, Composable, Disposable, CronJob {
    
    protected String schedulerTarget;
    
    protected ComponentManager manager;
    
    protected JobScheduler   scheduler;
    
    protected Map            entries = Collections.synchronizedMap(new HashMap());
    
    protected SourceResolver resolver;
    
    protected Source         writeSource;
    
    /** changed */
    protected boolean changed = false;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.schedulerTarget = parameters.getParameter("scheduler-target");
        int writeInterval = parameters.getParameterAsInteger("write-interval", 0);
        if ( writeInterval > 0) {
            try {
                this.writeSource = this.resolver.resolveURI(parameters.getParameter("write-source"));
            } catch (IOException ioe) {
                throw new ParameterException("Error getting write-source.", ioe);
            }
            if (!(this.writeSource instanceof ModifiableSource)) {
                throw new ParameterException("Write-source is not modifiable.");
            }
            SAXConfigurationHandler b = new SAXConfigurationHandler();
            try {
                SourceUtil.toSAX(this.manager, this.writeSource, this.writeSource.getMimeType(), b);
            } catch (Exception ignore) {
                this.getLogger().warn("Unable to read configuration from " + this.writeSource.getURI());
            }
            final Configuration conf = b.getConfiguration();
            if ( conf != null ) {
                final Configuration[] childs = conf.getChildren("target");
                if ( childs != null ) {
                    for(int i=0; i < childs.length; i++) {
                        try {
                            String uri = URLDecoder.decode(childs[i].getAttribute("uri"));
                            int   expires = childs[i].getAttributeAsInteger("expires");
                            String cache = childs[i].getAttribute("cache");                        
                            String key = URLDecoder.decode(childs[i].getAttribute("key"));
                            SimpleCacheKey cacheKey = new SimpleCacheKey(key, false);
                            TargetConfiguration tc = new TargetConfiguration(cacheKey, uri, expires, cache);
                            this.entries.put(key, tc);
                            final String name = cacheKey.getKey();
                            
                            this.scheduler.addPeriodicJob(name, this.schedulerTarget,
                                                  (long)expires,
                                                  true,
                                                  tc.parameters,
                                                  tc.map);

                        } catch (CascadingException ignore) {
                        }
                    
                    }
                }
            }
            try {
                this.scheduler.addPeriodicJob(this.getClass().getName(), 
                                      parameters.getParameter("write-target"),
                                      (long)writeInterval * 1000,
                                      true,
                                      null,
                                      null);
            } catch (CascadingException ignore) {
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.source.impl.Refresher#refresh(org.apache.cocoon.caching.SimpleCacheKey, java.lang.String, long, java.lang.String)
     */
    public void refresh(SimpleCacheKey cacheKey,
                        String uri,
                        long expires,
                        String cacheRole) 
    throws SourceException {
        TargetConfiguration conf = new TargetConfiguration(cacheKey, uri, expires, cacheRole);

        // call the target the first time
        ConfigurableCronJob t = null;
        final String name = cacheKey.getKey();
        try {
            t = (ConfigurableCronJob)this.manager.lookup(this.schedulerTarget);
            t.setup(conf.parameters, conf.map);
            t.execute(name);
        } catch (ComponentException ce) {
            throw new SourceException("Unable to lookup target " + this.schedulerTarget, ce);
        } finally {
            this.manager.release((Component)t);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.scheduler = (JobScheduler)this.manager.lookup(JobScheduler.ROLE);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( (Component)this.scheduler );
            this.scheduler = null;
            this.manager = null;
            if ( this.resolver != null ) {
                this.resolver.release( this.writeSource );
                this.writeSource = null;
                this.manager.release( this.resolver );
                this.resolver = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.source.impl.Refresher#refreshPeriodically(org.apache.cocoon.caching.SimpleCacheKey, java.lang.String, long, java.lang.String)
     */
    public void refreshPeriodically(SimpleCacheKey cacheKey,
                                    String uri,
                                    long expires,
                                    String cacheRole) {
        TargetConfiguration conf = (TargetConfiguration) this.entries.get(cacheKey);
        if ( conf == null ) {
            conf = new TargetConfiguration(cacheKey, uri, expires, cacheRole);
            this.entries.put(cacheKey, conf);
        } else {
            conf.update(uri, expires, cacheRole);
        }

        final String name = cacheKey.getKey();
        try {
            this.scheduler.addPeriodicJob(name, this.schedulerTarget,
                                  (long)expires,
                                  true,
                                  conf.parameters,
                                  conf.map);
        } catch (CascadingException ignore) {
        }

        this.changed = true;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.cornerstone.services.scheduler.Target#targetTriggered(java.lang.String)
     */
    public void execute(String name) {
        if ( this.changed && this.writeSource != null ) {
            this.changed = false;
            try {
                OutputStream stream = ((ModifiableSource)this.writeSource).getOutputStream();
                Writer writer = new OutputStreamWriter(stream);
                
                writer.write("<targets>\n");
                final Iterator iter = this.entries.values().iterator();
                while ( iter.hasNext() ) {
                    
                    final TargetConfiguration c = (TargetConfiguration)iter.next();
                    writer.write("<target uri=\"");
                    writer.write(URLEncoder.encode(c.parameters.getParameter("uri", "")));
                    writer.write("\" expires=\"");
                    writer.write(c.parameters.getParameter("cache-expires", "0"));
                    writer.write("\" cache=\"");
                    writer.write(c.parameters.getParameter("cache-role", ""));
                    writer.write("\" key=\"");
                    writer.write(URLEncoder.encode(((SimpleCacheKey)c.map.get("cache-key")).getKey()));
                    writer.write("\"/>\n"); 
                }
                writer.write("</targets>\n");
                writer.flush();
                writer.close();
            } catch (IOException ignore) {
            }
            
        }
    }

    class TargetConfiguration {
        
        public Parameters parameters;
        
        public Map        map;
        
        public TargetConfiguration(SimpleCacheKey cacheKey,
                            String uri,
                            long expires,
                            String cacheRole) {
            this.parameters = new Parameters();
            this.map = new HashMap();

            this.update(uri, expires, cacheRole);
            this.map.put("cache-key", cacheKey);
        }
        
        public void update(String uri, long expires, String cacheRole) {
            this.parameters.setParameter("uri",uri);
            this.parameters.setParameter("cache-role", cacheRole);
            this.parameters.setParameter("cache-expires", String.valueOf(expires));
        }
        
    }
}

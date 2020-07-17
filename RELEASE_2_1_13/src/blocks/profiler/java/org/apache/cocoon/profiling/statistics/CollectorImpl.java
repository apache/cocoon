/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.profiling.statistics;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.auth.ApplicationUtil;
import org.apache.cocoon.auth.User;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * Implementation for the statistics component.
 * In order to prevent an out of memory exception we implement the Store
 * interface (dummy implementation) so we can register this component with
 * the StoreJanitor component which will invoke this component to free
 * memory in case of low memory.
 *
 * @version $Id$
 * @since 2.1.10
 */
public class CollectorImpl
    extends AbstractLogEnabled
    implements Collector, Store, ThreadSafe, Serviceable, Disposable, Contextualizable {

    private static final String COUNT_ATTRIBUTE = CollectorImpl.class.getName();

    /** Are we currently collecting? */
    protected boolean isCollecting = false;

    /** All reports. */
    protected Map reports = new HashMap();

    /** All page reports. */
    protected Map pageReports = new HashMap();

    /** The store janitor for registering ourself. */
    protected StoreJanitor janitor;

    /** The service manager. */
    protected ServiceManager manager;

    /** The component context. */
    protected Context context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context c) throws ContextException {
        this.context = c;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.janitor = (StoreJanitor)this.manager.lookup(StoreJanitor.ROLE);
        this.janitor.register(this);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this.janitor != null ) {
                this.janitor.unregister(this);
            }
            this.manager.release(this.janitor);
            this.janitor = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#addStatistics(org.apache.cocoon.profiling.statistics.Statistics)
     */
    public void addStatistics(final Statistics stats) {
        if ( this.isCollecting && stats != null ) {
            // get report for this category
            ReportImpl report = (ReportImpl)this.reports.get(stats.getCategory());
            if ( report == null ) {
                synchronized (this) {
                    report = (ReportImpl)this.reports.get(stats.getCategory());
                    if ( report == null ) {
                        // create new report
                        report = new ReportImpl(stats.getCategory());
                        this.reports.put(stats.getCategory(), report);
                    }
                }
            }
            synchronized (report) {
                report.add(stats);
            }
            final String pageKey = this.getRequestKey();
            PageReportImpl pageReport = (PageReportImpl)this.pageReports.get(pageKey);
            if ( pageReport == null ) {
                pageReport = new PageReportImpl(pageKey);
                this.pageReports.put(pageKey, pageReport);
            }
            pageReport.addStatistics(stats);
        }
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#getStatistics()
     */
    public Collection getStatistics() {
        return this.reports.values();
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#getPageReports()
     */
    public Collection getPageReports() {
        return this.pageReports.values();
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#isCollectingStatistics()
     */
    public boolean isCollectingStatistics() {
        return this.isCollecting;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#setCollectingStatistics(boolean)
     */
    public void setCollectingStatistics(boolean value) {
        this.isCollecting = value;
        if ( !this.isCollecting ) {
            this.reports.clear();
            this.pageReports.clear();
        }
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Collector#clear()
     */
    public void clear() {
        this.reports.clear();
        this.pageReports.clear();
    }

    protected String getRequestKey() {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        final User user = ApplicationUtil.getUser(objectModel);
        final Request request = ObjectModelHelper.getRequest(objectModel);
        final Session session = request.getSession();
        Integer counter = (Integer) request.getAttribute(CollectorImpl.COUNT_ATTRIBUTE);
        if ( counter == null) {
            counter = (Integer) session.getAttribute(CollectorImpl.COUNT_ATTRIBUTE);
            if ( counter == null ) {
                counter = new Integer(0);
            } else {
                counter = new Integer(counter.intValue() + 1);
            }
            session.setAttribute(CollectorImpl.COUNT_ATTRIBUTE, counter);
            request.setAttribute(CollectorImpl.COUNT_ATTRIBUTE, counter);
        }
        return (user == null ? "anon" : user.getId()) + ':' + session.getId() + '/' + counter;
    }

    /**
     * @see org.apache.excalibur.store.Store#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object arg0) {
        return false;
    }

    /**
     * @see org.apache.excalibur.store.Store#free()
     */
    public void free() {
        // we simply free everything
        synchronized ( this ) {
            this.reports.clear();
            this.pageReports.clear();
        }
    }

    /**
     * @see org.apache.excalibur.store.Store#get(java.lang.Object)
     */
    public Object get(Object arg0) {
        return null;
    }

    /**
     * @see org.apache.excalibur.store.Store#keys()
     */
    public Enumeration keys() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /**
     * @see org.apache.excalibur.store.Store#remove(java.lang.Object)
     */
    public void remove(Object arg0) {
        // nothing to do here
    }

    /**
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        return this.reports.size();
    }

    /**
     * @see org.apache.excalibur.store.Store#store(java.lang.Object, java.lang.Object)
     */
    public void store(Object arg0, Object arg1) throws IOException {
        // nothing to do here
    }
}

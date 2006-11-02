package org.apache.cocoon.classloader;

import org.apache.commons.jci.listeners.NotificationListener;
import org.apache.commons.jci.stores.ResourceStore;

/**
 * Wraps all the stores configured into the sitemap classloaders, in order to dispatch 
 * the notification event to the treeprocessor and force the component reloading in cocoon
 * TODO Extend TransactionalResourceStore, if store is not private
 */
public class SitemapNotifierStore implements ResourceStore {

    private NotificationListener sitemapProcessor;

    public SitemapNotifierStore(NotificationListener sitemapProcessor) {
        this.sitemapProcessor = sitemapProcessor;
    }
    
    public byte[] read(String pResourceName) {
        return null;
    }

    public void remove(String pResourceName) {
    }

    public void write(String pResourceName, byte[] pResourceData) {
        this.sitemapProcessor.handleNotification();
    }
}

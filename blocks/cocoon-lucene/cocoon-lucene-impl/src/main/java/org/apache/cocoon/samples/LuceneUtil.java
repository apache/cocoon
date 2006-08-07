/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.samples;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.search.LuceneCocoonHelper;
import org.apache.cocoon.components.search.LuceneCocoonIndexer;
import org.apache.cocoon.configuration.Settings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

/**
 * This is a sample helper class that can be used from flow to 
 * create an index.
 * @version $Id$
 */
public class LuceneUtil 
    implements Serviceable {

    private File workDir;
    private ServiceManager manager;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        final Settings settings = (Settings)this.manager.lookup(Settings.ROLE);
        this.workDir = new File(settings.getWorkDirectory());
        this.manager.release(settings);
    }

    public void createIndex(String baseURL, boolean create)
    throws ProcessingException {
        LuceneCocoonIndexer lcii = null;
        Analyzer analyzer = LuceneCocoonHelper.getAnalyzer( "org.apache.lucene.analysis.standard.StandardAnalyzer" );
        
        try {
        
            lcii = (LuceneCocoonIndexer)this.manager.lookup( LuceneCocoonIndexer.ROLE );
            Directory directory = LuceneCocoonHelper.getDirectory( new File( workDir, "index" ), create );
            lcii.setAnalyzer( analyzer );
            URL base_url = new URL( baseURL );
            lcii.index( directory, create, base_url );
        } catch (MalformedURLException mue) {
            throw new ProcessingException( "MalformedURLException in createIndex()!", mue );
        } catch (IOException ioe) {
            // ignore ??
            throw new ProcessingException( "IOException in createIndex()!", ioe );
        } catch (ServiceException ce) {
            // ignore ??
            throw new ProcessingException( "ServiceException in createIndex()!", ce );
        } finally {
            this.manager.release( lcii );
        }
    }
    
}

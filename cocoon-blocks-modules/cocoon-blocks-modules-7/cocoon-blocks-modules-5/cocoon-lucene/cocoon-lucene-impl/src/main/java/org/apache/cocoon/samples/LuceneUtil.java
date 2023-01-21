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
package org.apache.cocoon.samples;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.search.LuceneCocoonHelper;
import org.apache.cocoon.components.search.LuceneCocoonIndexer;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

/**
 * This is a sample helper class that can be used from flow to create an index.
 * 
 * @version $Id$
 */
public class LuceneUtil {

    private LuceneCocoonIndexer luceneCocoonIndexer;

    public void createIndex(String baseURL, boolean create) throws ProcessingException {
        Analyzer analyzer = LuceneCocoonHelper.getAnalyzer("org.apache.lucene.analysis.standard.StandardAnalyzer");

        try {
            final Settings settings = (Settings) WebAppContextUtils.getCurrentWebApplicationContext().getBean(
                    "org.apache.cocoon.configuration.Settings");
            Directory directory = LuceneCocoonHelper.getDirectory(new File(new File(settings.getWorkDirectory()),
                    "index"), create);
            getLuceneCocoonIndexer().setAnalyzer(analyzer);
            URL base_url = new URL(baseURL);
            getLuceneCocoonIndexer().index(directory, create, base_url);
        } catch (MalformedURLException mue) {
            throw new ProcessingException("MalformedURLException in createIndex()!", mue);
        } catch (IOException ioe) {
            // ignore ??
            throw new ProcessingException("IOException in createIndex()!", ioe);
        }
    }

    public void createIndex2(String baseURL, boolean create) throws ProcessingException {
        Analyzer analyzer = LuceneCocoonHelper.getAnalyzer("org.apache.lucene.analysis.standard.StandardAnalyzer");

        try {
            final Settings settings = (Settings) WebAppContextUtils.getCurrentWebApplicationContext().getBean(
                    "org.apache.cocoon.configuration.Settings");
            Directory directory = LuceneCocoonHelper.getDirectory(new File(new File(settings.getWorkDirectory()),
                    "index2"), create);
            getLuceneCocoonIndexer().setAnalyzer(analyzer);
            URL base_url = new URL(baseURL);
            getLuceneCocoonIndexer().index(directory, create, base_url);
        } catch (MalformedURLException mue) {
            throw new ProcessingException("MalformedURLException in createIndex2()!", mue);
        } catch (IOException ioe) {
            // ignore ??
            throw new ProcessingException("IOException in createIndex2()!", ioe);
        }
    }

    /**
     * @return the luceneCocoonIndexer
     */
    public LuceneCocoonIndexer getLuceneCocoonIndexer() {
        return luceneCocoonIndexer;
    }

    /**
     * @param luceneCocoonIndexer
     *            the luceneCocoonIndexer to set
     */
    public void setLuceneCocoonIndexer(LuceneCocoonIndexer luceneCocoonIndexer) {
        this.luceneCocoonIndexer = luceneCocoonIndexer;
    }

}

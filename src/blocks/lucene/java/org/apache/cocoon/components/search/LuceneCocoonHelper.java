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
package org.apache.cocoon.components.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * This class encapsulates some helper methods.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: LuceneCocoonHelper.java,v 1.3 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public class LuceneCocoonHelper
{
    /**
     *Gets the directory attribute of the LuceneCocoonHelper class
     *
     * @param  directory        Description of Parameter
     * @param  create           Description of Parameter
     * @return                  The directory value
     * @exception  IOException  Description of Exception
     * @since
     */
    public static Directory getDirectory(File directory, boolean create) throws IOException {
        FSDirectory fsDirectory = FSDirectory.getDirectory(directory, create);
        return fsDirectory;
    }

    /**
     *Gets the analyzer attribute of the LuceneCocoonHelper class
     *
     * @param  analyzer_class_name  Description of Parameter
     * @return                      The analyzer value
     * @since
     */
    public static Analyzer getAnalyzer(String analyzer_class_name) {
        Analyzer analyzer = null;
        try {
            Class analyzer_class = Class.forName(analyzer_class_name);
            analyzer = (Analyzer) analyzer_class.newInstance();
        } catch (Exception e) {
        }
        return analyzer;
    }

    /**
     *Gets the indexReader attribute of the LuceneCocoonHelper class
     *
     * @param  directory        Description of Parameter
     * @return                  The indexReader value
     * @exception  IOException  Description of Exception
     * @since
     */
    public static IndexReader getIndexReader(Directory directory) throws IOException {
        IndexReader reader = IndexReader.open(directory);
        return reader;
    }

    /**
     *Gets the indexWriter attribute of the LuceneCocoonHelper class
     *
     * @param  index            Description of Parameter
     * @param  analyzer         Description of Parameter
     * @param  create           Description of Parameter
     * @return                  The indexWriter value
     * @exception  IOException  Description of Exception
     * @since
     */
    public static IndexWriter getIndexWriter(Directory index, Analyzer analyzer, boolean create) throws IOException {
        IndexWriter writer = new IndexWriter(index, analyzer, create);
        return writer;
    }
}


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
 * @version CVS $Id: LuceneCocoonHelper.java,v 1.2 2003/03/11 17:44:21 vgritsenko Exp $
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


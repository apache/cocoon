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
package org.apache.cocoon.tools.maven.daisy.export.strategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.daisycms.clientapp.adapter.DaisyClientException;
import org.daisycms.clientapp.adapter.DaisyDocument;
import org.daisycms.clientapp.adapter.DaisyDocumentProxy;
import org.daisycms.clientapp.adapter.transformer.impl.ResourceXsltTransformerSource;
import org.daisycms.clientapp.adapter.util.XMLUtils;
import org.daisycms.clientapp.maven.export.DaisyExportMojo.StreamingInformation;
import org.daisycms.clientapp.maven.export.strategy.DefaultExportStrategy;
import org.outerj.daisy.repository.PartNotFoundException;
import org.outerj.daisy.repository.RepositoryException;

public class CocoonExportStrategy extends DefaultExportStrategy {

    public StreamingInformation createStreamingInformation(DaisyDocument doc, String editUrl, String author, Log log) {
        long documentTypeId = doc.getDocument().getDocumentTypeId();
        StreamingInformation si = new StreamingInformation();

        si.originalData = doc.asByteArray();

        //   documentTypeId ==  2 --> SimpleDocument
        //   documentTypeId ==  5 --> CocoonDocument
        //   documentTypeId == 13 --> NewsItem
        //   documentTypeId == 12 --> SitemapComponent
        //   documentTypeId == 14 --> WebpageWithSidebar
        if(documentTypeId == 2 || documentTypeId == 5 || documentTypeId == 10 || documentTypeId == 12 || documentTypeId == 13 || documentTypeId == 14) {
            transformDocument(doc, editUrl, author, si, "org/apache/cocoon/tools/maven/daisy/export/strategy/cocoon-doc-2-xdoc.xslt");
        }
        //   documentTypeId ==  3 --> Image
        else if(documentTypeId == 3) {
            si.relativeName = PATH_RESOURCES_IMAGES + DaisyDocumentProxy.createUniqeFileName(doc) + ".img";
            try {
                si.data = doc.getDocument().getPart(3).getData();
            } catch (PartNotFoundException e) {
                throw new DaisyClientException("Problems occurred while accessing part id=3 of document id="
                        + doc.getDocId() + "'.", e);
            } catch (RepositoryException e) {
                throw new DaisyClientException("Problems occurred while accessing the Daisy repository.", e);
            }
        }
        //   documentTypeId ==  9 --> MultiMediaObject (flash hopefully)
        else if(documentTypeId == 9) {
            si.relativeName = PATH_RESOURCES_FLASH + DaisyDocumentProxy.createUniqeFileName(doc) + ".swf";
            log.debug("Trying to export flash object " + si.relativeName);
            try {
                // partTypeId == 13 --> MultiMediaData
                si.data = doc.getDocument().getPart(13).getData();
            } catch (PartNotFoundException e) {
                throw new DaisyClientException("Problems occurred while accessing the Daisy repository.", e);
            } catch (RepositoryException e) {
                throw new DaisyClientException("Problems occurred while accessing the Daisy repository.", e);
            }
            log.debug("The " + si.relativeName + " has been exported succesfully.");
        }
        else {
            log.warn("Document id=" + doc.getDocId() + " not streamed because there is no available serialization strategy. documentTypeId=" + documentTypeId);
            return null;
        }
        return si;
    }

    private void transformDocument(DaisyDocument doc, String editUrl, String author, StreamingInformation si, String stylesheet) {
        si.relativeName = PATH_XDOCS + DaisyDocumentProxy.createUniqeFileName(doc) + ".xml";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Map params = new HashMap();
        params.put("editUrl", editUrl + doc.getDocId() + "?branch=" + doc.getBranchId() + "&language=" + doc.getLanguageId());
        if(author != null) {
            params.put("author", author);
            params.put("documentName", doc.getDocument().getName());
        }

        XMLUtils.transform(
                new ByteArrayInputStream(doc.asByteArray()),
                baos,
                new ResourceXsltTransformerSource(stylesheet),
                params);
        si.data = baos.toByteArray();

        si.containsLinksToBeRewritten = true;
    }

}

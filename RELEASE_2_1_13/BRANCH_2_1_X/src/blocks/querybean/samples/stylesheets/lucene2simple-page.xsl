<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- CVS $Id: content2lucene.xsl 36225 2004-08-11 14:36:46Z vgritsenko $ -->
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:lucene="http://apache.org/cocoon/lucene/1.0" 
>
  <xsl:template match="lucene:index">
    <page>
      <h4 class="samplesGroup">Query Bean - Indexer</h4>
      <title>Query Bean - Indexer</title>
      <content>
        <p>
          <small>
            <a href="welcome">Welcome</a>
          </small>
        </p>
        <h3>What just happened?</h3>
        <ul>
            <li>A Flowscript made a collection of files to index by scanning samples/blocks for welcome.xml files.</li>
            <li>It passed this list to a JX Template, that output each file as a call to CInclude it's content.</li>
            <li>As the files were included, they were transformed by XSLT to define how they would be indexed.</li>
            <li>This was then passed to the IndexTransformer.</li>
        </ul>
        
        <p>You can now perform <a href="simple.html">searches</a></p>
        
        <h3>The following URLs were indexed :</h3>
        
        <table style="margin-left:20px;">
          <tr><th>url</th><th>elapsed-time</th></tr>
          <xsl:apply-templates/>
        </table>
        
        <h3>The following parameters were supplied :</h3>
        
        <ul>
          <li>merge-factor - <xsl:value-of select="@merge-factor"/></li>
          <li>create - <xsl:value-of select="@create"/></li>
          <li>directory - <xsl:value-of select="@directory"/></li>
          <li>analyzer - <xsl:value-of select="@analyzer"/></li>
        </ul>
      </content>
    </page>
  </xsl:template>

  <xsl:template match="lucene:document">
    <tr>
      <td><xsl:value-of select="@url"/></td>
      <td><xsl:value-of select="@elapsed-time"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1"></xsl:template>
  <xsl:template match="text()" priority="-1"></xsl:template>
  
</xsl:stylesheet> 
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

<!--
  - $Id$
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
    <wml>
      <card id="index" title="{title}">
        <xsl:apply-templates select="content"/>
        <do type="accept" label="About">
          <go href="#about"/>
        </do>
      </card>

      <card id="about" title="About">
        <onevent type="ontimer">
          <prev/>
        </onevent>
        <timer value="25"/>
        <p align="center">
          <br/>
          <br/>
          <small>
            Copyright &#xA9; @year@<br/>
            Apache Software Foundation.<br/>
            All rights reserved.
          </small>
        </p>
      </card>
    </wml>
  </xsl:template>
  
  <xsl:template match="para">
    <p align="center"><xsl:apply-templates/></p>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:ch="http://cocoonhive.org/portal/schema/2002"
  >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:param name="title"/>
	
	<xsl:template match="/">

          <ch:page xmlns:ch="http://cocoonhive.org/portal/schema/2002">
            <ch:menu>
              <ch:item>
                <ch:label>Home</ch:label>
                <ch:command>home</ch:command>
              </ch:item>
            </ch:menu>

            <ch:frame>
              <ch:title><xsl:value-of select="$title"/></ch:title>
              <ch:content>
                <xsl:copy-of select="."/>
              </ch:content>      
            </ch:frame>
          
          </ch:page>
	</xsl:template>

	<xsl:template match="*"/>
	
</xsl:stylesheet>

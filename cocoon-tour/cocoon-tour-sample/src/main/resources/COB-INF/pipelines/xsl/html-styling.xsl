<?xml version="1.0" encoding="iso-8859-1"?>

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

<!-- add some style to our HTML output -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <xsl:template id="main" match="/">
        <html>
            <head>
                <link rel="stylesheet" href="css/tour.css" type="text/css"/>
                <xsl:copy-of select="//head/*"/>
            </head>

            <body>
                <xsl:copy-of select="//body/*"/>
                <p class="footer">
                    This footer has been added by html-styling.xsl
                </p>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>

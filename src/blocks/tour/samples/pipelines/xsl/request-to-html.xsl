<?xml version="1.0" encoding="iso-8859-1"?>

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

<!-- simple transformation of RequestGenerator output to HTML -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:h="http://apache.org/cocoon/request/2.0"
>

    <xsl:template id="main" match="/">
        <html>
            <body>
                <h1>
                    Request to host <xsl:value-of select="//h:header[@name='Host']"/>
                </h1>
                <p>
                    Contains <xsl:value-of select="count(//h:header)"/> headers.
                </p>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>

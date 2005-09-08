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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <html>
      <head>
        <title>CForms / SQLTransformer sample</title>
        <link rel="stylesheet" type="text/css" href="/styles/main.css"/>
      </head>

      <body>
        <h1>CForms / SQLTransformer sample</h1>
        <p>
          This sample demonstrates a way of editing SQL data via CForms without having to write Java code.
        </p>
        <p>
          The database used is the "personnel" demo database, running on the embedded
          HSQL database provided by the "databases" block.
        </p>
        <p>
          Study the sitemap (in the sqldatabase subdirectory of the CForms samples) to see how the SQLTransformer is
          used to load data for the CForms binding, and to update data based on the document updated by CForms.
        </p>
        <p>
          Only editing of existing data is implemented for now, complete CRUD functionality can
          be implemented in the same way.
        </p>
        <p>
          If you look at the form definitions and SQLTransformer parameters , you'll see that most
          or all of it could be generated from a data dictionary or a database schema. This would
          of course be a welcome enhancement...
        </p>

        <h1>List of employees in database</h1>
        <p>
          Click one to edit, or use the XML links to view raw data:
        </p>
        <xsl:apply-templates/>
        <p>
          <a href="../">Back to Forms samples</a>
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="employee">
    <a href="{@id}.xml">XML</a>
    &#160;
    <a href="{@id}/edit">
      <xsl:value-of select="name"/>
    </a>
    <br/>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0"?>
<!-- $Id: esql.xsl,v 1.1.2.15 2000-12-21 21:06:39 bloritsch Exp $-->
<!--

 ============================================================================
                   The Apache Software License, Version 1.2
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsp="http://apache.org/xsp"
    xmlns:esql="http://apache.org/cocoon/SQL/v2"
    xmlns:xspdoc="http://apache.org/cocoon/XSPDoc/v1"
>
<xspdoc:title>the esql logicsheet</xspdoc:title>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-string">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        ""
        <xsl:for-each select="$content/node()">
          <xsl:choose>
            <xsl:when test="name(.)">
              <xsl:choose>
                <xsl:when test="namespace-uri(.)='http://apache.org/xsp' and local-name(.)='text'">
                  + "<xsl:value-of select="."/>"
                </xsl:when>
                <xsl:otherwise>
                  + <xsl:apply-templates select="."/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <xsp:structure>
        <xsp:include>java.sql.DriverManager</xsp:include>
        <xsp:include>java.sql.Connection</xsp:include>
        <xsp:include>java.sql.Statement</xsp:include>
        <xsp:include>java.sql.PreparedStatement</xsp:include>
        <xsp:include>java.sql.ResultSet</xsp:include>
        <xsp:include>java.sql.ResultSetMetaData</xsp:include>
        <xsp:include>java.sql.SQLException</xsp:include>
        <xsp:include>java.text.SimpleDateFormat</xsp:include>
        <xsp:include>java.text.DecimalFormat</xsp:include>
      </xsp:structure>
      <xsp:logic>

        class EsqlSession {
            Connection connection=null;
            boolean closeConnection = true;
            String query;
            Statement statement;
            PreparedStatement preparedStatement;
            ResultSet resultset;
            ResultSetMetaData resultsetMetadata;
            int count;
            int maxRows;
            int skipRows;
            boolean hasResultset;
            int updateCount;
        }

      </xsp:logic>
      <xsl:for-each select=".//esql:execute-query[not(@inner-method='no')]">
        <xsl:call-template name="generate-code">
          <xsl:with-param name="inner-method">yes</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="xsp:page/*[not(namespace-uri(.)='http://www.apache.org/1999/XSP/Core')]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsp:logic>

        Stack esqlSessions = new Stack();
        EsqlSession esqlSession = null;

      </xsp:logic>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" priority="-1">
    <xsl:value-of select="text()"/>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Indicates that a sql connection is going to be defined and one or
    more queries may be executed
  </xspdoc:desc>

  <xsl:template match="esql:execute-query">
    <xsl:choose>
      <xsl:when test="@inner-method='no'">
        <xsl:call-template name="generate-code">
          <xsl:with-param name="inner-method">no</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>esqlExecuteQuery_<xsl:value-of select="generate-id(.)"/>(esqlSessions,esqlSession);</xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="generate-code">
    <xsl:param name="inner-method"/>

    <xsl:variable name="use-connection">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:use-connection"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="driver">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:driver"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="dburl">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:dburl"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="username">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:username"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="password">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:password"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="max-rows">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:max-rows"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="skip-rows">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:skip-rows"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="query">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:query"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="statement">
      <xsl:call-template name="get-nested-string">
        <xsl:with-param name="content" select="esql:statement"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$inner-method='yes'">
          void esqlExecuteQuery_<xsl:value-of select="generate-id(.)"/>(
                  Stack esqlSessions,
                  EsqlSession esqlSession)
          throws Exception {
        </xsl:when>
        <xsl:when test="$inner-method='no'">
          {
        </xsl:when>
      </xsl:choose>

      if (esqlSession != null) {
          esqlSessions.push(esqlSession);
      }

      esqlSession = new EsqlSession();

      try {
          esqlSession.maxRows = Integer.parseInt(String.valueOf(<xsl:copy-of select="$max-rows"/>).trim());
      } catch (Exception esqlE) {
          cocoonLogger.debug("Row retrieval is not limited", esqlE);
          esqlSession.maxRows = -1;
      }

      try {
           esqlSession.skipRows = Integer.parseInt(String.valueOf(<xsl:copy-of select="$skip-rows"/>).trim());
      } catch (Exception esqlE) {
          cocoonLogger.debug("We are not skipping any rows", esqlE);
          esqlSession.skipRows = 0;
      }

      try {
          <xsl:choose>
            <xsl:when test="not(esql:use-connection or esql:dburl)">
              esqlSession.connection = ((EsqlSession)esqlSessions.peek()).connection;
              esqlSession.closeConnection = false;
            </xsl:when>
            <xsl:when test="esql:use-connection">
              <!-- FIXME - need to do avalon pooling here maybe? -->
            </xsl:when>
            <xsl:otherwise>
              ClassUtils.newInstance(String.valueOf(<xsl:copy-of select="$driver"/>).trim());

              <xsl:choose>
                <xsl:when test="esql:username">
                  esqlSession.connection = DriverManager.getConnection(
                          String.valueOf(<xsl:copy-of select="$dburl"/>).trim(),
                          String.valueOf(<xsl:copy-of select="$username"/>).trim(),
                          String.valueOf(<xsl:copy-of select="$password"/>).trim()
                      );
                </xsl:when>
                <xsl:otherwise>
                  esqlSession.connection = DriverManager.getConnection(
                          String.valueOf(<xsl:copy-of select="$dburl"/>).trim()
                      );
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>

          <xsl:choose>
            <xsl:when test="esql:query">
              esqlSession.query = String.valueOf(<xsl:copy-of select="$query"/>).trim();
              esqlSession.statement = esqlSession.connection.createStatement();
              esqlSession.hasResultset = esqlSession.statement.execute(esqlSession.query);
            </xsl:when>
            <xsl:when test="esql:statement">
              esqlSession.preparedStatement = esqlSession.connection.prepareStatement(String.valueOf(<xsl:copy-of select="$statement"/>).trim());
              esqlSession.statement = esqlSession.preparedStatement;

              <xsl:for-each select=".//esql:parameter">
                <xsl:text>esqlSession.preparedStatement.</xsl:text>

                <xsl:choose>
                  <xsl:when test="@type">
                    <xsl:variable name="type"><xsl:value-of select="concat(translate(substring(@type,0,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),substring(@type,1))"/></xsl:variable>
                    <xsl:text>set</xsl:text><xsl:value-of select="$type"/>(<xsl:value-of select="position()"/>,<xsl:call-template name="get-nested-content"><xsl:with-param name="content" select="."/></xsl:call-template>);
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text>setString(</xsl:text><xsl:value-of select="position()"/>,String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>).trim());
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>

              esqlSession.hasResultset = esqlSession.preparedStatement.execute();
            </xsl:when>
          </xsl:choose>

          if (esqlSession.hasResultset) {
              esqlSession.resultset = esqlSession.statement.getResultSet();
              esqlSession.resultsetMetadata = esqlSession.resultset.getMetaData();
              esqlSession.updateCount = -1;
              esqlSession.count = 0;

              if (esqlSession.skipRows &gt; 0) {
                  while (esqlSession.resultset.next()) {
                      esqlSession.count++;

                      if (esqlSession.count == esqlSession.skipRows) {
                          break;
                      }
                  }
              }

              boolean esqlResults_<xsl:value-of select="generate-id(.)"/> = false;

              while (esqlSession.resultset.next()) {
                  esqlResults_<xsl:value-of select="generate-id(.)"/> = true;
                  <xsl:apply-templates select="esql:results/*"/>

                  if ((esqlSession.maxRows != -1) &amp;&amp; ((esqlSession.count - esqlSession.skipRows) == (esqlSession.maxRows - 1))) {
                      break;
                  }

                  esqlSession.count++;
              }

              esqlSession.resultset.close();

              if (!esqlResults_<xsl:value-of select="generate-id(.)"/>) {
                  <xsl:apply-templates select="esql:no-results/*"/>
              }
          } else {
              esqlSession.updateCount = esqlSession.statement.getUpdateCount();
              <xsl:apply-templates select="esql:count-results/*"/>
          }

          esqlSession.statement.close();
      } catch (Exception esqlException) {
          cocoonLogger.error("esql XSP exception", esqlException);

          <xsl:if test="esql:error-results//esql:get-stacktrace">
            StringWriter esqlException_writer = new StringWriter();
            esqlException.printStackTrace(new PrintWriter(esqlException_writer));
          </xsl:if>

          <xsl:apply-templates select="esql:error-results/*"/>
      } finally {
          if (esqlSession.closeConnection) {
              if (esqlSession.connection != null) {
                  try {
                      esqlSession.connection.close();
                  } catch (SQLException esqlException) {
                      cocoonLogger.debug("Could not close DB connection", esqlException);
                  }
              }

              <xsl:if test="esql:use-connection">
                <!-- FIXME - need to release avalon pooling here maybe -->
              </xsl:if>
          }

          if (esqlSessions.empty()) {
              esqlSession = null;
          } else {
              esqlSession = (EsqlSession)esqlSessions.pop();
          }
      }
      } // Where does this match?
    </xsp:logic>
  </xsl:template>

  <xsl:template match="esql:statement//esql:parameter">"?"</xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    If the query has results, this element's children will be
    instantiated for each row in the result set
  </xspdoc:desc>

  <xsl:template match="esql:execute-query/esql:results">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    If the query has no results, this element's children will be
    instantiated once
  </xspdoc:desc>

  <xsl:template match="esql:execute-query/esql:no-results">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    If the query results in an error, this element's children will be
    instantiated once
  </xspdoc:desc>

  <xsl:template match="esql:execute-query/esql:error-results">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Results in a set of elements whose names are the names of the
    columns. the elements each have one text child, whose value is the
    value of the column interpreted as a string. No special formatting
    is allowed here. If you want to mess around with the names of the
    elements or the value of the text field, use the type-specific get
    methods and write out the result fragment yourself.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-columns">
    <xsp:logic>
      for (int esqlI=1; esqlI &lt;= esqlSession.resultsetMetadata.getColumnCount(); esqlI++) {
          String tagName = esqlSession.resultsetMetadata.getColumnName(esqlI);

          <xsp:element>
            <xsp:param name="name"><xsp:expr>tagName</xsp:expr></xsp:param>
            <xsp:expr>esqlSession.resultset.getString(esqlI)</xsp:expr>
          </xsp:element>
      }

      this.characters("\n");
    </xsp:logic>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a string
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-string" name="get-string">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getString(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a date. if a format
    attribute exists, its value is taken to be a date format string as
    defined in java.text.SimpleDateFormat, and the result is formatted
    accordingly.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-date">
    <xsl:choose>
      <xsl:when test="@format">
        <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>))</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a time. if a format
    attribute exists, its value is taken to be a date format string as
    defined in java.text.SimpleDateFormat, and the result is formatted
    accordingly.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-time">
    <xsl:choose>
      <xsl:when test="@format">
        <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>))</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a timestamp. if a format
    attribute exists, its value is taken to be a date format string as
    defined in java.text.SimpleDateFormat, and the result is formatted
    accordingly.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-timestamp">
    <xsl:choose>
      <xsl:when test="@format">
        <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>))</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as true or false
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-boolean">
    <xsl:choose>
      <xsl:when test="@string='true'">
        <xsp:expr><xsl:call-template name="get-resultset"/>.getBoolean(<xsl:call-template name="get-column"/>) ? "true" : "false"</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getBoolean(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a double. if a format
    attribute exists, its value is taken to be a decimal format string
    as defined in java.text.DecimalFormat, and the result is formatted
    accordingly.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-double">
    <xsl:choose>
      <xsl:when test="@format">
        <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Double(<xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)))</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a float. if a format
    attribute exists, its value is taken to be a decimal format string
    as defined in java.text.DecimalFormat, and the result is formatted
    accordingly.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-float">
    <xsl:choose>
      <xsl:when test="@format">
        <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Float(<xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)))</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr><xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as an integer
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-int">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getInt(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a long
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-long">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getLong(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column as a short
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-short">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getShort(<xsl:call-template name="get-column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the value of the given column interpeted as an xml
    fragment. the fragment is parsed by the default xsp parser and the
    document element is returned. if a root attribute exists, its
    value is taken to be the name of an element to wrap around the
    contents of the fragment before parsing.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-xml">
    <xsl:variable name="content">
      <xsl:choose>
        <xsl:when test="@root">
          <xsl:text>"&lt;</xsl:text>
          <xsl:value-of select="@root"/>
          <xsl:text>&gt;"+</xsl:text>
          <xsl:call-template name="get-string"/>
          <xsl:text>+"&lt;/</xsl:text>
          <xsl:value-of select="@root"/>
          <xsl:text>&gt;"</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="get-string"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>this.xspParser.parse(new InputSource(new StringReader(<xsl:copy-of select="$content"/>))).getDocumentElement()</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the position of the current row in the result set
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-row-number">
    <xsp:expr>esqlSession.count</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the name of the given column. the column mus tbe specified
    by number, not name.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-column-name">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnName(<xsl:value-of select="@column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the label of the given column. the column mus the
    specified by number, not name.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-column-label">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnLabel(<xsl:value-of select="@column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the name of the type of the given column. the column must
    be specified by number, not name.
  </xspdoc:desc>

  <xsl:template match="esql:results//esql:get-column-type-name">
    <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnTypeName(<xsl:value-of select="@column"/>)</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the message of the current exception
  </xspdoc:desc>

  <xsl:template match="esql:error-results//esql:get-message">
    <xsp:expr>esqlException.getMessage()</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the current exception as a string
  </xspdoc:desc>

  <xsl:template match="esql:error-results//esql:to-string">
    <xsp:expr>esqlException.toString()</xsp:expr>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Returns the stacktrace of the current exception
  </xspdoc:desc>

  <xsl:template match="esql:error-results//esql:get-stacktrace">
    <xsp:expr>esqlException_writer.toString()</xsp:expr>
  </xsl:template>

  <xsl:template name="get-resultset">
    <xsl:choose>
      <xsl:when test="@ancestor">
        <xsl:text>((EsqlSession)esqlSessions.elementAt(esqlSessions.size()-</xsl:text>
        <xsl:value-of select="@ancestor"/>
        <xsl:text>)).resultset</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>esqlSession.resultset</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- =========================================================== -->

  <xspdoc:desc>
    Used internally to determine which column is the given column. if
    a column attribute exists and its value is a number, it is taken
    to be the column's position. if the value is not a number, it is
    taken to be the column's name. if a column attribute does not
    exist, an esql:column element is assumed to exist and to render as
    a string (after all of the xsp instructions have been evaluated),
    which is taken to be the column's name.
  </xspdoc:desc>

  <xsl:template name="get-column">
    <xsl:choose>
      <xsl:when test="@column">
        <xsl:choose>
          <xsl:when test="not(string(number(@column))='NaN')">
            <xsl:value-of select="@column"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>"</xsl:text>
            <xsl:value-of select="@column"/>
            <xsl:text>"</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="esql:column">
        <xsl:call-template name="get-nested-string">
          <xsl:with-param name="content" select="esql:column"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

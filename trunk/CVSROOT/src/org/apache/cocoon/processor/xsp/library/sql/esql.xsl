<?xml version="1.0"?>
<!-- $Id: esql.xsl,v 1.28 2000-11-11 21:40:18 balld Exp $-->
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
  xmlns:xsp="http://www.apache.org/1999/XSP/Core"
  xmlns:esql="http://apache.org/cocoon/SQL/v2"
  xmlns:xspdoc="http://apache.org/cocoon/XSPDoc/v1"
>

<xsl:template name="get-nested-content">
  <xsl:param name="content"/>
  <xsl:choose>
    <xsl:when test="$content/*">
      <xsl:apply-templates select="$content/*"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$content"/>
    </xsl:otherwise>
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
            + <xsl:apply-templates select="."/>
          </xsl:when>
          <xsl:otherwise>
            + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      "<xsl:value-of select="normalize-space($content)"/>"
    </xsl:otherwise>
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
      <xsp:include>org.apache.turbine.services.db.PoolBrokerService</xsp:include>
      <xsp:include>org.apache.turbine.util.db.pool.DBConnection</xsp:include>
    </xsp:structure>
    <xsp:logic>
      static PoolBrokerService _esql_pool = PoolBrokerService.getInstance();
      class EsqlConnection {
        DBConnection db_connection = null;
        Connection connection = null;
        String dburl = null;
        String username = null;
        String password = null;
      }
      class EsqlQuery {
        String query;
        Statement statement;
        PreparedStatement prepared_statement;
        ResultSet resultset;
        ResultSetMetaData resultset_metadata;
        /** the position of the current row in the resultset **/
        int position = -1;
        int max_rows = -1;
        int skip_rows = 0;
        boolean results;
      }
    </xsp:logic>
    <xsl:apply-templates/>
  </xsp:page>
</xsl:template>

<xsl:template match="xsp:page/*[not(namespace-uri(.)='http://www.apache.org/1999/XSP/Core')]">
 <xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsp:logic>
    Stack _esql_connections = new Stack();
    EsqlConnection _esql_connection = null;
    Stack _esql_queries = new Stack();
    EsqlQuery _esql_query = null; 
    SQLException _esql_exception = null;
  </xsp:logic>
  <xsl:apply-templates/>
 </xsl:copy>
</xsl:template>

<xsl:template match="esql:connection">
  <xsl:variable name="driver"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:driver"/></xsl:call-template></xsl:variable>
  <xsl:variable name="dburl"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:dburl"/></xsl:call-template></xsl:variable>
  <xsl:variable name="username"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:username"/></xsl:call-template></xsl:variable>
  <xsl:variable name="password"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:password"/></xsl:call-template></xsl:variable>
  <xsl:variable name="pool"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:pool"/></xsl:call-template></xsl:variable>
  <xsp:logic>
    if (_esql_connection != null) {
      _esql_connections.push(_esql_connection);
    }
    _esql_connection = new EsqlConnection();
    try {
      <xsl:choose>
        <xsl:when test="esql:pool">
          _esql_connection.db_connection = _esql_pool.getConnection(String.valueOf(<xsl:copy-of select="$pool"/>));
          _esql_connection.connection = _esql_connection.db_connection.getConnection();
        </xsl:when>
        <xsl:otherwise>
          Class.forName(String.valueOf(<xsl:copy-of select="$driver"/>)).newInstance();
          <xsl:choose>
            <xsl:when test="esql:username">
              _esql_connection.connection = DriverManager.getConnection(
                String.valueOf(<xsl:copy-of select="$dburl"/>),
                String.valueOf(<xsl:copy-of select="$username"/>),
                String.valueOf(<xsl:copy-of select="$password"/>)
              );
            </xsl:when>
            <xsl:otherwise>
              _esql_connection.connection = DriverManager.getConnection(
                String.valueOf(<xsl:copy-of select="$dburl"/>)
              );
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    } finally {
      <xsl:choose>
        <xsl:when test="esql:pool">
          _esql_pool.releaseConnection(_esql_connection.db_connection);
        </xsl:when>
        <xsl:otherwise>
          _esql_connection.connection.close();
        </xsl:otherwise>
      </xsl:choose>
    }
    if (_esql_connections.empty()) {
      _esql_connection = null;
    } else {
      _esql_connection = (EsqlConnection)_esql_connections.pop();
    }
  </xsp:logic>
</xsl:template>

<xsl:template match="esql:connection/esql:driver"/>
<xsl:template match="esql:connection/esql:dburl"/>
<xsl:template match="esql:connection/esql:username"/>
<xsl:template match="esql:connection/esql:password"/>

<xsl:template match="esql:connection//esql:execute-query">
  <xsl:variable name="query"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:query"/></xsl:call-template></xsl:variable>
  <xsp:logic>
    if (_esql_query != null) {
      _esql_queries.push(_esql_query);
    }
    _esql_query = new EsqlQuery();
    _esql_query.query = String.valueOf(<xsl:copy-of select="$query"/>);
    try {
      <xsl:choose>
        <!-- this is a prepared statement -->
        <xsl:when test="esql:query//esql:parameter">
          _esql_query.prepared_statement = _esql_connection.connection.prepareStatement(_esql_query.query);
          _esql_query.statement = _esql_query.prepared_statement;
          <xsl:for-each select="esql:query//esql:parameter">
            <xsl:text>_esql_query.prepared_statement.</xsl:text>
            <xsl:choose>
              <xsl:when test="@type">
                <xsl:variable name="type"><xsl:value-of select="concat(translate(substring(@type,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),substring(@type,2))"/></xsl:variable>
                <xsl:text>set</xsl:text><xsl:value-of select="$type"/>(<xsl:value-of select="position()"/>,<xsl:call-template name="get-nested-content"><xsl:with-param name="content" select="."/></xsl:call-template>);<xsl:text>
  </xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>setString(</xsl:text><xsl:value-of select="position()"/>,String.valueOf(<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>));<xsl:text>
  </xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
          _esql_query.results = _esql_query.prepared_statement.execute();
        </xsl:when>
        <!-- this is a normal query -->
        <xsl:otherwise>
          _esql_query.statement = _esql_connection.connection.createStatement();
          _esql_query.results = _esql_query.statement.execute(_esql_query.query);
        </xsl:otherwise>
      </xsl:choose>
      if (_esql_query.results) {
        _esql_query.resultset = _esql_query.statement.getResultSet();
        _esql_query.resultset_metadata = _esql_query.resultset.getMetaData();
        _esql_query.position = 0;
        if (_esql_query.skip_rows &gt; 0) {
          while (_esql_query.resultset.next()) {
            _esql_query.position++;
            if (_esql_query.position == _esql_query.skip_rows) {
              break;
            }
          }
        }
        <xsl:apply-templates select="esql:results"/>
        _esql_query.resultset.close();
      } else {
        _esql_query.position = _esql_query.statement.getUpdateCount();
        if (_esql_query.position &gt;= 0) {
          <xsl:apply-templates select="esql:update-results/*"/>
        }
      }
      _esql_query.statement.close();
    } catch (SQLException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
      <xsl:choose>
        <xsl:when test="esql:error-results">
          _esql_exception = _esql_exception_<xsl:value-of select="generate-id(.)"/>;
          <xsl:apply-templates select="esql:error-results"/>
        </xsl:when>
        <xsl:otherwise>
          throw(_esql_exception_<xsl:value-of select="generate-id(.)"/>);
        </xsl:otherwise>
      </xsl:choose>
    }
    if (_esql_queries.empty()) {
      _esql_query = null;
    } else {
      _esql_query = (EsqlQuery)_esql_queries.pop();
    }
  </xsp:logic>
</xsl:template>

<xsl:template match="esql:query//esql:parameter">"?"</xsl:template>

<xsl:template match="esql:execute-query/esql:results" priority="2">
  <xsl:for-each select="esql:row-results[1]">
    <xsl:apply-templates select="preceding-sibling::*[not(name()='esql:no-results')]"/>
    <xsp:logic>
      while (_esql_query.resultset.next()) {
        <xsl:apply-templates/>
        if (_esql_query.max_rows != -1 &amp;&amp; _esql_query.position - _esql_query.skip_rows == _esql_query.max_rows-1) {
          break;
        }
        _esql_query.position++;
      }
    </xsp:logic>
    <xsl:apply-templates select="following-sibling::*[not(name()='esql:no-results')]"/>
    <xsl:if test="esql:no-results">
      <xsp:logic>
        if (_esql_query.position == _esql_query.skip_rows) {
          <xsl:apply-templates select="esql:no-results/*"/>
        }
      </xsp:logic>
    </xsl:if>
  </xsl:for-each>
</xsl:template>

<xspdoc:desc>results in a set of elements whose names are the names of the columns. the elements each have one text child, whose value is the value of the column interpreted as a string. No special formatting is allowed here. If you want to mess around with the names of the elements or the value of the text field, use the type-specific get methods and write out the result fragment yourself.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-columns">
  <xsp:logic>
    for (int _esql_i=1; _esql_i &lt;= _esql_query.resultset_metadata.getColumnCount(); _esql_i++) {
      Node _esql_node = document.createElement(_esql_query.resultset_metadata.getColumnName(_esql_i));
      _esql_node.appendChild(document.createTextNode(_esql_query.resultset.getString(_esql_i)));
      xspCurrentNode.appendChild(_esql_node);
    }
  </xsp:logic>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a string</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-string" name="get-string">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getString(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a date. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-date">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a time. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-time">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a timestamp. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-timestamp">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as true or false</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-boolean">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getBoolean(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a double. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-double">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Double(<xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getDouble(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a float. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-float">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Float(<xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as an integer</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-int">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getInt(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a long</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-long">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getLong(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a short</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-short">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getShort(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column interpeted as an xml fragment. the fragment is parsed by the default xsp parser and the document element is returned. if a root attribute exists, its value is taken to be the name of an element to wrap around the contents of the fragment before parsing.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-xml">
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

<xspdoc:desc>returns the position of the current row in the result set</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-row-position">
  <xsp:expr>_esql_query.position</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the name of the given column. the column mus tbe specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-column-name">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnName(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the label of the given column. the column mus tbe specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-column-label">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnLabel(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the name of the type of the given column. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-column-type-name">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnTypeName(<xsl:value-of select="@column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the message of the current exception</xspdoc:desc>
<xsl:template match="esql:error-results//esql:get-message">
  <xsp:expr>_esql_exception.getMessage()</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the current exception as a string</xspdoc:desc>
<xsl:template match="esql:error-results//esql:to-string">
  <xsp:expr>_esql_exception.toString()</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the stacktrace of the current exception</xspdoc:desc>
<xsl:template match="esql:error-results//esql:get-stacktrace">
  <xsp:expr>_esql_exception_writer.toString()</xsp:expr>
</xsl:template>

<xsl:template name="get-resultset">
  <xsl:call-template name="get-query"/><xsl:text>.resultset</xsl:text>
</xsl:template>

<xsl:template name="get-query">
  <xsl:choose>
    <xsl:when test="@ancestor">
      <xsl:text>((EsqlQuery)_esql_queries.elementAt(_esql_queries.size()-</xsl:text>
      <xsl:value-of select="@ancestor"/>
      <xsl:text>))</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>_esql_query</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>used internally to determine which column is the given column. if a column attribute exists and its value is a number, it is taken to be the column's position. if the value is not a number, it is taken to be the column's name. if a column attribute does not exist, an esql:column element is assumed to exist and to render as a string (after all of the xsp instructions have been evaluated), which is taken to be the column's name.</xspdoc:desc>
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

<xsl:template match="@*|node()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>

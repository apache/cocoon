<?xml version="1.0"?><!-- -*- xsl -*- -->
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

<!--
 * ESQL Logicsheet
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Revision: 1.9 $ $Date: 2004/03/17 11:28:17 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:esql="http://apache.org/cocoon/SQL/v2"
  xmlns:xspdoc="http://apache.org/cocoon/XSPDoc/v1"
>

<xsl:param name="XSP-ENVIRONMENT"/>
<xsl:param name="XSP-VERSION"/>
<xsl:param name="filename"/>
<xsl:param name="language"/>

<xsl:variable name="environment">Cocoon 2</xsl:variable>
<xsl:variable name="xsp-namespace-uri">http://apache.org/xsp</xsl:variable>

<xsl:variable name="prefix">esql</xsl:variable>

<xsl:template name="get-nested-content">
  <xsl:param name="content"/>
  <xsl:choose>
    <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
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
  <!-- if $content has sub-elements, concatenate them -->
    <xsl:when test="$content/*">
      ""
      <xsl:for-each select="$content/node()">
        <xsl:choose>
          <xsl:when test="name(.)">
          <!-- element -->
            <xsl:choose>
              <xsl:when test="namespace-uri(.)='http://apache.org/xsp' and local-name(.)='text'">
              <!-- xsp:text element -->
                + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
              </xsl:when>
              <xsl:otherwise>
              <!-- other elements -->
                + <xsl:apply-templates select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
          <!-- text node -->
            + "<xsl:value-of select="translate(.,'&#9;&#10;&#13;','   ')"/>"
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:when>
    <!-- else return the text value of $content -->
    <xsl:otherwise>"<xsl:value-of select="normalize-space($content)"/>"</xsl:otherwise>
  </xsl:choose>
</xsl:template>


  <xsl:template name="get-parameter">
    <xsl:param name="name"/>
    <xsl:param name="default"/>
    <xsl:param name="required">false</xsl:param>

    <xsl:variable name="qname">
      <xsl:value-of select="concat($prefix, ':param')"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="@*[name(.) = $name]">"<xsl:value-of select="@*[name(.) = $name]"/>"</xsl:when>
      <xsl:when test="(*[name(.) = $qname])[@name = $name]">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content"
                          select="(*[name(.) = $qname])[@name = $name]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($default) = 0">
            <xsl:choose>
              <xsl:when test="$required = 'true'">
                <xsl:call-template name="error">
                  <xsl:with-param name="message">[Logicsheet processor]
Parameter '<xsl:value-of select="$name"/>' missing in dynamic tag &lt;<xsl:value-of select="name(.)"/>&gt;
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>""</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise><xsl:copy-of select="$default"/></xsl:otherwise>
        </xsl:choose>
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
      <xsp:include>java.sql.CallableStatement</xsp:include>
      <xsp:include>java.sql.ResultSet</xsp:include>
      <xsp:include>java.sql.ResultSetMetaData</xsp:include>
      <xsp:include>java.sql.Struct</xsp:include>
      <xsp:include>java.sql.SQLException</xsp:include>
      <xsp:include>java.sql.Clob</xsp:include>
      <xsp:include>java.sql.Blob</xsp:include>
      <xsp:include>java.text.SimpleDateFormat</xsp:include>
      <xsp:include>java.text.DecimalFormat</xsp:include>
      <xsp:include>java.io.StringWriter</xsp:include>
      <xsp:include>java.io.PrintWriter</xsp:include>
      <xsp:include>java.io.BufferedInputStream</xsp:include>
      <xsp:include>java.io.InputStream</xsp:include>
      <xsp:include>java.util.Set</xsp:include>
      <xsp:include>java.util.List</xsp:include>
      <xsp:include>java.util.Iterator</xsp:include>
      <xsp:include>java.util.ListIterator</xsp:include>
      <xsp:include>java.math.BigDecimal</xsp:include>
      <xsp:include>java.sql.Struct</xsp:include>
      <xsp:include>java.sql.Types</xsp:include>
      <xsp:include>org.apache.cocoon.components.language.markup.xsp.EsqlHelper</xsp:include>
      <xsp:include>org.apache.cocoon.components.language.markup.xsp.AbstractEsqlQuery</xsp:include>
      <xsp:include>org.apache.cocoon.components.language.markup.xsp.AbstractEsqlConnection</xsp:include>
      <xsp:include>org.apache.cocoon.components.language.markup.xsp.Cocoon2EsqlConnection</xsp:include>
      <xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPUtil</xsp:include>
      <xsl:if test=".//esql:connection/esql:pool">
        <xsp:include>org.apache.avalon.excalibur.datasource.DataSourceComponent</xsp:include>
      </xsl:if>
    </xsp:structure>
    <xsp:logic>
      <xsl:call-template name="variables"><xsl:with-param name="modifier" select="'private'"/></xsl:call-template>

      <xsl:if test=".//esql:connection/esql:pool">
            private static ComponentSelector _esql_selector = null;

            protected ComponentSelector _esql_get_selector() throws org.apache.avalon.framework.component.ComponentException {
              if (_esql_selector == null) {
                try {
                  _esql_selector = (ComponentSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
                } catch (ComponentException cme) {
                  getLogger().error("Could not look up the datasource component", cme);
                }
              }
              return _esql_selector;
            }
      </xsl:if>

            protected void _esql_printObject ( Object obj, AttributesImpl xspAttr) throws SAXException
            {
               if ( obj instanceof List) {
           ListIterator j=((List)obj).listIterator();
           <xsp:element name="sql-list">
                     <xsp:logic>
                       while (j.hasNext()){
                      <xsp:element name="sql-list-item">
                        <xsp:attribute name="pos"><xsp:expr>j.nextIndex()</xsp:expr></xsp:attribute>
                        <xsp:logic>this._esql_printObject(j.next(),xspAttr);</xsp:logic>
                      </xsp:element>
                       };
                     </xsp:logic>
                   </xsp:element>
               } else if ( obj instanceof Set ) {
            Iterator j=((Set)obj).iterator();
            <xsp:element name="sql-set">
                      <xsp:logic>
                        while (j.hasNext()){
                           <xsp:element name="sql-set-item">
                 <xsp:logic>this._esql_printObject(j.next(),xspAttr);</xsp:logic>
                   </xsp:element>
                        };
                      </xsp:logic>
                    </xsp:element>
           } else {
              <xsp:content><xsp:expr>obj</xsp:expr></xsp:content>;
           }
        }
    </xsp:logic>
    <xsl:apply-templates/>
  </xsp:page>
</xsl:template>

<xsl:template name="variables">
  <xsl:param name="modifier" select="''"/>
  <xsp:logic>
    <xsl:value-of select="$modifier"/> Stack _esql_connections = new Stack();
    <xsl:value-of select="$modifier"/> Cocoon2EsqlConnection _esql_connection = null;
    <xsl:value-of select="$modifier"/> Stack _esql_queries = new Stack();
    <xsl:value-of select="$modifier"/> AbstractEsqlQuery _esql_query = null;
    <xsl:value-of select="$modifier"/> SQLException _esql_exception = null;
    <xsl:value-of select="$modifier"/> StringWriter _esql_exception_writer = null;
  </xsp:logic>
</xsl:template>

<xsl:template match="xsp:page/*[not(self::xsp:*)]">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="esql:connection">
  <xsl:variable name="driver"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:driver"/></xsl:call-template></xsl:variable>
  <xsl:variable name="dburl"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:dburl"/></xsl:call-template></xsl:variable>
  <xsl:variable name="username"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:username"/></xsl:call-template></xsl:variable>
  <xsl:variable name="password"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:password"/></xsl:call-template></xsl:variable>
  <xsl:variable name="pool"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:pool"/></xsl:call-template></xsl:variable>
  <xsl:variable name="autocommit"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:autocommit"/></xsl:call-template></xsl:variable>
  <xsl:variable name="allow-multiple-results"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:allow-multiple-results"/></xsl:call-template></xsl:variable>
  <xsp:logic>
    if (_esql_connection != null) {
      _esql_connections.push(_esql_connection);
    }
    try {
      <xsl:choose>
        <xsl:when test="esql:pool">
          try {
            _esql_connection = new Cocoon2EsqlConnection( (DataSourceComponent) _esql_get_selector().select(String.valueOf(<xsl:copy-of select="$pool"/>)) );
            setupLogger(_esql_connection);

            <xsl:if test="esql:allow-multiple-results">
             _esql_connection.setMultipleResults(String.valueOf(<xsl:copy-of select="$allow-multiple-results"/>));
            </xsl:if>
          } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
            getLogger().error("Could not get the datasource",_esql_exception_<xsl:value-of select="generate-id(.)"/>);
            throw new RuntimeException("Could not get the datasource "+_esql_exception_<xsl:value-of select="generate-id(.)"/>);
          }
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="esql:driver">
          try {
            Thread.currentThread().getContextClassLoader().loadClass(String.valueOf(<xsl:copy-of select="$driver"/>)).newInstance();
          } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
            throw new RuntimeException("Error loading driver: "+String.valueOf(<xsl:copy-of select="$driver"/>));
          }
          </xsl:if>
          try {
            _esql_connection = new Cocoon2EsqlConnection();
            setupLogger(_esql_connection);

            _esql_connection.setURL(String.valueOf(<xsl:copy-of select="$dburl"/>));
            <xsl:if test="esql:username">
              _esql_connection.setUser(String.valueOf(<xsl:copy-of select="$username"/>));
            </xsl:if>
            <xsl:if test="esql:password">
              _esql_connection.setPassword(String.valueOf(<xsl:copy-of select="$password"/>));
            </xsl:if>
            <xsl:for-each select="esql:property">
              _esql_connection.setProperty("<xsl:value-of select="@name"/>",<xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="."/></xsl:call-template>);
            </xsl:for-each>
            <xsl:if test="esql:allow-multiple-results">
              _esql_connection.setMultipleResults(String.valueOf(<xsl:copy-of select="$allow-multiple-results"/>));
            </xsl:if>
          } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
            throw new RuntimeException("Error opening connection to dburl: "+String.valueOf(<xsl:copy-of select="$dburl"/>)+": "+_esql_exception_<xsl:value-of select="generate-id(.)"/>.getMessage());
          }
        </xsl:otherwise>
      </xsl:choose>
      try {
        if ("false".equalsIgnoreCase(String.valueOf(<xsl:copy-of select="$autocommit"/>))) {
          if (_esql_connection.getAutoCommit()) {
            _esql_connection.setAutoCommit(false);
          }
        } else {
          if (!_esql_connection.getAutoCommit()) {
            _esql_connection.setAutoCommit(true);
          }
        }
      } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
        // do NOT: throw new RuntimeException("Error setting connection autocommit");
      }
      <xsl:apply-templates/>
    }
    catch (SQLException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
      getLogger().error("",_esql_exception_<xsl:value-of select="generate-id(.)"/>);
    }
    finally {
      try {
        if(!_esql_connection.getAutoCommit()) {
          _esql_connection.commit();
        }
        _esql_connection.close();
        if (_esql_connections.empty()) {
          _esql_connection = null;
        } else {
          _esql_connection = (Cocoon2EsqlConnection)_esql_connections.pop();
        }
      } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {}
    }
  </xsp:logic>
</xsl:template>

<xsl:template match="esql:connection/esql:driver"/>
<xsl:template match="esql:connection/esql:dburl"/>
<xsl:template match="esql:connection/esql:username"/>
<xsl:template match="esql:connection/esql:password"/>
<xsl:template match="esql:connection/esql:pool"/>
<xsl:template match="esql:connection/esql:allow-multiple-results"/>
<xsl:template match="esql:connection/esql:autocommit"/>
<xsl:template match="esql:connection/esql:use-limit-clause"/>
<xsl:template match="esql:connection/esql:property"/>

<xspdoc:desc>Returns the connection's meta data.</xspdoc:desc>
<xsl:template match="esql:get-connection-metadata">
  <xsp:expr>_esql_connection.getConnection().getMetaData()</xsp:expr>
</xsl:template>

<xsl:template match="esql:connection//esql:get-connection">
  <xsp:expr>_esql_connection.getConnection()</xsp:expr>
</xsl:template>

<!-- set one parameter of a prepared or callable statement and use correct method for type -->
<xsl:template name="set-query-parameter">
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
</xsl:template>

<xspdoc:desc> internal. set one parameter of a callable statement </xspdoc:desc>
<xsl:template name="set-call-parameter">
  <xsl:if test="@direction='out' or @direction='inout'">
    <xsl:text>_esql_query.getCallableStatement().</xsl:text>
    registerOutParameter(<xsl:value-of select="position()"/>, <xsl:call-template name="get-Sql-Type"><xsl:with-param name="type"><xsl:value-of select="@type"/></xsl:with-param></xsl:call-template><xsl:if test="@typename">, <xsl:value-of select="@typename"/> </xsl:if>);
  </xsl:if>
  <xsl:if test="not(@direction) or @direction='inout' or @direction='in'">
    <xsl:text>_esql_query.getCallableStatement().</xsl:text>
    <xsl:call-template name="set-query-parameter"/>
  </xsl:if>
</xsl:template>



<xspdoc:desc> internal. set one parameter of a prepared statement </xspdoc:desc>
<xsl:template name="set-parameter">
  <xsl:text>_esql_query.getPreparedStatement().</xsl:text>
  <xsl:call-template name="set-query-parameter"/>
</xsl:template>

<xsl:template name="do-results">
  do {
     if (_esql_query.hasResultSet()) {
        _esql_query.getResultRows();
        if (_esql_query.nextRow()) {
           switch (_esql_query.getQueryResultsCount()) {
           <xsl:for-each select="esql:results">
             case <xsl:value-of select="position()"/>: <xsl:if test="position()=last()"><xsl:text>
             default: </xsl:text></xsl:if><xsl:apply-templates select="."/>
                 break;
           </xsl:for-each>
           }
        } else {
           switch (_esql_query.getUpdateResultsCount()) {
           <xsl:for-each select="esql:no-results">
             case <xsl:value-of select="position()"/>: <xsl:if test="position()=last()"><xsl:text>
           default: </xsl:text></xsl:if><xsl:apply-templates select="."/>
                 break;
           </xsl:for-each>
           }
        }
        _esql_query.getResultSet().close();
     } else {
        if (_esql_query.getUpdateCount() &gt; 0) {
           switch (_esql_query.getUpdateResultsCount()) {
           <xsl:for-each select="esql:update-results">
             case <xsl:value-of select="position()"/>: <xsl:if test="position()=last()"><xsl:text>
             default: </xsl:text></xsl:if><xsl:apply-templates select="."/>
                 break;
           </xsl:for-each>
           }
        } else {
           switch (_esql_query.getUpdateResultsCount()) {
           <xsl:for-each select="esql:no-results">
             case <xsl:value-of select="position()"/>: <xsl:if test="position()=last()"><xsl:text>
             default: </xsl:text></xsl:if><xsl:apply-templates select="."/>
                 break;
           </xsl:for-each>
           }
        }
     }
   } while(_esql_connection.getMultipleResults() &amp;&amp; _esql_query.getMoreResults());
</xsl:template>


<xspdoc:descr>Internal helper to select the resultset-from-object attribute of the nested esql:call element.</xspdoc:descr>
<xsl:template match="esql:call" mode="resultset">
  <xsl:value-of select="@resultset-from-object"/>
</xsl:template>

<xsl:template match="esql:connection//esql:execute-query">

  <xsl:variable name="query"><xsl:choose><xsl:when test="esql:query"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:query"/></xsl:call-template></xsl:when><xsl:when test="esql:call"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:call"/></xsl:call-template></xsl:when></xsl:choose></xsl:variable>

  <xsl:variable name="maxrows"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:max-rows"/></xsl:call-template></xsl:variable>
  <xsl:variable name="skiprows"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:skip-rows"/></xsl:call-template></xsl:variable>
  <xsl:variable name="use-limit-clause"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:use-limit-clause"/></xsl:call-template></xsl:variable>

  <xsp:logic>
    if (_esql_query != null) {
      _esql_queries.push(_esql_query);
    }

    _esql_query = _esql_connection.createQuery(
             String.valueOf(<xsl:copy-of select="$use-limit-clause"/>),
             String.valueOf(<xsl:copy-of select="$query"/>)
      );

    <xsl:if test="esql:max-rows">
      try {
        _esql_query.setMaxRows( Integer.parseInt(String.valueOf(<xsl:copy-of select="$maxrows"/>).trim()) );
      } catch (NumberFormatException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {}
    </xsl:if>

    <xsl:if test="esql:skip-rows">
      try {
        _esql_query.setSkipRows( Integer.parseInt(String.valueOf(<xsl:copy-of select="$skiprows"/>).trim()) );
      } catch (NumberFormatException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {}
    </xsl:if>

    try {
      <xsl:choose>
        <xsl:when test="esql:call">
            _esql_query.prepareCall();
          <xsl:for-each select="esql:call//esql:parameter">
            <xsl:call-template name="set-call-parameter"/>
          </xsl:for-each>
          <xsl:choose>
            <xsl:when test="esql:call[@needs-query='true' or @needs-query='yes']">_esql_query.executeQuery();</xsl:when>
            <xsl:when test="esql:call[@resultset-from-object]">_esql_query.execute(<xsl:apply-templates select="esql:call" mode="resultset"/>);</xsl:when>
            <xsl:otherwise>_esql_query.execute();</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="esql:query//esql:parameter">
            _esql_query.prepareStatement();
          <xsl:for-each select="esql:query//esql:parameter">
            <xsl:call-template name="set-parameter"/>
          </xsl:for-each>
            _esql_query.execute();
        </xsl:when>
        <xsl:otherwise>
          _esql_query.prepareStatement();
          _esql_query.execute();
        </xsl:otherwise>
      </xsl:choose>
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("esql query: " + _esql_query.getQueryString());
      }

      <xsl:call-template name="do-results"/>

      <xsl:if test="esql:call">
        // call results
        <xsp:content>
          <xsl:apply-templates select="esql:call-results"/>
        </xsp:content>
      </xsl:if>

      _esql_query.getPreparedStatement().close();

    } catch (SQLException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
        <xsl:choose>
          <xsl:when test="esql:error-results">
          try {
            _esql_exception = _esql_exception_<xsl:value-of select="generate-id(.)"/>;
            _esql_exception_writer = new StringWriter();
            _esql_exception.printStackTrace(new PrintWriter(_esql_exception_writer));
            <xsl:apply-templates select="esql:error-results"/>
            if (!_esql_connection.getAutoCommit()) {
              _esql_connection.rollback();
            }
          } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>_2) {}
          </xsl:when>
          <xsl:otherwise>
          try {
            if (!_esql_connection.getAutoCommit()) {
              _esql_connection.rollback();
            }
          } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>_2) {}
          throw new RuntimeException("Error executing statement: " + _esql_query.getQueryString() + ": "+_esql_exception_<xsl:value-of select="generate-id(.)"/>);
          </xsl:otherwise>
        </xsl:choose>
    } finally {
        _esql_query.cleanUp();
    }
    if (_esql_queries.empty()) {
      _esql_query = null;
    } else {
      _esql_query = (AbstractEsqlQuery)_esql_queries.pop();
    }
  </xsp:logic>
</xsl:template>

<xsl:template match="esql:query//esql:parameter">"?"</xsl:template>
<xsl:template match="esql:call//esql:parameter">"?"</xsl:template>

<xsl:template match="esql:execute-query//esql:results//esql:row-count">
  <xsp:expr>_esql_query.getRowCount()</xsp:expr>
</xsl:template>

<xsl:template match="esql:execute-query//esql:results">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xsl:template match="esql:execute-query//esql:call-results">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xsl:template match="esql:execute-query//esql:error-results">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xsl:template match="esql:execute-query//esql:no-results">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xsl:template match="esql:execute-query//esql:update-results">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xsl:template match="esql:update-results//esql:get-update-count">
  <xsp:expr>_esql_query.getUpdateCount()</xsp:expr>
</xsl:template>


<xsl:template match="esql:results//esql:row-results">
  <xsl:variable name="group" select=".//esql:group"/>
  <xsp:logic>
    do {
      <xsp:content>
        <xsl:apply-templates/>
      </xsp:content>
      <xsl:call-template name="nextRow"/>
    } while ( _esql_query.keepGoing() );

    if (_esql_query.getSkipRows() > 0 ) {
        <xsl:apply-templates select="ancestor::esql:results//esql:previous-results" mode="more"/>
    }

    if (_esql_query.nextRow()) {
        <xsl:apply-templates select="ancestor::esql:results//esql:more-results" mode="more"/>
    }
  </xsp:logic>
</xsl:template>



<xspdoc:desc>Only advance one row if no nested groups exist in this query. Ignore nested queries.</xspdoc:desc>
<xsl:template name="nextRow">
  <xsl:if test="not(.//esql:group) or generate-id(.//esql:group)=generate-id(.//esql:execute-query//esql:group)">
    <xsp:logic>
  //checking out early?
    if (_esql_query.getMaxRows() != -1 &amp;&amp; _esql_query.getCurrentRow() - _esql_query.getSkipRows() == _esql_query.getMaxRows()) {
      _esql_query.setKeepGoing( false );
    } else {	//if not, advance normally
      _esql_query.setKeepGoing( _esql_query.nextRow() );
    }
    </xsp:logic>
  </xsl:if>
</xsl:template>




<xsl:template match="esql:results//esql:previous-results"/>


<xsl:template match="esql:results//esql:previous-results" mode="more">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>




<xspdoc:desc>Allows header elements around groups of consecutive records with identical values in column named by @group-on.  Facilitates a single query with joins to be used in lieu of some nested queries.</xspdoc:desc>
<xsl:template match="esql:group//esql:member">
  <xsp:logic>
    do {
       <xsp:content>
         <xsl:apply-templates/>
       </xsp:content>
       <xsl:call-template name="nextRow"/>
    } while (_esql_query.keepGoing() &amp;&amp; !_esql_query.hasGroupingVarChanged());
     </xsp:logic>
</xsl:template>


<xspdoc:desc>Used in conjunction with and nested inside esql:group.  Formatting for individual records goes within esql:member. Header stuff goes in between group and member.</xspdoc:desc>
<xsl:template match="esql:group">
  <xsl:variable name="group">
    <xsl:call-template name="get-column">
      <xsl:with-param name="name">group-on</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>

  <xsp:logic>
    _esql_query.incGroupLevel();
    if (!_esql_query.groupLevelExists()) {
       _esql_query.setGroupingVar(<xsl:copy-of select="$group"/>);
    }
    <xsp:content>
      <xsl:apply-templates/>
    </xsp:content>
    _esql_query.decGroupLevel();
  </xsp:logic>
</xsl:template>


<xsl:template match="esql:results//esql:more-results"/>

<xsl:template match="esql:results//esql:more-results" mode="more">
  <xsp:content>
    <xsl:apply-templates/>
  </xsp:content>
</xsl:template>

<xspdoc:desc>results in a set of elements whose names are the names of the columns. the elements each have one text child, whose value is the value of the column interpreted as a string. No special formatting is allowed here. If you want to mess around with the names of the elements or the value of the text field, use the type-specific get methods and write out the result fragment yourself.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-columns">
  <xsl:variable name="tagcase"><xsl:value-of select="@tag-case"/></xsl:variable>
      <xsp:logic>
        for (int _esql_i = 1; _esql_i &lt;= _esql_query.getResultSetMetaData().getColumnCount(); _esql_i++) {
          String _esql_tagname = _esql_query.getResultSetMetaData().getColumnName(_esql_i);
          <xsp:element>
            <xsp:param name="name">
              <xsl:choose>
                <xsl:when test="$tagcase='lower'">
                  <xsp:expr>_esql_tagname.toLowerCase()</xsp:expr>
                </xsl:when>
                <xsl:when test="$tagcase='upper'">
                  <xsp:expr>_esql_tagname.toUpperCase()</xsp:expr>
                </xsl:when>
                <xsl:otherwise>
                  <xsp:expr>_esql_tagname</xsp:expr>
                </xsl:otherwise>
              </xsl:choose>
            </xsp:param>
            <xsp:logic>
              switch(_esql_query.getResultSet().getMetaData().getColumnType(_esql_i)){
                 case java.sql.Types.ARRAY:
                 case java.sql.Types.STRUCT:
                    <xsp:element name="sql-row">
                      <xsp:logic>
                        Object[] _esql_struct = ((Struct) _esql_query.getResultSet().getObject(_esql_i)).getAttributes();
                        for ( int _esql_k=0; _esql_k&lt;_esql_struct.length; _esql_k++){
                        <xsp:element name="sql-row-item"><xsp:logic>this._esql_printObject(_esql_struct[_esql_k],xspAttr);</xsp:logic></xsp:element>
                        }
                      </xsp:logic>
                    </xsp:element>
                    break;

                 case java.sql.Types.OTHER: // This is what Informix uses for Sets, Bags, Lists
                    // postgres is broken as it doesn't allow getObject()
                    // to retrieve any type (i.e. bit and bit varying)
                    // so don't handle complex types different for postgres
                    if (!_esql_connection.getURL().startsWith("jdbc:postgresql:")) {
                       this._esql_printObject(_esql_query.getResultSet().getObject(_esql_i), xspAttr);
                       break;
                    }

                 default:
                    // standard type
                    <xsp:content>
                    <xsp:expr>
                      <xsl:call-template name="get-string-encoded">
                        <xsl:with-param name="null"><xsl:value-of select="@null"/></xsl:with-param>
                        <xsl:with-param name="column-spec">_esql_i</xsl:with-param>
                        <xsl:with-param name="resultset">_esql_query.getResultSet()</xsl:with-param>
                      </xsl:call-template>
                    </xsp:expr></xsp:content>
              }
            </xsp:logic>
          </xsp:element>
        }
        this.characters("\n");
      </xsp:logic>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a string</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-string|esql:call-results//esql:get-string" name="get-string">
  <xsp:expr>
    <xsl:call-template name="get-string-encoded">
      <xsl:with-param name="null"><xsl:value-of select="@null"/></xsl:with-param>
      <xsl:with-param name="column-spec"><xsl:call-template name="get-column"/></xsl:with-param>
      <xsl:with-param name="resultset"><xsl:call-template name="get-resultset"/></xsl:with-param>
    </xsl:call-template>
  </xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a date. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-date|esql:call-results//esql:get-date">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr><xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>) == null ? "" : new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getDate(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a time. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-time|esql:call-results//esql:get-time">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>) == null ? "" : new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTime(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a timestamp. if a format attribute exists, its value is taken to be a date format string as defined in java.text.SimpleDateFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-timestamp|esql:call-results//esql:get-timestamp">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>) == null ? "" : new SimpleDateFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getTimestamp(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as true or false</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-boolean|esql:call-results//esql:get-boolean">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getBoolean(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a double. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-double|esql:call-results//esql:get-double">
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
<xsl:template match="esql:row-results//esql:get-float|esql:call-results//esql:get-float">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(new Float(<xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getFloat(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a BigDecimal. if a format attribute exists, its value is taken to be a decimal format string as defined in java.text.DecimalFormat, and the result is formatted accordingly.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-bigdecimal|esql:call-results//esql:get-bigdecimal">
  <xsl:choose>
    <xsl:when test="@format">
      <xsp:expr>new DecimalFormat("<xsl:value-of select="@format"/>").format(<xsl:call-template name="get-resultset"/>.getBigDecimal(<xsl:call-template name="get-column"/>))</xsp:expr>
    </xsl:when>
    <xsl:otherwise>
      <xsp:expr><xsl:call-template name="get-resultset"/>.getBigDecimal(<xsl:call-template name="get-column"/>)</xsp:expr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xspdoc:desc>returns the current result set</xspdoc:desc>
<xsl:template match="esql:results//esql:get-resultset">
  <xsp:expr><xsl:call-template name="get-resultset"/></xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as an object</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-object|esql:call-results//esql:get-object">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getObject(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as an array</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-array|esql:call-results//esql:get-array">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getArray(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a struct</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-struct|esql:call-results//esql:get-struct">
  <xsp:expr>(Struct) <xsl:call-template name="get-resultset"/>.getObject(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as an integer</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-int|esql:call-results//esql:get-int">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getInt(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a long</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-long|esql:call-results//esql:get-long">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getLong(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a short</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-short|esql:call-results//esql:get-short">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getShort(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as byte array</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-blob|esql:call-results//esql:get-blob" name="get-blob">
	<xsp:expr>EsqlHelper.getBlob(<xsl:call-template name="get-resultset"/>,<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as unicode string (column can be string or clob</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-clob|esql:call-results//esql:get-clob" name="get-clob">
  <xsl:param name="null">
   <xsl:choose>
    <xsl:when test="@null"><xsl:value-of select="@null"/></xsl:when>
    <xsl:otherwise>_null_</xsl:otherwise>
   </xsl:choose>
  </xsl:param>
  <xsp:expr>EsqlHelper.getStringOrClob(<xsl:call-template name="get-resultset"/>,<xsl:call-template name="get-column"/>, "<xsl:value-of select="$null"/>")</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the value of the given column as a clob as ascii string with optinal encoding</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-ascii|esql:call-results//esql:get-ascii">
  <xsl:param name="null">
   <xsl:choose>
    <xsl:when test="@null"><xsl:value-of select="@null"/></xsl:when>
    <xsl:otherwise>_null_</xsl:otherwise>
   </xsl:choose>
  </xsl:param>
  <xsp:expr>EsqlHelper.getAscii(<xsl:call-template name="get-resultset"/>, <xsl:call-template name="get-column"/>, "<xsl:value-of select="$null"/>")</xsp:expr>
</xsl:template>

 <xspdoc:desc>returns the value of the given column interpeted as an xml fragment.
 The fragment is parsed by the default xsp parser and the document element is returned.
 If a root attribute exists, its value is taken to be the name of an element to wrap around the contents of
 the fragment before parsing.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-xml|esql:call-results//esql:get-xml">
  <xsl:variable name="content">
    <xsl:choose>
      <xsl:when test="@root">
        <xsl:text>"&lt;</xsl:text>
        <xsl:if test="@root-ns-prefix">
            <xsl:value-of select="@root-ns-prefix"/>
            <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:value-of select="@root"/>
        <xsl:if test="@root-ns">
            <xsl:text> xmlns</xsl:text>
            <xsl:if test="@root-ns-prefix">
                <xsl:text>:</xsl:text>
                <xsl:value-of select="@root-ns-prefix"/>
            </xsl:if>
            <xsl:text>=\&quot;</xsl:text>
            <xsl:value-of select="@root-ns"/>
            <xsl:text>\&quot;</xsl:text>
        </xsl:if>
        <xsl:text>&gt;"+</xsl:text>
        <xsl:call-template name="get-string"/>
        <xsl:text>+"&lt;/</xsl:text>
        <xsl:if test="@root-ns-prefix">
            <xsl:value-of select="@root-ns-prefix"/>
            <xsl:text>:</xsl:text>
        </xsl:if>
        <xsl:value-of select="@root"/>
        <xsl:text>&gt;"</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="get-string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsp:logic>
    {
        try {
            XSPUtil.includeString(<xsl:copy-of select="$content"/>, this.manager, this.contentHandler);
        } catch (Exception _esql_exception_<xsl:value-of select="generate-id(.)"/>) {
            getLogger().error("Could not include XML string", _esql_exception_<xsl:value-of select="generate-id(.)"/>);
        }
    }
  </xsp:logic>
</xsl:template>

<xspdoc:desc>returns the number of columns in the resultset.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-count">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnCount()</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the metadata of the resultset.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-metadata">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData()</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the position of the current row in the result set</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-row-position|esql:results//esql:get-row-position|esql:call-results//esql:get-row-position">
  <xsp:expr>_esql_query.getCurrentRow()</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the name of the given column. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-name">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnName(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the label of the given column. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-label">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnLabel(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the name of the type of the given column. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:results//esql:get-column-type-name">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnTypeName(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>returns the type of the given column as int. the column must be specified by number, not name.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:get-column-type" name="get-column-type">
  <xsp:expr><xsl:call-template name="get-resultset"/>.getMetaData().getColumnType(<xsl:call-template name="get-column"/>)</xsp:expr>
</xsl:template>

<xspdoc:desc>allows null-column testing. Evaluates to a Java expression, which is true when the referred column contains a null-value for the current resultset row</xspdoc:desc>
<xsl:template match="esql:row-results//esql:is-null">
  <xsp:expr>((<xsl:call-template name="get-resultset"/>.getObject(<xsl:call-template name="get-column"/>) == null) || <xsl:call-template name="get-resultset"/>.wasNull())</xsp:expr>
</xsl:template>

<xsl:template match="esql:result"/>

<xspdoc:desc>creates a nested query like block that uses the result set obtained from a column as current result set. This version is deprecated, please use &lt;esql:use-result&gt; instead.</xspdoc:desc>
<xsl:template match="esql:row-results//esql:results[child::esql:result]|esql:call-results//esql:results[child::esql:result]">
  <xsl:call-template name="use-results"/>
</xsl:template>

<xspdoc:desc>creates a nested query like block that uses the result set obtained from a column as current result set.</xspdoc:desc>
<xsl:template name="use-results" match="esql:use-results[child::esql:result]">
  <xsl:variable name="maxrows"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:max-rows"/></xsl:call-template></xsl:variable>
  <xsl:variable name="skiprows"><xsl:call-template name="get-nested-string"><xsl:with-param name="content" select="esql:skip-rows"/></xsl:call-template></xsl:variable>
<xsp:logic>
  // nested result set
    if (_esql_query != null) {
      _esql_queries.push(_esql_query);
    }

    _esql_query = _esql_query.newInstance((ResultSet) <xsl:apply-templates select="esql:result/*"/>);

    <xsl:if test="esql:max-rows">
      try {
        _esql_query.setMaxRows( Integer.parseInt(String.valueOf(<xsl:copy-of select="$maxrows"/>).trim()) );
      } catch (NumberFormatException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {}
    </xsl:if>

    <xsl:if test="esql:skip-rows">
      try {
        _esql_query.setSkipRows( Integer.parseInt(String.valueOf(<xsl:copy-of select="$skiprows"/>).trim()) );
      } catch (NumberFormatException _esql_exception_<xsl:value-of select="generate-id(.)"/>) {}
    </xsl:if>

   {
       <xsl:call-template name="do-results"/>
    }
    if (_esql_queries.empty()) {
      _esql_query = null;
    } else {
      _esql_query = (AbstractEsqlQuery)_esql_queries.pop();
    }
  </xsp:logic>
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
  <xsl:call-template name="get-query"/><xsl:choose><xsl:when test="@from-call='yes' or @from-call='true'"><xsl:text>.getCallableStatement()</xsl:text></xsl:when><xsl:otherwise><xsl:text>.getResultSet()</xsl:text></xsl:otherwise></xsl:choose>
</xsl:template>

<xsl:template name="get-query">
  <xsl:choose>
    <xsl:when test="@ancestor">
      <xsl:text>((AbstractEsqlQuery)_esql_queries.elementAt(_esql_queries.size()-</xsl:text>
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
  <xsl:param name="name">column</xsl:param>
  <xsl:variable name="column">
     <xsl:call-template name="get-parameter">
       <xsl:with-param name="name"><xsl:value-of select="$name"/></xsl:with-param>
       <xsl:with-param name="required">true</xsl:with-param>
     </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="starts-with($column,'&quot;')">
      <xsl:variable name="raw-column">
        <xsl:value-of select="substring($column,2,string-length($column)-2)"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="not(string(number($raw-column))='NaN')">
          <xsl:value-of select="$raw-column"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$column"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="$column"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="get-string-encoded">
  <xsl:param name="column-spec"/>
  <xsl:param name="resultset"/>
  <xsl:param name="null"/>
  <xsl:variable name="encoding">
    <xsl:choose>
      <xsl:when test="@encoding">"<xsl:value-of select="@encoding"/>"</xsl:when>
      <xsl:when test="esql:encoding">
        <xsl:call-template name="get-nested-string">
          <xsl:with-param name="content" select="esql:encoding"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>default</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="$encoding = 'default'">
      EsqlHelper.getAscii(<xsl:value-of select="$resultset"/>,<xsl:value-of select="$column-spec"/>,"<xsl:value-of select="$null"/>")
    </xsl:when>
    <xsl:otherwise>
      EsqlHelper.getStringFromByteArray(<xsl:value-of select="$resultset"/>.getBytes
        (<xsl:value-of select="$column-spec"/>), <xsl:value-of select="$encoding"/>,"<xsl:value-of select="$null"/>")
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="get-Sql-Type">
  <xsl:param name="type"/>
  <xsl:choose>
    <!-- just do the 'unusual' mappings -->
    <xsl:when test="$type='Byte'">Types.TINYINT</xsl:when>
    <xsl:when test="$type='Short'">Types.SMALLINT</xsl:when>
    <xsl:when test="$type='Int'">Types.INTEGER</xsl:when>
    <xsl:when test="$type='Long'">Types.BIGINT</xsl:when>
    <xsl:when test="$type='Float'">Types.REAL</xsl:when>
    <xsl:when test="$type='BigDecimal'">Types.DECIMAL</xsl:when>
    <xsl:when test="$type='Boolean'">Types.BIT</xsl:when>
    <xsl:when test="$type='String'">Types.VARCHAR</xsl:when>
    <xsl:when test="$type='Bytes'">Types.BINARY</xsl:when>
    <xsl:when test="$type='AsciiStream'">Types.LONGVARCHAR</xsl:when>
    <xsl:when test="$type='UnicodeStream'">Types.LONGVARCHAR</xsl:when>
    <xsl:when test="$type='BinaryStream'">Types.VARBINARY</xsl:when>
    <!-- handle DBMS specific types e.g. oracle.jdbc.driver.OracleTypes.CURSOR -->
    <xsl:when test="contains($type,'.')"><xsl:value-of select="$type"/></xsl:when>
    <!-- default to upper case type -->
    <xsl:otherwise>Types.<xsl:value-of select="translate(@type,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--
  Break on error.
  @param message explanation of the error
-->
<xsl:template name="error">
  <xsl:param name="message"/>
  <xsl:message terminate="yes"><xsl:value-of select="$message"/></xsl:message>
</xsl:template>

<!--
     swallow esql:param tags
-->
<xsl:template match="esql:param"/>


<xsl:template match="@*|node()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>

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

<!-- $Id$ -->
<!--
 * XSP Form-Validator logicsheet for the Java language
 *
 * @version $Revision: 1.4 $ $Date: 2004/04/05 12:25:31 $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-formval="http://apache.org/xsp/form-validator/2.0"
  xmlns:xspdoc="http://apache.org/cocoon/XSPDoc/v1"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:variable name="prefix">xsp-formval</xsl:variable>

  <xspdoc:desc>
    The <code>xsp-formval</code> taglib serves as interface to
  retrieve validation results from a request attribute. This
  attribute is set up by the
  <code>org.apache.cocoon.acting.FormValidatorAction</code>. As the
  validation resuls are communicated via the request object the result
  will be reset whenever a new request is generated. Hence using a
  redirection in the sitemap will clear out the validation results as
  this results in a new request object.

  For usage of the <code>FormValidatorAction</code> and how to set up
  a descriptor file see the apropriate javadocs.

  To use this taglib declare it as usal.

  On your page you need to specify the same descriptor file as for the
  <code>FormValidatorAction</code> as well as the same constraint set
  and parameters.

  Afterwards validation results can be obtained for specific
  parameters. In addition it is possible to obtain any attribute
  specified in the descriptor file inside the parameter and
  constraint-set/validate childs. You can even define attributes that
  <code>FormValidatorAction</code> does not know about e.g. define the
  size of an input field in addition to the maximum length....
  </xspdoc:desc>

  <xspdoc:desc>
    Include the necessary classes.
  </xspdoc:desc>
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <!-- keep existing includes -->
      <xsp:structure>
        <xsl:for-each select="xsp:structure">
          <xsl:for-each select="xsp:include">
            <xsl:copy-of select="."/>
          </xsl:for-each>
        </xsl:for-each>
        <xsp:include>org.apache.cocoon.acting.ValidatorActionResult</xsp:include>
        <xsp:include>org.apache.cocoon.transformation.helpers.FormValidatorHelper</xsp:include>
      </xsp:structure>
      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>


  <xspdoc:desc>
    This element must be present to query attributes from the
    descriptor file as it defines the file's URL, the constraint-set
    to use and if the descriptor file should be reread when its
    modification time changes.

    All other tags from this taglib should be inside this one.

    You may use it multiple times on a page.

    You don't need it if you're only interested in the validation
    results and don't hint more details at the user why input
    validation failed.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:descriptor">
    <xsl:variable name="descriptor">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="constraintset">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">constraint-set</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="reloadable">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">reloadable</xsl:with-param>
        <xsl:with-param name="default">true</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
      {
      FormValidatorHelper __formval_fvh = new FormValidatorHelper(<xsl:copy-of select="$descriptor"/>, <xsl:copy-of select="$reloadable"/>, getLogger(), resolver, <xsl:copy-of select="$constraintset"/>);
    </xsp:logic>
    <xsl:apply-templates/>
    <xsp:logic>
      }
    </xsp:logic>
  </xsl:template>


  <xspdoc:desc>
    Returns a Map containg all validation results.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:results"><xsp:expr>FormValidatorHelper.getResults(objectModel)</xsp:expr></xsl:template>


  <xspdoc:desc>
    Returns a boolean whether validation of this parameter was
    successful.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-ok">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.OK)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.OK)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter was successful.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-ok">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.OK)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.OK)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    a "Too Small" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-toosmall">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.TOOSMALL)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.TOOSMALL)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned a "Too Small" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-toosmall">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.TOOSMALL)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.TOOSMALL)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    an error (ERROR or above). See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-error">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).ge(ValidatorActionResult.ERROR)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).ge(ValidatorActionResult.ERROR)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned an error (ERROR or above). See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-error">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).ge(ValidatorActionResult.ERROR)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).ge(ValidatorActionResult.ERROR)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    an "Too Large" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-toolarge">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.TOOLARGE)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.TOOLARGE)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned a "Too Large" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-toolarge">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.TOOLARGE)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.TOOLARGE)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    an "No Match" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-nomatch">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.NOMATCH)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.NOMATCH)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned a "No Match" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-nomatch">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.NOMATCH)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.NOMATCH)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    an "Is Null" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-null">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.ISNULL)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.ISNULL)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned an "Is Null" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-null">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.ISNULL)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.ISNULL)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Returns a boolean whether validation of this parameter returned
    an "Not Present" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives: 1) stand alone, specify parameter name 2)
    within a xsp-formval:validate context it's not necessary to
    specify the parameter name.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:is-notpresent">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:expr>FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.NOTPRESENT)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.NOTPRESENT)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Only displays the enclosed markup if the validation of this
    parameter returned a "Not Present" error. See javadoc of
    <code>org.apache.cocoon.acting.ValidationActionResult</code> for
    detailed meaning.

    Two usage alternatives:
    1) standalone, specify parameter name
    2) within a xsp-formval:validate context (no parameter name necessary)
  </xspdoc:desc>
  <xsl:template match="xsp-formval:on-notpresent">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($name)!='-1'">
        <xsp:logic>
          if (FormValidatorHelper.getParamResult(objectModel,<xsl:copy-of select="$name"/>).equals(ValidatorActionResult.NOTPRESENT)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:when>
      <xsl:otherwise>
        <xsp:logic>
          if (__formval_fvh.getParamResult(objectModel).equals(ValidatorActionResult.NOTPRESENT)) {
             <xsp:content><xsl:apply-templates/></xsp:content>
          }
        </xsp:logic>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xspdoc:desc>
    Retrieve any attribute from a parameter's declaration in the
    descriptor file with respect to parameter or the current
    constraint-set decalarations.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:get-attribute">
    <xsl:variable name="parameter">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">parameter</xsl:with-param>
        <xsl:with-param name="default">-1</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string($parameter)!='-1'">
        <xsp:expr>__formval_fvh.getParameterAttribute(<xsl:copy-of select="$parameter"/>, <xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:otherwise>
        <xsp:expr>__formval_fvh.getParameterAttribute(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xspdoc:desc>
    Provide a context so that within this element it is not necessary
    to provide the parameter's name again and again.
  </xspdoc:desc>
  <xsl:template match="xsp-formval:validate">
    <xsl:variable name="name">
      <xsl:call-template name="get-parameter">
        <xsl:with-param name="name">name</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
      __formval_fvh.setParameter(<xsl:copy-of select="$name"/>);
    </xsp:logic>
    <xsl:apply-templates/>
  </xsl:template>



  <!-- don't copy these twice -->
  <xsl:template match="xsp:structure"/>
  <xsl:template match="xsp:include"/>

  <!-- Utility templates -->
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

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*|$content/text()"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="error">
    <xsl:param name="message"/>
    <xsl:message terminate="yes"><xsl:value-of select="$message"/></xsl:message>
  </xsl:template>

  <!-- Default: match all -->

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

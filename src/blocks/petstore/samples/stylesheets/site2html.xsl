<?xml version="1.0" encoding="utf-8"?>
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

  <xsl:include href="ps-woody-default.xsl"/>

  <xsl:template match="site">
    <html>
      <head>
        <title>Cocoon Petstore Demo</title>
        <meta content="text/html; charset=windows-1252" http-equiv="Content-Type" />
        <meta HTTP-EQUIV="Cache-Control" CONTENT="max-age=0"/>
        <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache"/>
        <meta http-equiv="expires" content="0"/>
        <meta HTTP-EQUIV="Expires" CONTENT="Tue, 01 Jan 1980 1:00:00 GMT"/>
        <meta HTTP-EQUIV="Pragma" CONTENT="no-cache"/>
      </head>
      <body bgcolor="white">
        <table background="images/bkg-topbar.gif" border="0" cellspacing="0" cellpadding="5" width="100%">
          <tbody>
            <tr>
              <td>
                <form method="post" action="index.do">
                  <input type="image" border="0" src="images/logo-topbar.gif" />
                  <select name="view" width="14" align="left" onchange="document.forms[0].submit()">
                    <xsl:if test="@view='woody'">
                      <xsl:attribute name="disabled">disabled</xsl:attribute>
                      <option value="woody" selected="selected">woody</option>
                    </xsl:if>
                    <option value="jexl"><xsl:if test="@view='jexl'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>Jexl</option>
                    <option value="jxpath"><xsl:if test="@view='jxpath'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>JXPath</option>
                    <option value="xsp"><xsl:if test="@view='xsp'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>Xsp</option>
                    <option value="velocity"><xsl:if test="@view='velocity'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>Velocity</option>
                  </select>
                </form>
              </td>
              <td align="right">
                <a href="viewCart.do"><img border="0" name="img_cart" src="images/cart.gif" /></a><img border="0" src="images/separator.gif" hspace="4" />
                <xsl:choose>
                  <xsl:when test="@signOn='true'">
                    <a href="signonForm.do"><img border="0" name="img_signin" src="images/sign-in.gif" /></a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="signonForm.do?signoff=true"><img border="0" name="img_signout" src="images/sign-out.gif" /></a><img border="0" src="images/separator.gif" hspace="4" />
                    <a href="editAccount.do"><img border="0" name="img_myaccount" src="images/my_account.gif" /></a>
                  </xsl:otherwise>
                </xsl:choose>
                <img border="0" src="images/separator.gif" hspace="4" /><a href="../help.html"><img border="0" name="img_help" src="images/help.gif" /></a>
              </td>
              <td align="left" valign="bottom">
                <form method="post" action="searchProducts.do">
                  <input name="keyword" size="14" />
                  <input border="0" src="images/search.gif" type="image" />
                </form>
              </td>
            </tr>
          </tbody>
        </table>
        <center>
          <a href="viewCategory.do?categoryId=FISH"><img border="0" src="images/sm_fish.gif" /></a> 
          <img border="0" src="images/separator.gif" hspace="4" /> 
          <a href="viewCategory.do?categoryId=DOGS"><img border="0" src="images/sm_dogs.gif" /></a> 
          <img border="0" src="images/separator.gif" hspace="4" /> 
          <a href="viewCategory.do?categoryId=REPTILES"><img border="0" src="images/sm_reptiles.gif" /></a> 
          <img border="0" src="images/separator.gif" hspace="4" /> 
          <a href="viewCategory.do?categoryId=CATS"><img border="0" src="images/sm_cats.gif" /></a> 
          <img border="0" src="images/separator.gif" hspace="4" />
          <a href="viewCategory.do?categoryId=BIRDS"><img border="0" src="images/sm_birds.gif" /></a> 
        </center>
        <xsl:apply-templates/>
        <br/>
        <p align="center">
          <a href="http://cocoon.apache.org/"><img border="0" align="center" src="images/cocoon.gif" /></a>
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="welcome">
    <table border="0" cellspacing="0" width="100%">
      <tbody>
        <tr>
          <td valign="top" width="100%">
            <table align="left" border="0" cellspacing="0" width="80%">
              <tbody>
                <tr>
                  <td valign="top">
                    <xsl:apply-templates/>
                  </td>
                  <td align="center" bgcolor="white" height="300" width="100%">
                    <map name="estoremap">
                      <area alt="Birds" coords="72,2,280,250" href="viewCategory.do?categoryId=BIRDS" shape="RECT" />
                      <area alt="Fish" coords="2,180,72,250" href="viewCategory.do?categoryId=FISH" shape="RECT" />
                      <area alt="Dogs" coords="60,250,130,320" href="viewCategory.do?categoryId=DOGS" shape="RECT" />
                      <area alt="Reptiles" coords="140,270,210,340" href="viewCategory.do?categoryId=REPTILES" shape="RECT" />
                      <area alt="Cats" coords="225,240,295,310" href="viewCategory.do?categoryId=CATS" shape="RECT" />
                      <area alt="Birds" coords="280,180,350,250" href="viewCategory.do?categoryId=BIRDS" shape="RECT" />
                    </map>
                    <img border="0" height="355" src="images/splash.gif" align="center" usemap="#estoremap" width="350" />
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="menu">
    <table bgcolor="#FFFF88" border="0" cellspacing="0" cellpadding="5" width="200">
      <tbody>
        <xsl:apply-templates/>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template match="firstName">
    <tr>
      <td>
        <b><i><font size="2" color="BLACK">Welcome <xsl:value-of select="." /></font></i></b>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="menu/category">
    <xsl:variable name="up" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
    <xsl:variable name="lo" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="loname"><xsl:value-of select="translate(@name,$up,$lo)" /></xsl:variable>

    <tr>
      <td>
        <a href="viewCategory.do?categoryId={@id}"><img border="0" src="images/{$loname}_icon.gif" alt="{@name}"/></a>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="backpointer">
    <table align="left" bgcolor="#008800" border="0" cellspacing="2" cellpadding="2">
      <tr>
        <td bgcolor="#FFFF88">
          <a href="{@do}"><b><font color="BLACK" size="2">&lt;&lt; <xsl:value-of select="@name" /></font></b></a>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="category">
    <p>
      <center>
        <h2><xsl:value-of select="@name" /></h2>
      </center>
      <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
        <tr bgcolor="#CCCCCC">
          <td>
            <b>Product ID</b>
          </td>
          <td>
            <b>Name</b>
          </td>
        </tr>
        <xsl:apply-templates/>
      </table>
    </p>
  </xsl:template>

  <xsl:template match="category/product">
    <tr bgcolor="#FFFF88">
      <td>
        <b><a href="viewProduct.do?productId={@id}"><font color="BLACK"><xsl:value-of select="@id" /></font></a></b>
      </td>
      <td>
        <xsl:value-of select="@name" />
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="search">
    <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
      <tr bgcolor="#CCCCCC">
        <td></td>
        <td>
          <b>Product ID</b>
        </td>
        <td>
          <b>Name</b>
        </td>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="search/product">
    <tr bgcolor="#FFFF88">
      <td><a href="viewProduct.do?productId={@id}"><xsl:value-of select="product-desc" disable-output-escaping="yes"/></a></td>
      <td>
        <b><a href="viewProduct.do?productId={@id}"><font color="BLACK"><xsl:value-of select="@id" /></font></a></b>
      </td>
      <td><xsl:value-of select="@name" /></td>
    </tr>
  </xsl:template>

  <xsl:template match="situation">
    <tr>
      <td>
        <xsl:if test="@firstPage='false'" >
          <a href="{@continuation}.kont?page=previous"><font color="white"><B>&lt;&lt; Prev</B></font></a>&#160;
        </xsl:if>
        <xsl:if test="@lastPage='false'" >
          <a href="{@continuation}.kont?page=next"><font color="white"><B>Next &gt;&gt;</B></font></a>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="product">
    <p>
      <center>
        <b><font size="4"><xsl:value-of select="@name" /></font></b>
      </center>
      <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
        <tr bgcolor="#CCCCCC">
          <td><b>Item ID</b></td>
          <td><b>Product ID</b></td>
          <td><b>Description</b></td>
          <td><b>List Price</b></td>
          <td></td>
        </tr>
        <xsl:apply-templates/>
      </table>
    </p>
  </xsl:template>

  <xsl:template match="product/item">
    <tr bgcolor="#FFFF88">
      <td>
        <b><a href="viewItem.do?itemId={@id}"><xsl:value-of select="@id" /></a></b>
      </td>
      <td>
        <b><xsl:value-of select="@productId" /></b>
      </td>
      <td>
        <xsl:value-of select="desc" /><xsl:text> </xsl:text><xsl:value-of select="../@name" />
      </td>
      <td>
        <xsl:text>$</xsl:text> <xsl:value-of select="price" />
      </td>
      <td>
        <a href="addItemToCart.do?itemId={@id}"><img border="0" src="images/button_add_to_cart.gif" /></a>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="cart-common-columns">
    <td><b>Item ID</b></td>
    <td><b>Product ID</b></td>
    <td><b>Description</b></td>
    <td><b>Quantity</b></td>
    <td><b>List Price</b></td>
    <td><b>Total Cost</b></td>
  </xsl:template>

  <xsl:template match="cart">
    <table border="0" width="100%" cellspacing="0" cellpadding="0">
      <tr>
        <td valign="top" width="20%" align="left">
          <xsl:apply-templates select="backpointer" />
        </td>
        <td valign="top" align="center">
          <h2 align="center"><xsl:value-of select="@name" /></h2>
          <form action="updateCartQuantities.do" method="post" >
            <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="5">
              <tr bgcolor="#cccccc">
                <xsl:call-template name="cart-common-columns" />
                <xsl:if test="@name='Shopping Cart'">
                  <td></td>
                </xsl:if>
              </tr>
              <xsl:if test="not(item)">
                <tr bgcolor="#FFFF88">
                  <td colspan="7">
                    <b>Your cart is empty.</b>
                  </td>
                </tr>
              </xsl:if>
              <xsl:apply-templates select="item" />
              <tr bgcolor="#FFFF88">
                <td colspan="6" align="right">
                  <b>Sub Total: $<xsl:value-of select="format-number (subTotal, '###,##0.00')"/></b><br />
                  <xsl:if test="@name='Shopping Cart'">
                  <input type="image" border="0" src="images/button_update_cart.gif" name="update" />
                  </xsl:if>
                </td>
                <xsl:if test="@name='Shopping Cart'">
                  <td></td>
                </xsl:if>
              </tr>
            </table>
          </form>
          <xsl:if test="item">
            <xsl:apply-templates select="nextpointer" />
          </xsl:if>
        </td>
        <td valign="top" width="20%" align="right">
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="cart[@name='Status']">
    <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="5">
      <tr bgcolor="#cccccc">
        <td><b>Pos.</b></td>
        <td><b>Item ID</b></td>
        <td><b>Quantity</b></td>
        <td><b>List Price</b></td>
        <td><b>Total Cost</b></td>
      </tr>
      <xsl:apply-templates select="lineItem" />
      <tr bgcolor="#FFFF88">
        <td colspan="5" align="right">
          <b>Total: $<xsl:value-of select="format-number (total, '###,##0.00')"/></b><br />
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="nextpointer">
    <br />
    <center>
      <a href="{@do}"><img border="0" src="{@img}" /></a>
    </center>
  </xsl:template>

  <xsl:template match="cart/lineItem">
    <tr bgcolor="#FFFF88">
      <td><xsl:value-of select="@linenum" />.</td>
      <td><b><xsl:value-of select="@id" /></b></td>
      <td><xsl:value-of select="format-number (quantity, '####')"/></td>
      <td align="right">$<xsl:value-of select="format-number (unitprice, '###,##0.00')"/></td>
      <td align="right">$<xsl:value-of select="format-number (total, '###,##0.00')"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="cart/item">
    <tr bgcolor="#FFFF88">
      <td>
        <b><xsl:value-of select="@id" /></b>
      </td>
      <td>
        <xsl:value-of select="@productId" />
      </td>
      <td>
        <xsl:value-of select="desc" />
      </td>
      <td align="center">
        <xsl:choose>
          <xsl:when test="../@name='Shopping Cart'">
            <input type="text" size="3" name="{@id}" >
              <xsl:attribute name="value"><xsl:value-of select="format-number(quantity, '####')"/></xsl:attribute>
            </input>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="format-number(quantity, '####')"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      <td align="right">
        $<xsl:value-of select="format-number(listPrice, '###,##0.00')"/>
      </td>
      <td align="right">
        $<xsl:value-of select="format-number(total, '###,##0.00')"/>
      </td>
      <xsl:if test="../@name='Shopping Cart'">
        <td>
          <a href="removeItemFromCart.do?workingItemId={@id}"><img border="0" src="images/button_remove.gif" /></a>
        </td>
      </xsl:if>
    </tr>
  </xsl:template>

  <xsl:template match="item">
    <p>
      <table align="center" bgcolor="#008800" cellspacing="2" cellpadding="3" border="0" width="60%">
        <tr bgcolor="#FFFF88">
          <td bgcolor="#FFFFFF">
            <xsl:value-of select="product-desc" disable-output-escaping="yes"/>
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td width="100%" bgcolor="#cccccc">
            <b><xsl:value-of select="@id" /></b>
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td>
            <b><font size="4"><xsl:value-of select="desc" /></font></b>
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td>
            <font size="3"><i><xsl:value-of select="product-name" /></i></font>
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td>
            <xsl:value-of select="format-number (instock, '####')"/> in stock.
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td>
            $<xsl:value-of select="format-number (price, '###,##0.00')"/>
          </td>
        </tr>
        <tr bgcolor="#FFFF88">
          <td>
            <a href="addItemToCart.do?itemId={@id}" ><img border="0" src="images/button_add_to_cart.gif" /></a>
          </td>
        </tr>
      </table>
    </p>
  </xsl:template>

  <xsl:template match="form[@label='signon']">
    <xsl:apply-templates select="message"/>
    <form>
      <xsl:copy-of select="@action | @method"/>
      <table align="center" border="0">
        <tr>
          <td colspan="2">Please enter your username and password.<br /> </td>
        </tr>
        <xsl:apply-templates select="input"/>
      </table>
    </form>
  </xsl:template>

  <xsl:template match="message">
    <br clear="all" />
      <center>
        <b>
          <font size="3">
          <xsl:if test="@type='warning'">
            <xsl:attribute name="color">RED</xsl:attribute> 
          </xsl:if>
          <xsl:value-of select="." />
        </font>
      </b>
    </center>
    <br clear="all" />
  </xsl:template>

  <xsl:template match="input">
    <tr>
      <td><xsl:value-of select="@label" /></td>
      <td><input><xsl:copy-of select="@type | @src | @value | @name | @size | @selected"/></input></td>
    </tr>
  </xsl:template>

  <xsl:template match="register">
    <center>
      <a href="{@do}"><img border="0" src="images/button_register_now.gif" /></a>
    </center>
  </xsl:template>

  <xsl:template match="panel[panel]">
    <table width="60%" align="center" border="0" cellpadding="3" cellspacing="1" bgcolor="#FFFF88">
      <xsl:if test="@header">
        <tr bgcolor="#FFFF88">
          <td align="center" colspan="2">
            <font size="4"><b><xsl:value-of select="@header" /></b></font>
            <xsl:if test="@subheader"><br /><font size="3"><b><xsl:value-of select="@subheader" /></b></font></xsl:if>
          </td>
        </tr>
      </xsl:if>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="panel/panel">
    <tr bgcolor="#FFFF88">
      <td colspan="2">
        <b><font size="4"><font color="GREEN"><xsl:value-of select="@label" /></font> <xsl:value-of select="@value" /></font></b>
      </td>
    </tr>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="panel/field">
    <tr bgcolor="#FFFF88">
      <td><xsl:value-of select="@label" />:</td>
      <td><xsl:value-of select="." /></td>
    </tr>
  </xsl:template>

  <xsl:template match="panel/field[@empty]">
    <tr bgcolor="#FFFF88">
      <td colspan="{@empty}"><xsl:value-of select="@label" />
        <xsl:apply-templates />
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="orderlist">
    <p>
      <center>
        <h2><xsl:value-of select="@name" /></h2>
      </center>
      <table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="3">
        <tr bgcolor="#CCCCCC">
          <td>
            <b>Order ID</b>
          </td>
          <td>
            <b>Date</b>
          </td>
          <td>
            <b>Total Price</b>
          </td>
          <td>
            <b>Status</b>
          </td>
        </tr>
        <xsl:apply-templates/>
      </table>
    </p>
  </xsl:template>

  <xsl:template match="orderlist/order">
    <tr bgcolor="#FFFF88">
      <td>
        <b><a href="viewOrder.do?orderId={@id}"><font color="BLACK"><xsl:value-of select="@id" /></font></a></b>
      </td>
      <td>
        <xsl:value-of select="date"/>
      </td>
      <td>
        $<xsl:value-of select="format-number (total, '###,##0.00')"/>
      </td>
      <td>
        <b><xsl:value-of select="@status" /></b>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="error">
    <h3>Error</h3>
    <b><xsl:value-of select="." /></b>
  </xsl:template>



</xsl:stylesheet>

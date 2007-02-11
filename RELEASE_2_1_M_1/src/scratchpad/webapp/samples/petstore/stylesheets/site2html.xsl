<?xml version="1.0" encoding="utf-8"?>


<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xf="http://xml.apache.org/cocoon/xmlform/2002"
	exclude-result-prefixes="xalan" >

	<xsl:template match="site">
		<html>
			<head>
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
								   <select name="view" width="14" align="left" onChange="document.forms[0].submit()">
								<xsl:choose>
									<xsl:when test="@view='jxpath'">
									  <option value="JXPath">JXPath</option>
									  <option value="Jexl">Jexl</option>
									  <option value="Xsp">Xsp</option>
									  <option value="Velocity">Velocity</option>
                                                                        </xsl:when>
									<xsl:when test="@view='jexl'">
									  <option value="Jexl">Jexl</option>
									  <option value="JXPath">JXPath</option>
									  <option value="Xsp">Xsp</option>
									  <option value="Velocity">Velocity</option>
                                                                        </xsl:when>
									<xsl:otherwise>
									  <option value="Xsp">Xsp</option>
									  <option value="JXPath">JXPath</option>
									  <option value="Jexl">Jexl</option>
									  <option value="Velocity">Velocity</option>
									</xsl:otherwise>
							              </xsl:choose>
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
										<a href="editAccount.form"><img border="0" name="img_myaccount" src="images/my_account.gif" /></a>
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
					<a href="http://cocoon.apache.org"><img border="0" align="center" src="images/cocoon.gif" /></a>
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
				<tr>
					<td>
						<!-- 
						#if (!$accountForm.signOn)
						<b><i><font size="2" color="BLACK">Welcome $accountForm.account.firstName!</font></i></b>
						#end
						-->
					</td>
				</tr>
				<xsl:apply-templates/>
			</tbody>
		</table>
	</xsl:template>

	<xsl:template match="menu/category">
		<tr>
			<td>
				<a href="viewCategory.do?categoryId={@id}"><i><h2><xsl:value-of select="@name" /></h2></i></a>
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
			<td><a href="viewProduct.do?productId={@id}"><xsl:value-of select="product-desc" /></a></td>
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
					<a href="{@continuation}.kont?page=previous"><font color="white"><B>&lt;&lt; Prev</B></font></a>
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
				<b><xsl:value-of select="@product-id" /></b>
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
	</xsl:template>

	<xsl:template match="cart[@name='Shopping Cart']">
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
								<td></td>
							</tr>
							<xsl:if test="not(item)">
								<tr bgcolor="#FFFF88">
									<td colspan="6">
										<b>Your cart is empty.</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:apply-templates select="item" />
							<tr bgcolor="#FFFF88">
								<td colspan="5" align="right">
									<b>Sub Total: $<xsl:value-of select="format-number (total, '###,##0.00')"/></b><br />
									<input type="image" border="0" src="images/button_update_cart.gif" name="update" />
								</td>
								<td></td>
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

	<xsl:template match="cart[@name='Checkout Summary']">
		<table border="0" width="100%" cellspacing="0" cellpadding="0">
			<tr>
				<td valign="top" width="20%" align="left">
					<xsl:apply-templates select="backpointer" />
				</td>
				<td valign="top" align="center">
					<h2 align="center"><xsl:value-of select="@name" /></h2>
						<table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="5">
							<tr bgcolor="#cccccc">
								<xsl:call-template name="cart-common-columns" />
							</tr>
							<xsl:apply-templates select="item" />
							<tr bgcolor="#FFFF88">
								<td colspan="5" align="right">
									<b>Sub Total: $<xsl:value-of select="format-number (total, '###,##0.00')"/></b><br />
								</td>
							</tr>
						</table>
						<xsl:apply-templates select="nextpointer" />
				</td>
				<td valign="top" width="20%" align="right">
				</td>
			</tr>
		</table>
	</xsl:template>

	<xsl:template match="cart[@name='Status']">
		<table align="center" bgcolor="#008800" border="0" cellspacing="2" cellpadding="5">
			<tr bgcolor="#cccccc">
				<xsl:call-template name="cart-common-columns" />
			</tr>
			<xsl:apply-templates select="item" />
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


	<xsl:template match="cart/item">
		<tr bgcolor="#FFFF88">
			<td>
				<b><xsl:value-of select="@id" /></b>
			</td>
			<td>
				<xsl:value-of select="@product-id" />
			</td>
			 <td>
					<xsl:value-of select="desc" />
			 </td>
			<td align="center">
				<xsl:choose>
					<xsl:when test="../@name='Shopping Cart'">
						<input type="text" size="3" name="{@id}" >
							<xsl:attribute name="value"><xsl:value-of select="format-number (quantity, '####')"/></xsl:attribute>
						</input>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="format-number (quantity, '####')"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td align="right">
				$<xsl:value-of select="format-number (price, '###,##0.00')"/>
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
						<xsl:value-of select="product-desc" />
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
					<!-- quantity stuff still missing -->
				
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

	<xsl:template match="form[@styleId='workingAccountForm']">
		<form>
			<xsl:copy-of select="@action | @method | @styleId "/>
			<xsl:choose>
				<xsl:when test="/site/@signOn='true'">
					<hidden name="workingAccountForm" property="validate" value="newAccount"/>
				</xsl:when>
				<xsl:otherwise>
					<hidden name="workingAccountForm" property="validate" value="editAccount" />
					<hidden name="workingAccountForm" property="account.username" />
				</xsl:otherwise>
			</xsl:choose>
			<table cellpadding="10" cellspacing="0" align="center" border="1" bgcolor="#dddddd">
				<tr>
					<td>
					<xsl:apply-templates/>
					</td>
				</tr>
			</table>
			<br />
			<center>
				<input border="0" type="image" src="images/button_submit.gif" name="submit" value="Save Account Information" />
			</center>
		</form>
		<xsl:if test="/site/@signOn='false'">
			<p>
				<center><b><a href="listOrders.do">My Orders</a></b></center>
			</p>
		</xsl:if>
	</xsl:template>

	
	<xsl:template match="form[@styleId='workingOrderForm']">
		<b><font color="RED"><xsl:value-of select="message" /></font></b>
		<form>
			<xsl:copy-of select="@action | @method | @styleId"/>
				<xsl:apply-templates/>
		<p>
			<input type="image" src="images/button_submit.gif"/>
		</p>
		</form>
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
	
	
	<xsl:template match="panel[panel]">
		<table width="60%" align="center" border="0" cellpadding="3" cellspacing="1" bgcolor="#FFFF88">
			<xsl:if test="@header">
				<tr bgcolor="#FFFF88">
					<td align="center" colspan="2">
						<font size="4"><b><xsl:value-of select="@header" /></b></font>
						<xsl:if test="@subheader">
							<br /><font size="3"><b><xsl:value-of select="@subheader" /></b></font>
						</xsl:if>
					</td>
				</tr>
			</xsl:if>
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	
	<xsl:template match="panel">
		<font color="darkgreen"><h3><xsl:value-of select="@label" /></h3></font>
		<table border="0" cellpadding="3" cellspacing="1" bgcolor="#008800">
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	
	<xsl:template match="panel/panel">
		<tr bgcolor="#FFFF88">
			<td colspan="2">
				<font color="GREEN" size="4"><b><xsl:value-of select="@label" />:</b></font>
			</td>
		</tr>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="panel/select">
		<tr bgcolor="#FFFF88">
			<td><xsl:value-of select="@label" /></td>
			<td>
				<select>
					<xsl:copy-of select="@type | @src | @value | @name | @size | @selected | node()" />
				</select>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="panel/input">
		<tr bgcolor="#FFFF88">
			<td><xsl:value-of select="@label" /></td>
			<td><input><xsl:copy-of select="@type | @src | @value | @name | @size | @selected"/></input></td>
		</tr>
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

	<xsl:template match="message">
		<br clear="all" />
			<center>
				<b>
					<font size="4">
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
			<a href="newAccountForm.do"><img border="0" src="images/button_register_now.gif" /></a>
		</center>
	</xsl:template>


<!-- XML-Form -->


	<xsl:template match="xf:form[@view='edit-account']">
		<form>
			<xsl:copy-of select="@*"/>
			<input type="hidden" name="cocoon-xmlform-view" value="{@view}"/>
			<table cellpadding="10" cellspacing="0" align="center" border="1" bgcolor="#dddddd">

				<xsl:if test="count(error/xf:violation) > 0">
					<tr>
						<td align="left" colspan="3"
							class="{error/xf:violation[1]/@class}">
							<p>* There are [<b><xsl:value-of
								select="count(error/xf:violation)"/></b>] 
								errors. Please fix these errors and submit the
								form again.</p>
						</td>
					</tr>
				</xsl:if>

				<tr>
					<td>
					<xsl:apply-templates select="*[name() != 'xf:submit']" />
					</td>
				</tr>
			</table>
			<br />
			<center>
				<!--<input border="0" type="image" src="images/button_submit.gif" name="submit" value="Save Account Information"  id="{xf:submit/@id}" continuation="forward"/> -->
			<xsl:apply-templates select="xf:submit" />
			</center>
		</form>
		<xsl:if test="/site/@signOn='false'">
			<p>
				<center><b><a href="listOrders.do">My Orders</a></b></center>
			</p>
		</xsl:if>
	</xsl:template>

	<xsl:template match="xf:group/xf:caption" />
	<xsl:template match="xf:hint">
		<xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="xf:violation" />

	<xsl:template match="xf:group">
		<font color="darkgreen"><h3><xsl:value-of select="xf:caption" /></h3></font>
		<table border="0" cellpadding="3" cellspacing="1" bgcolor="#008800">
			<xsl:apply-templates/>
		</table>
	</xsl:template>


	<xsl:template match="xf:textbox">
		<tr bgcolor="#FFFF88">
			<td><xsl:value-of select="xf:caption" /></td>
			<td>
				<input name="{@ref}" type="textbox" value="{xf:value/text()}">
					<xsl:copy-of select="@*[not(name()='ref')]"/>
				</input>
				<xsl:apply-templates select="xf:hint"/>
				<xsl:apply-templates select="xf:violation"/>
			</td>
		</tr>
   </xsl:template>
   
	<xsl:template match="xf:password">
		<tr bgcolor="#FFFF88">
			<td><xsl:value-of select="xf:caption" /></td>
			<td>
				<input name="{@ref}" type="password" value="{xf:value/text()}">
					<xsl:copy-of select="@*[not(name()='ref')]"/>
				</input>
				<xsl:apply-templates select="xf:violation"/>
			</td>
		</tr>
   </xsl:template>


	<xsl:template match="xf:selectOne | xf:selectOne[@selectUIType='listbox']">
		<tr bgcolor="#FFFF88">
			<td><xsl:value-of select="xf:caption" /></td>
			<td>
				<select name="{@ref}">
					<xsl:copy-of select="@*[not(name()='ref')]"/>
					<xsl:variable name="selected" select="xf:value"/>
					<xsl:for-each select="xf:item">
						<option value="{xf:value}">
							<xsl:if test="$selected = xf:value">
								<xsl:attribute name="selected"/>
							</xsl:if>
							<xsl:value-of select="xf:caption"/>
						</option>
					</xsl:for-each>
				</select>
				<xsl:apply-templates select="xf:violation"/>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="xf:selectBoolean">
		<tr bgcolor="#FFFF88">
			<td colspan="2">
				<input name="{@ref}" type="checkbox" value="true">
					<xsl:copy-of select="@*[not(name()='ref')]"/>
					<xsl:if test="xf:value/text() = 'true'">
						<xsl:attribute name="checked"/>
					</xsl:if>
				</input> 
				<xsl:value-of select="xf:caption" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="xf:submit">
       <!-- the id attribute of the submit control is sent to the server -->
       <!-- as a conventional Cocoon Action parameter of the form cocoon-action-* -->
		<input name="cocoon-action-{@id}" type="submit" value="{xf:caption/text()}">
			<xsl:copy-of select="@*[not(name()='id')]"/>
			<xsl:apply-templates select="xf:hint"/>
		</input>
	</xsl:template>

</xsl:stylesheet>


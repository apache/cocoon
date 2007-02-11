/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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

*/

// Page Flow for PetStore Application

// load xml form support
cocoon.load("resource://org/apache/cocoon/components/flow/javascript/xmlForm.js");

var MAX_RESULTS = 5;

var VIEW = "Velocity";
var EXT = ".vm";


function Format() {
}

Format.prototype.formatNumber = function(num, format) {
    return new java.text.DecimalFormat(format).format(num);
}

Format.prototype.formatDate = function(date, format) {
    return new java.text.SimpleDateFormat(format).format(date);
}

var fmt = new Format();
var petStore = null;
var accountForm = null;
var cartForm = null;
var categoryList = null;


function main(funName) {
    var fun = this[funName];
    var args = new Array(arguments.length -1);
    for (var i = 1; i < arguments.length; i++) {
        args[i-1] = arguments[i];
    }
    getPetStore();
    fun.apply(args);

}

function getPetStore() {
    if (petStore == null) {
        cocoon.createSession();
        this.petStore = new PetStore("hsql");
        this.cartForm = new CartForm();
        this.accountForm = new AccountForm();
        this.categoryList = getPetStore().getCategoryList();
    }
    return petStore;
}

function setView() {
    VIEW = cocoon.request.get("view");
    print("setView: VIEW="+VIEW);
    if (VIEW == "Velocity") {
        EXT = ".vm";
    } else if (VIEW == "Xsp") {
        EXT = ".xsp";
    } else if (VIEW == "Jexl") {
        EXT = ".jexl";
    } else if (VIEW == "JXPath") {
        EXT = ".jxpath";
    }
    print("EXT="+EXT);
}

// Index page

function index() {
    setView();
    getPetStore();
    sendPage("/view/index" + EXT, {
             view: VIEW,
             accountForm: accountForm,
             categoryList: categoryList,
    });
}

// Cart page

function viewCart() {
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        cartItems.push(cartItem);
    }
    sendPage("/view/Cart" + EXT, {
             view: VIEW,
             accountForm: accountForm, 
             cartForm: cartForm, 
             fmt: fmt,
             cartItems: cartItems

    });
}

function removeItemFromCart() {
    var itemId = cocoon.request.getParameter("workingItemId");
    var item = getPetStore().getItem(itemId);
    cartForm.cart.removeItem(item);
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        cartItems.push(cartItem);
    }
    sendPage("/view/Cart" + EXT, {
             view: VIEW,
             fmt: fmt, 
             accountForm: accountForm, 
                        cartForm: cartForm, cartItems: cartItems
    });
}

function updateCartQuantities() {
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        var itemId = cartItem.item.itemId;
        var quantity = new java.lang.Double(cocoon.request.get(itemId)).intValue();
        cartItem.updateQuantity(quantity);
        cartItems.push(cartItem);
    }
    sendPage("/view/Cart" + EXT, {
             view: VIEW,
             fmt: fmt, 
             accountForm: accountForm, 
             cartForm:cartForm,
             cartItems: cartItems
    });
}

function addItemToCart() {
    var itemId = cocoon.request.getParameter("itemId");
    var item = getPetStore().getItem(itemId);
    cartForm.cart.addItem(item);
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        cartItems.push(cartItem);
    }
    sendPage("/view/Cart" + EXT, {
             view: VIEW,
             fmt: fmt, 
             accountForm: accountForm, 
             cartForm: cartForm, 
             cartItems: cartItems
    });
}


// Category page

function viewCategory() {
    var categoryId = cocoon.request.get("categoryId");
    var category = getPetStore().getCategory(categoryId);
    var skipResults = 0;
    var maxResults = MAX_RESULTS;
    while (true) {
        var productList = 
            getPetStore().getProductListByCategory(categoryId,
                                                   skipResults, 
                                                   maxResults);
        var lastPage = !productList.isLimitedByMaxRows;
        var rowCount = productList.rowCount;
        sendPageAndWait("/view/Category" + EXT, {
                        accountForm: accountForm, 
                        productList: productList.rows, 
                        category: category, 
                        firstPage: skipResults == 0, 
                        lastPage: lastPage
        });

        catch (break) {
            print("zapping productList");
            productList = null;
        }
        
        catch (continue) {
            print("returning from continuation");
            print("productList="+productList);
        }

        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (skipResults != 0) {
                skipResults -= maxResults;
            }
        } else if (page == "next") {
            if (!lastPage) {
                skipResults += rowCount;
            }
        }
    } 
}

// Product page

function viewProduct() {
    var productId = cocoon.request.get("productId");
    var product = getPetStore().getProduct(productId);
    var skipResults = 0;
    var maxResults = MAX_RESULTS;

    while (true) {
        var itemList = 
            getPetStore().getItemListByProduct(productId,
                                               skipResults, 
                                               maxResults);
        sendPageAndWait("/view/Product" + EXT, {
                        view: VIEW,
                        accountForm: accountForm, 
                        fmt: fmt,
                        product: product,
                        firstPage: skipResults == 0, 
                        lastPage: !itemList.isLimitedByMaxRows,
                        itemList: itemList.rows
        });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (skipResults != 0) {
                skipResults -= maxResults;
            }
        } else if (page == "next") {
            if (!itemList.isLimitedByMaxRows) {
                skipResults += itemList.rowCount;
            }
        } 
    }
}

// Item page

function viewItem() {
    var itemId = cocoon.request.getParameter("itemId");
    var item = getPetStore().getItem(itemId);
    sendPage("/view/Item" + EXT, {
             view: VIEW,
             accountForm: accountForm, 
             cartForm: cartForm, 
             item: item, 
             quantity: getPetStore().getItemRowCountByProduct(item.productId),
             product: item.product, 
             fmt: fmt
    });
}


// Sign-on page

function signonForm() {
    signOn();
    index();
}

function signOn() {
    if (cocoon.request.get("signoff") != null) {
        accountForm = new AccountForm();
        cartForm = new CartForm();
    } else {
        var message = "";
        while (true) {
            sendPageAndWait("/view/SignonForm" + EXT, {
                            view: VIEW,
                            accountForm: accountForm, 
                            message: message
            });
            var username = cocoon.request.get("username");
            var password = cocoon.request.get("password");
            print("getting account: " + username);
            account = getPetStore().getAccount(username, password);
            if (account == null) {
                message = "Invalid username or password";
            } else {
                accountForm = new AccountForm(username, password);
                accountForm.account = account;
                accountForm.signOn = false;
                break;
            }
        } 
    }
}

// Account Form

function newAccountForm() {
    print("new account");
    var accountForm = new AccountForm();
    var account = new Account();
    sendPageAndWait("/view/NewAccountForm" + EXT, {
                     view: VIEW,
                     accountForm: accountForm,
                     account: account,
                     categoryList: categoryList
    });
}

//
// Edit Account page: example of using XMLForm in a flow script
//


function validateZIP(field) {
    var valid = "0123456789-";
    var hyphencount = 0;
    if (field.length != 5 && field.length != 10) {
        throw "Please enter your 5 digit or 5 digit+4 zip code.";
    }
    for (var i=0; i < field.length; i++) {
        var temp = "" + field.substring(i, i+1);
        if (temp == "-") hyphencount++;
        if (valid.indexOf(temp) == "-1") {
            throw "Invalid characters in your zip code";
        }
    }
    if (hyphencount > 1 || (field.length == 10 && field.charAt(5) != "-")) {
        throw "The hyphen character should be used with a properly formatted 5 digit+four zip code, like '12345-6789'";
    }
}

function validateEmail(value) {
    var reg  = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return reg.test(value);
}

function editAccountForm(form) {
    var model = {accountForm: accountForm,
                 account: accountForm.account,
                 categoryList: categoryList, 
                 username: accountForm.account.userid,
                 password: "",
                 password2: ""};
    form.setModel(model);
    form.sendView("editAccountForm", 
                  "view/xmlform/EditAccountForm.xml", 
                  function(form) {
        for (var i in model.account) {
            print(i+"="+model.account[i]);
        }
        if (model.userName == "") {
            form.addViolation("/userName", "User ID is required");
        } else {
            if (model.password != model.password2) {
                form.addViolation("/password2", "Passwords don't match");
            }
        }
        if (account.firstName == "") {
            form.addViolation("/account/firstName", "First name is required");
        }
        if (account.lastName == "") {
            form.addViolation("/account/lastName", "Last name is required");
        }
        if (!validateEmail(account.email)) {
            form.addViolation("/account/email", "Email address is invalid");
        }
        try {
            validateZIP(account.zip);
        } catch (e) {
            form.addViolation("/account/zip", e);
        }

    });
    index();
}

// Search

function searchProducts() {
    var keyword = cocoon.request.get("keyword");
    if (keyword == null || keyword == "") {
        sendPage("/view/Error" + EXT, {
           view: VIEW,
           message: "Please enter a keyword to search for, then press the search button"
        });
        return;
    }
    var skipResults = 0;
    var maxResults = 3;
    while (true) {
        var result = 
            getPetStore().searchProductList(keyword, skipResults,
                                            maxResults);
        sendPageAndWait("/view/SearchProducts" + EXT, {
                        view: VIEW,
                        searchResultsProductList: result.rows,
                        firstPage: skipResults == 0,
                        lastPage: !result.isLimitedByMaxRows
        });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (skipResults != 0) {
                skipResults -= maxResults;
            }
        } else if (page == "next") {
            if (!result.isLimitedByMaxRows) {
                skipResults += result.rowCount;
            }
        }
    }
}

// Checkout

function checkout() {
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        cartItems.push(cartItem);
    }
    sendPageAndWait("/view/Checkout" + EXT, {
                    view: VIEW,
                    accountForm: accountForm,
                    cartForm: cartForm, 
                    fmt: fmt,
                    cartItems: cartItems
    });
    if (accountForm.signOn) {
        signOn();
    }
    var orderForm = new OrderForm();
    orderForm.initOrder(accountForm, cartForm);
    var order = orderForm.order;
    var valid = false;
    while (!valid) {
        sendPageAndWait("/view/NewOrderForm" + EXT, { 
	                accountForm: accountForm,
                        view: VIEW,
                        fmt: fmt,
                        creditCardTypes: ["Visa", "MasterCard", "American Express"],
                        order: order});
        var shippingAddressRequired = cocoon.request.get("shippingAddressRequired");
        if (shippingAddressRequired) {
            sendPageAndWait("/view/ShippingForm" + EXT,
                            {order: order, fmt: fmt, accountForm: accountForm});
        }
        // fix me !! do real validation
        valid = true;
    }
    sendPageAndWait("/view/ConfirmOrder" + EXT,
	            {accountForm: accountForm,
                     view: VIEW, order: order, fmt: fmt});
    
    var oldCartForm = cartForm;
    cartForm = new CartForm();
    sendPage("/view/ViewOrder" + EXT,
             {view: VIEW, order: order, 
	      accountForm: accountForm,
              itemList: order.lineItems, 
              fmt: fmt});
}

function listOrders() {
}

function viewOrder() {
    var webservice = cocoon.request.get("webservice");
    if (webservice) {
    }
}




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

var MAX_RESULTS = 5;

function Yoshi() {
}

Yoshi.prototype.formatNumber = function(num, format) {
    return new java.text.DecimalFormat(format).format(num);
}

Yoshi.prototype.formatDate = function(date, format) {
    return new java.text.SimpleDateFormat(format).format(date);
}

var yoshi = new Yoshi();
var petStore = null;
var accountForm = null;
var cartForm = null;
var categoryList = null;

function getPetStore() {
    if (petStore == null) {
        petStore = new PetStore("hsql");
    }
    return petStore;
}

// Index page

function index() {
    if (petStore == null) {
        cocoon.createSession();
        cartForm = new CartForm();
        accountForm = new AccountForm();
    }
    this.categoryList = getPetStore().getCategoryList();
    sendPage("view/templates/index.vm", {
             accountForm: accountForm,
             categoryList: categoryList
    });
}

// Cart page

function viewCart() {
    sendPage("view/templates/Cart.vm", {
             accountForm: accountForm, 
             cartForm: cartForm, 
             yoshi: yoshi
    });
}

function removeItemFromCart() {
    var itemId = cocoon.request.getParameter("workingItemId");
    var item = getPetStore().getItem(itemId);
    cartForm.cart.removeItem(item);
    sendPage("view/templates/Cart.vm", {
             yoshi: yoshi, 
             accountForm: accountForm, 
             cartForm: cartForm
    });
}

function updateCartQuantities() {
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        var itemId = cartItem.item.itemId;
        var quantity = new java.lang.Double(cocoon.request.get(itemId)).intValue();
        cartItem.updateQuantity(quantity);
    }
    sendPage("view/templates/Cart.vm", {
             yoshi: yoshi, 
             accountForm: accountForm, 
            cartForm:cartForm
    });
}

function addItemToCart() {
    var itemId = cocoon.request.getParameter("itemId");
    var item = getPetStore().getItem(itemId);
    cartForm.cart.addItem(item);
    sendPage("view/templates/Cart.vm", {
             yoshi: yoshi, 
             accountForm: accountForm, 
             cartForm:cartForm
    });
}


// Category page

function viewCategory() {
    var categoryId = cocoon.request.get("categoryId");
    var category = getPetStore().getCategory(categoryId);
    var skipResults = 0;
    var maxResults = MAX_RESULTS;
    while (true) {
        var productList = getPetStore().getProductListByCategory(categoryId,
                                                                 skipResults, 
                                                                 maxResults);
        sendPageAndWait("view/templates/Category.vm", {
                        accountForm: accountForm, 
                        productList: productList.rows, 
                        category: category, 
                        firstPage: skipResults == 0, 
                        lastPage: !productList.isLimitedByMaxRows
        });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            skipResults -= maxResults;
        } else if (page == "next") {
            skipResults += productList.rowCount;
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
        var itemList = getPetStore().getItemListByProduct(productId,
                                                          skipResults, 
                                                          maxResults);
        sendPageAndWait("view/templates/Product.vm", {
                        accountForm: accountForm, 
                        yoshi: yoshi,
                        product: product,
                        firstPage: skipResults == 0, 
                        lastPage: !itemList.isLimitedByMaxRows,
                        itemList: itemList.rows
        });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            skipResults -= maxResults;
        } else if (page == "next") {
            skipResults += itemList.rowCount;
        } 
    }
}

// Item page

function viewItem() {
    var itemId = cocoon.request.getParameter("itemId");
    var item = getPetStore().getItem(itemId);
    sendPageAndWait("view/templates/Item.vm", {
                    accountForm: accountForm, 
                    cartForm: cartForm, 
                    item: item, 
                    product: item.product, 
                    yoshi: yoshi
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
            sendPageAndWait("view/templates/SignonForm.vm", {
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
    sendPageAndWait("view/templates/NewAccountForm.vm", {
                     accountForm: accountForm,
                     account: account,
                     categoryList: categoryList
    });
}

function editAccountForm() {
    if (accountForm.signOn) {
        newAccountForm();
    } else {
        sendPageAndWait("view/templates/EditAccountForm.vm", {
                        accountForm: accountForm,
                        account: accountForm.account,
                        categoryList: categoryList
        });
    }
}

// Search

function searchProducts() {
    var keyword = cocoon.request.get("keyword");
    if (keyword == null || keyword == "") {
        sendPage("view/templates/Error.vm", {
           message: "Please enter a keyword to search for, then press the search button"
        });
        return;
    }
    var skipResults = 0;
    var maxResults = 3;
    while (true) {
        var result = getPetStore().searchProductList(keyword, skipResults,
                                                     maxResults);
        sendPageAndWait("view/templates/SearchProducts.vm", {
                        searchResultsProductList: result.rows,
                        firstPage: skipResults == 0,
                        lastPage: !result.isLimitedByMaxRows
        });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            skipResults -= maxResults;
        } else if (page == "next") {
            skipResults += result.rowCount;
        }
    }
}

// Checkout

function checkout() {
    sendPageAndWait("view/templates/Checkout.vm", {
                    accountForm: accountForm,
                    cartForm: cartForm, 
                    yoshi: yoshi
    });
    if (accountForm.signOn) {
        signOn();
    }
    var orderForm = new OrderForm();
    orderForm.initOrder(accountForm, cartForm);
    var order = orderForm.order;
    var valid = false;
    while (!valid) {
        sendPageAndWait("view/templates/NewOrderForm.vm", { 
                        yoshi: yoshi,
                        creditCardTypes: ["Visa", "MasterCard", "American Express"],
                        order: order});
        var shippingAddressRequired = cocoon.request.get("shippingAddressRequired");
        if (shippingAddressRequired) {
            sendPageAndWait("view/templates/ShippingForm.vm",
                            {order: order, yoshi: yoshi});
        }
        // fix me !! do real validation
        valid = true;
    }
    sendPageAndWait("view/templates/ConfirmOrder.vm",
                    {order: order, yoshi: yoshi});
    
    var oldCartForm = cartForm;
    cartForm = new CartForm();
    sendPage("view/templates/ViewOrder.vm",
             {order: order, itemList: order.lineItems, yoshi: yoshi});
}

function listOrders() {
}

function viewOrder() {
    var webservice = cocoon.request.get("webservice");
    if (webservice) {
    }
}




/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Page Flow for PetStore Application

// load WOODY support
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/forms.js");

var MAX_RESULTS = 5;

var VIEW = "jexl";
var EXT = ".jexl";

// Utility to format numbers and dates (for use by Velocity templates)

function Formatter() {
}

Formatter.prototype.formatNumber = function(num, format) {
    return new java.text.DecimalFormat(format).format(num);
}

Formatter.prototype.formatDate = function(date, format) {
    return new java.text.SimpleDateFormat(format).format(date);
}

var formatter = new Formatter();
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
    if (VIEW == "velocity") {
        EXT = ".vm";
    } else if (VIEW == "xsp") {
        EXT = ".xsp";
    } else if (VIEW == "jexl") {
        EXT = ".jexl";
    } else if (VIEW == "jxpath") {
        EXT = ".jxpath";
    }
    print("EXT="+EXT);
}

// Index page

function index() {
    setView();
    getPetStore();
    cocoon.sendPage("view/index" + EXT, {
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
    cocoon.sendPage("view/Cart" + EXT, {
            accountForm: accountForm,
            cartForm: cartForm,
            fmt: formatter,
            cartItems: cartItems,
            label: "Shopping Cart"
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
    cocoon.sendPage("view/Cart" + EXT, {
            fmt: formatter,
            accountForm: accountForm,
            cartForm: cartForm,
            cartItems: cartItems,
            label: "Shopping Cart"
    });
}

function updateCartQuantities() {
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        var itemId = cartItem.item.itemId;
        var quantity = parseInt(cocoon.request.get(itemId));
        cartItem.updateQuantity(quantity);
        cartItems.push(cartItem);
    }
    cocoon.sendPage("view/Cart" + EXT, {
            fmt: formatter,
            accountForm: accountForm,
            cartForm:cartForm,
            cartItems: cartItems,
            label: "Shopping Cart"
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
    cocoon.sendPage("view/Cart" + EXT, {
            fmt: formatter,
            accountForm: accountForm,
            cartForm: cartForm,
            cartItems: cartItems,
            label: "Shopping Cart"
    });
}

// Category page

function viewCategory() {
    var categoryId = cocoon.request.get("categoryId");
    var category = getPetStore().getCategory(categoryId);
    var maxResults = MAX_RESULTS;
    /* page local variable to keep track of pagination */
    var local = cocoon.createPageLocal();
    local.skipResults = 0;
    while (true) {
        var productList =
            getPetStore().getProductListByCategory(categoryId,
                                                   local.skipResults,
                                                   maxResults);
        local.lastPage = !productList.isLimitedByMaxRows;
        local.rowCount = productList.rowCount;
        var contextData = {
            accountForm: accountForm,
            productList: productList.rows,
            category: category,
            firstPage: local.skipResults == 0,
            lastPage: local.lastPage
        };
        cocoon.sendPageAndWait("view/Category" + EXT, 
                               contextData, 
                               function () {
                                   /* release contextData and productList */
                                   contextData = null;
                                   productList = null;
                               });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (local.skipResults != 0) {
                local.skipResults -= maxResults;
            }
        } else if (page == "next") {
            if (!local.lastPage) {
                local.skipResults += local.rowCount;
            }
        }
    }
}

// Product page

function viewProduct() {
    var productId = cocoon.request.get("productId");
    var product = getPetStore().getProduct(productId);
    var maxResults = MAX_RESULTS;
    /* page local variable to handle pagination */
    var local = cocoon.createPageLocal();
    local.skipResults = 0;
    while (true) {
        var itemList =
            getPetStore().getItemListByProduct(productId,
                                               local.skipResults,
                                               maxResults);
        local.lastPage = !itemList.isLimitedByMaxRows;
        var contextData = {
            accountForm: accountForm,
            fmt: formatter,
            product: product,
            firstPage: local.skipResults == 0,
            lastPage: local.lastPage,
            itemList: itemList.rows
        };
        cocoon.sendPageAndWait("view/Product" + EXT, 
                               contextData,
                               function() {
                                   /* release contextData and itemList */
                                   contextData = null;
                                   itemList = null;
                               });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (local.skipResults != 0) {
                local.skipResults -= maxResults;
            }
        } else if (page == "next") {
            if (!local.lastPage) {
                local.skipResults += itemList.rowCount;
            }
        }
    }
}

// Item page

function viewItem() {
    var itemId = cocoon.request.getParameter("itemId");
    var item = getPetStore().getItem(itemId);
    cocoon.sendPage("view/Item" + EXT, {
             accountForm: accountForm,
             cartForm: cartForm,
             item: item,
             quantity: getPetStore().getItemRowCountByProduct(item.productId),
             product: item.product,
             fmt: formatter
    });
}

// Sign-on page

function signonForm() {
    signOn();
    index();
}

function signOn(process) {
    if (cocoon.request.get("signoff") != null) {
        accountForm = new AccountForm();
        cartForm = new CartForm();
    } else {
        var message = "";
        var registerType;
        if (process) {
            registerType = process;
        } else {
            registerType = "new";
        }
        while (true) {
            cocoon.sendPageAndWait("view/SignonForm" + EXT, {
                            accountForm: accountForm,
                            message: message,
                            registerType: registerType
            });
            var username = cocoon.request.get("username");
            var password = cocoon.request.get("password");
            print("getting account: " + username);
            var account = getPetStore().getAccount(username, password);
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

// Account Forms

function editAccount() {
    editAccountData();
    cocoon.sendPage("index.do");

}

function newAccount() {
    newAccountData();
    cocoon.sendPage("index.do");
}

function instantAccount() {
    newAccountData();
    cocoon.sendPage("checkout.do");
}

function editAccountData() {
    var editAccountDataForm = new Form("view/forms/editAccountForm_d.xml");
    var model = editAccountDataForm.getModel();
    model.message = "";
    model.username = accountForm.username;
    model.changePwdOption = false;
    model.password = "";
    model.retypepassword = "";
    model.firstname = accountForm.account.firstname;
    model.lastname = accountForm.account.lastname;
    model.email = accountForm.account.email;
    model.phone= accountForm.account.phone;
    model.addr1 = accountForm.account.addr1;
    model.addr2 = accountForm.account.addr2;
    model.city = accountForm.account.city;
    model.state = accountForm.account.state;
    model.zip = accountForm.account.zip;
    model.country = accountForm.account.country;
    model.langpref = accountForm.account.langpref;
    model.favcategory = accountForm.account.favcategory;
    model.mylistopt = accountForm.account.mylistopt;
    model.banneropt = accountForm.account.banneropt;

    editAccountDataForm.showForm("view/editAccountForm.cforms");
    while (model.changePwdOption && (model.password != model.retypepassword || model.password == null)) {
        model.message = "Passwords don't match!";
        editAccountDataForm.showForm("view/editAccountForm.cforms");
    }

    if (!accountForm.signOn) {
        var update = getPetStore().updateAccount(model);
    } else {
        var insert = getPetStore().insertAccount(model);
        accountForm.signOn = false;
    }

    if (model.changePwdOption) {
        var chPwd = getPetStore().updateSignon(accountForm.username, model.password);
        accountForm.password = model.password;
    }
    accountForm.account = getPetStore().getAccount(accountForm.username, accountForm.password);
}

function newAccountData() {
    var newAccountDataForm = new Form("view/forms/newAccountForm_d.xml");
    var model = newAccountDataForm.getModel();

    model.message = "";
    model.username = "";
    model.password = "";
    model.retypepassword = "";

    newAccountDataForm.showForm("view/newAccountForm.cforms");
    while (getPetStore().testDuplicateLogin(model.username) > 0) {
        model.message = "Username already in use. Please choose another username.";
        newAccountDataForm.showForm("view/newAccountForm.cforms");
    }
    var insertNewUser = getPetStore().insertNewUser(model);
    print("insertNewUser: "+insertNewUser);
    accountForm = new AccountForm(model.username, model.password);
    accountForm.account = new Account();
    editAccountData();

}

// Search

function empty(str) {
    return str == null ||
      (str instanceof java.lang.String ?
           str.length() == 0 :
                  str.length == 0);
}

function searchProducts() {
    var keyword = cocoon.request.get("keyword");
    if (empty(keyword)) {
        cocoon.sendPage("view/Error" + EXT, {
            accountForm: accountForm,
            message: "Please enter a keyword to search for, then press the search button"
        });
        return;
    }
    /* page local variable to manage pagination */
    var local = cocoon.createPageLocal();
    local.skipSearchResults = 0;
    var maxSearchResults = 3;
    while (true) {
        var result =
            getPetStore().searchProductList(keyword, 
                                            local.skipSearchResults,
                                            maxSearchResults);
        local.lastPage = !result.isLimitedByMaxRows;
        local.rowCount = result.rowCount;
        var contextData = {
            accountForm: accountForm,
            searchResultsProductList: result.rows,
            firstPage: local.skipSearchResults == 0,
            lastPage: local.lastPage
        };
        cocoon.sendPageAndWait("view/SearchProducts" + EXT, 
                               contextData, 
                               function() {
                                   /* release contextData and result */
                                   contextData = null;
                                   result = null;
                               });
        var page = cocoon.request.get("page");
        if (page == "previous") {
            if (local.skipSearchResults != 0) {
                local.skipSearchResults -= maxSearchResults;
            }
        } else if (page == "next") {
            if (!local.lastPage) {
                local.skipSearchResults += local.rowCount;
            }
        }
    }
}

function billingForm(order) {
    var billingForm = new Form("view/forms/newOrderForm_d.xml");
    var model = billingForm.getModel();
    model.cardType = order.cardType;
    model.creditCard = order.creditCard;
    model.expiryDate = order.expiryDate;
    model.billToFirstName = order.billToFirstName;
    model.billToLastName = order.billToLastName;
    model.billAddress1 = order.billAddress1;
    model.billAddress2 = order.billAddress2;
    model.billCity = order.billCity;
    model.billState = order.billState;
    model.billZip = order.billZip;
    model.billCountry = order.billCountry;
    model.shippingAddressRequired = false;
    billingForm.showForm("view/newOrderForm.cforms");
    return model;
}

function shippingForm(order) {
    var shippingForm = new Form("view/forms/newShippingForm_d.xml");
    var model = shippingForm.getModel();
    model.shipToFirstName = order.shipToFirstName;
    model.shipToLastName = order.shipToLastName;
    model.shipAddress1 = order.shipAddress1;
    model.shipAddress2 = order.shipAddress2;
    model.shipCity= order.shipCity;
    model.shipState= order.shipState;
    model.shipZip= order.shipZip;
    model.shipCountry= order.shipCountry;
    shippingForm.showForm("view/newShippingForm.cforms");
    return model;
}

// Checkout

function checkout() {
    var cartItems = [];
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        cartItems.push(cartItem);
    }
    cocoon.sendPageAndWait("view/Cart" + EXT, {
                    accountForm: accountForm,
                    cartForm: cartForm,
                    fmt: formatter,
                    cartItems: cartItems,
                    label: "Checkout Summary"
    });
    if (accountForm.signOn) {
        signOn("instant");
    }
    var orderForm = new OrderForm();
    orderForm.initOrder(accountForm, cartForm);

    var model = billingForm(orderForm.order);
    orderForm.order.billToFirstName = model.billToFirstName;
    orderForm.order.billToLastName = model.billToLastName;
    orderForm.order.billAddress1 = model.billAddress1;
    orderForm.order.billAddress2 = model.billAddress2;
    orderForm.order.billCity = model.billCity;
    orderForm.order.billState = model.billState;
    orderForm.order.billZip = model.billZip;
    orderForm.order.billCountry = model.billCountry;
    orderForm.order.cardType = model.cardType;
    orderForm.order.creditCard = model.creditCard;
    orderForm.order.expiryDate = model.expiryDate;
    orderForm.shippingAddressRequired = model.shippingAddressRequired;
    if (orderForm.shippingAddressRequired == true) {
        model = shippingForm(orderForm.order);
        orderForm.order.shipToFirstName = model.shipToFirstName;
        orderForm.order.shipToLastName = model.shipToLastName;
        orderForm.order.shipAddress1 = model.shipAddress1;
        orderForm.order.shipAddress2 = model.shipAddress2;
        orderForm.order.shipCity = model.shipCity;
        orderForm.order.shipState = model.shipState;
        orderForm.order.shipZip = model.shipZip;
        orderForm.order.shipCountry = model.shipCountry;
    }
    
    cocoon.sendPageAndWait("view/ConfirmOrder" + EXT,
                    {accountForm: accountForm,
                    order: orderForm.order,
                    fmt: formatter});

    orderForm.confirmed = 
        cocoon.request.getParameter("confirmed") == "true";

    if (cartForm.cart.numberOfItems > 0 && orderForm.confirmed) {
        var lastOID = getPetStore().insertOrder(orderForm.order, accountForm.username);
        cartForm = new CartForm();
//        cocoon.sendPage("viewOrder.do?orderId=" + lastOID);
		viewOrder(lastOID);
    }
    else {
        cocoon.sendPage("index.do");
    }
}

function listOrders() {
    var orderList = getPetStore().getOrderList(accountForm.username);
    cocoon.sendPage("view/ListOrders" + EXT, {
            accountForm: accountForm,
            fmt: formatter,
            orderList: orderList
    });
}

function viewOrder(lastOID) {
    var orderId;
    var message;
    if (lastOID != null) {
        orderId = lastOID;
        message = "Thank you, your order has been submitted.";
    } else {
        orderId = cocoon.request.getParameter("orderId");
    }
    var archivedOrder = getPetStore().getOrder(orderId, accountForm.username);
    var lineItemList = getPetStore().getLineItems(orderId);
    cocoon.sendPage("view/ViewOrder" + EXT, {
            accountForm: accountForm,
            fmt: formatter,
            message: message,
            archivedOrder: archivedOrder,
            lineItemList: lineItemList.rows,
            process: {label: "Ordered Items:",
                       id: "checkout"}
    });
}

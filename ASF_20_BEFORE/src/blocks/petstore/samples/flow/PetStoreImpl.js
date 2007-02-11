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

cocoon.load("resource://org/apache/cocoon/components/flow/javascript/Database.js");

function OrderForm() {
    this.order = new Order();
    this.shippingAddressRequired = false;
    this.confirmed = false;
}

OrderForm.prototype.initOrder = function(accountForm, cartForm) {
    var acct = accountForm.account;
    this.order.username = accountForm.username;
    this.order.orderDate = new java.util.Date();
    this.order.shipAddress1 = acct.addr1;
    this.order.shipAddress2 = acct.addr2;
    this.order.shipCity = acct.city;
    this.order.shipState = acct.state;
    this.order.shipZip = acct.zip;
    this.order.shipCountry = acct.country;
    this.order.billAddress1 = acct.addr1;
    this.order.billAddress2 = acct.addr2;
    this.order.billCity = acct.city;
    this.order.billState = acct.state;
    this.order.billZip = acct.zip;
    this.order.billCountry = acct.country;
    this.order.totalPrice = cartForm.cart.subTotal;
    this.order.billToFirstName = acct.firstName;
    this.order.billToLastName = acct.lastName;
    this.order.shipToFirstName = acct.firstName;
    this.order.shipToLastName = acct.lastName;
    this.order.shipDate = new java.util.Date();
    for (var i in cartForm.cart.cartItems) {
        var cartItem = cartForm.cart.cartItems[i];
        this.order.lineItems.push(cartItem);
    }
}

function Order() {
    this.orderId = 0;
    this.username = "";
    this.orderDate = null;
    this.shipAddress1 = "";
    this.shipAddress2 = "";
    this.shipCity = "";
    this.shipState = "";
    this.shipZip = "";
    this.shipCountry = "";
    this.billAddress1 = "";
    this.billAddress2 = "";
    this.billCity = "";
    this.billState = "";
    this.billZip = "";
    this.billCountry = "";
    this.courier = "UPS";
    this.totalPrice = 0;
    this.billToFirstName = "";
    this.billToLastName = "";
    this.shipToFirstName = "";
    this.shipToLastName = "";
    this.creditCard = "";
    this.expiryDate = "";
    this.cardType = "";
    this.locale = "en";
    this.status = "P";
    this.lineItems = [];
}

function Account() {
    this.username = "";
    this.password = "";
    this.email = "";
    this.firstname = "";
    this.lastname = "";
    this.status = "";
    this.addr1 = "";
    this.addr2 = "";
    this.city = "";
    this.state = "";
    this.zip = "";
    this.country = "";
    this.phone = "";
    this.favcategory = "FISH";
    this.langpref = "english";
    this.mylistopt = false;
    this.banneropt = false;
    this.bannername = "";
}

function AccountForm(username, password) {
    this.username = username;
    this.password = password;
    this.signOn = true;
}

function CartItem(cart, item) {
    this.cart = cart;
    this.item = item;
    this.quantity = 1;
    this.listPrice = new Number(item.listPrice);
    this.total = this.listPrice * this.quantity;
    cart.subTotal += this.listPrice;
}

CartItem.prototype.updateQuantity = function(newQuantity) {
    var delta = newQuantity - this.quantity;
    this.cart.subTotal += (this.item.listPrice * delta);
    this.quantity = newQuantity;
    this.total += (this.item.listPrice * delta);
}

function Cart() {
    this.cartItems = {};
    this.numberOfItems = 0;
    this.subTotal = 0;
}

Cart.prototype.addItem = function(item) {
    if (!(item.itemId in this.cartItems)) {
        this.cartItems[item.itemId] = new CartItem(this, item);
        this.numberOfItems++;
    } else {
        var cartItem;
        cartItem = this.cartItems[item.itemId];
        cartItem.updateQuantity(cartItem.quantity + 1);
    }
}

Cart.prototype.removeItem = function(item) {
    if (item.itemId in this.cartItems) {
        this.numberOfItems--;
        var cartItem = this.cartItems[item.itemId];
        delete this.cartItems[item.itemId];
        this.subTotal -= (cartItem.quantity * cartItem.listPrice);
    }
}

function CartForm() {
    this.cart = new Cart();
}

var DROP_SCRIPT = 
    "drop index productCat;" +
    "drop index productName;" +
    "drop index itemProd;" +
    "" +
    "drop table lineitem;" +
    "drop table orderstatus;" +
    "drop table orders;" +
    "drop table bannerdata;" +
    "drop table profile;" +
    "drop table signon;" +
    "drop table inventory;" +
    "drop table item;" +
    "drop table product;" +
    "drop table account;" +
    "drop table category;" +
    "drop table supplier;" +
    "drop table sequence;";

var CREATE_SCRIPT = 
"" +
"create table supplier (" +
"    suppid int not null," +
"    name varchar(80) null," +
"    status varchar(2) not null," +
"    addr1 varchar(80) null," +
"    addr2 varchar(80) null," +
"    city varchar(80) null," +
"    state varchar(80) null," +
"    zip varchar(5) null," +
"    phone varchar(80) null," +
"    constraint pk_supplier primary key (suppid)" +
");" +
"" +
"create table signon (" +
"    username varchar(25) not null," +
"    password varchar(25)  not null," +
"    constraint pk_signon primary key (username)" +
");" +
"" +
"create table account (" +
"    userid varchar(80) not null," +
"    email varchar(80) not null," +
"    firstname varchar(80) not null," +
"    lastname varchar(80) not null," +
"    status varchar(2) null," +
"    addr1 varchar(80) not null," +
"    addr2 varchar(40) null," +
"    city varchar(80) not null," +
"    state varchar(80) not null," +
"    zip varchar(20) not null," +
"    country varchar(20) not null," +
"    phone varchar(80) not null," +
"    constraint pk_account primary key (userid)" +
");" +
"" +
"create table profile (" +
"    userid varchar(80) not null," +
"    langpref varchar(80) not null," +
"    favcategory varchar(30)," +
"    mylistopt bit," +
"    banneropt bit" +
    //",    constraint pk_profile primary key (userid)" +
");" +
"" +
"create table bannerdata (" +
"    favcategory varchar(80) not null," +
"    bannername varchar(255) null" +
    //"    ,constraint pk_bannerdata primary key (favcategory)" +
");" +
"" +
"create table orders (" +
"    orderid int not null," +
"    userid varchar(80) not null," +
"    orderdate date not null," +
"    shipaddr1 varchar(80) not null," +
"    shipaddr2 varchar(80) null," +
"    shipcity varchar(80) not null," +
"    shipstate varchar(80) not null," +
"    shipzip varchar(20) not null," +
"    shipcountry varchar(20) not null," +
"    billaddr1 varchar(80) not null," +
"    billaddr2 varchar(80)  null," +
"    billcity varchar(80) not null," +
"    billstate varchar(80) not null," +
"    billzip varchar(20) not null," +
"    billcountry varchar(20) not null," +
"    courier varchar(80) not null," +
"    totalprice decimal(10,2) not null," +
"    billtofirstname varchar(80) not null," +
"    billtolastname varchar(80) not null," +
"    shiptofirstname varchar(80) not null," +
"    shiptolastname varchar(80) not null," +
"    creditcard varchar(80) not null," +
"    exprdate varchar(7) not null," +
"    cardtype varchar(80) not null," +
"    locale varchar(80) not null" +
    //"    ,constraint pk_orders primary key (orderid)" +
");" +
"" +
"create table orderstatus (" +
"    orderid int not null," +
"    linenum int not null," +
"    timestamp date not null, " +
"    status varchar(2) not null" +
    //"    ,constraint pk_orderstatus primary key (orderid, linenum)" +
");" +
"" +
"create table lineitem (" +
"    orderid int not null," +
"    linenum int not null," +
"    itemid varchar(10) not null," +
"    quantity int not null," +
"    unitprice decimal(10,2) not null" +
    //"    ,constraint pk_lineitem primary key (orderid, linenum)" +
");" +
"" +
"create table category (" +
"    catid varchar(10) not null," +
"    name varchar(80) null," +
"    descn varchar(255) null" +
    //",    constraint pk_category primary key (catid)" +
");" +
"" +
"create table product (" +
"    productid varchar(10) not null," +
"    category varchar(10) not null," +
"    name varchar(80) null," +
"    descn varchar(255) null" +
    //",    constraint pk_product primary key (productid)," +
    //"    constraint fk_product_1 foreign key (category)" +
    //"    references category (catid)" +
");" +
"" +
"create index productCat on product (category);" +
"create index productName on product (name);" +
"" +
"create table item (" +
"    itemid varchar(10) not null," +
"    productid varchar(10) not null," +
"    listprice decimal(10,2) null," +
"    unitcost decimal(10,2) null," +
"    supplier int null," +
"    status varchar(2) null," +
"    attr1 varchar(80) null," +
"    attr2 varchar(80) null," +
"    attr3 varchar(80) null," +
"    attr4 varchar(80) null," +
"    attr5 varchar(80) null" +
    //",    constraint pk_item primary key (itemid)," +
    //"    constraint fk_item_1 foreign key (productid)" +
    //"    references product (productid)," +
    //"    constraint fk_item_2 foreign key (supplier)" +
    //"    references supplier (suppid)" +
");" +
"" +
"create index itemProd on item (productid);" +
"" +
"create table inventory (" +
"    itemid varchar(10) not null," +
"    qty int not null" +
    //",    constraint pk_inventory primary key (itemid)" +
");" +
"" +
"CREATE TABLE sequence" +
"(" +
"    name varchar(30) not null," +
"    nextid int not null" +
    //"    ,constraint pk_sequence primary key (name)" +
");" +
"";

function PetStore(poolId) {
    this.poolId = poolId;
    this.hsql = null;
    this.populate();
}

PetStore.prototype.getConnection = function(id) {
    if (true) {
        // temporary hack to avoid requiring datasource config in cocoon.xconf
        java.lang.Class.forName("org.hsqldb.jdbcDriver");
        var jdbc = java.sql.DriverManager.getConnection("jdbc:hsqldb:.", "sa", "")
        var conn = new Database(jdbc);
        if (this.hsql == null) {
            // keep hsql in-memory database alive
            this.hsql = java.sql.DriverManager.getConnection("jdbc:hsqldb:.", "sa", "");
        }
        return conn;
    } else {
        // lookup datasource in cocoon.xconf
        return Database.getConnection(id);
    }
}

PetStore.prototype.populate = function() {
    var conn = this.getConnection(this.poolId);
    try {
        conn.update(CREATE_SCRIPT);
    } catch (ignored) {
        conn.close();
        return;
    }
    //conn.update("INSERT INTO sequence VALUES('Order', 1);");

    conn.update("INSERT INTO signon VALUES('j2ee','j2ee');");
    conn.update("INSERT INTO signon VALUES('ACID','ACID');");

    conn.update("INSERT INTO account VALUES('j2ee','yourname@yourdomain.com','ABC', 'XYX', 'OK', '901 San Antonio Road', 'MS UCUP02-206', 'Palo Alto', 'CA', '94303', 'USA',  '555-555-5555');");
    conn.update("INSERT INTO account VALUES('ACID','acid@yourdomain.com','ABC', 'XYX', 'OK', '901 San Antonio Road', 'MS UCUP02-206', 'Palo Alto', 'CA', '94303', 'USA',  '555-555-5555');");

    conn.update("INSERT INTO profile VALUES('j2ee','english','DOGS',true,true);");
    conn.update("INSERT INTO profile VALUES('ACID','english','CATS',true,true);");

    conn.update("INSERT INTO bannerdata VALUES ('FISH','<image src=\"images/banner_fish.gif\">');");
    conn.update("INSERT INTO bannerdata VALUES ('CATS','<image src=\"images/banner_cats.gif\">');");
    conn.update("INSERT INTO bannerdata VALUES ('DOGS','<image src=\"images/banner_dogs.gif\">');");
    conn.update("INSERT INTO bannerdata VALUES ('REPTILES','<image src=\"images/banner_reptiles.gif\">');");
    conn.update("INSERT INTO bannerdata VALUES ('BIRDS','<image src=\"images/banner_birds.gif\">');");
    conn.update("INSERT INTO category VALUES ('FISH','Fish','<image src=\"images/fish_icon.gif\"><font size=\"5\" color=\"blue\"> Fish</font>');");
    
    conn.update("INSERT INTO category VALUES ('DOGS','Dogs','<image src=\"images/dogs_icon.gif\"><font size=\"5\" color=\"blue\"> Dogs</font>');");
    conn.update("INSERT INTO category VALUES ('REPTILES','Reptiles','<image src=\"images/reptiles_icon.gif\"><font size=\"5\" color=\"blue\"> Reptiles</font>');");
    conn.update("INSERT INTO category VALUES ('CATS','Cats','<image src=\"images/cats_icon.gif\"><font size=\"5\" color=\"blue\"> Cats</font>');");
    conn.update("INSERT INTO category VALUES ('BIRDS','Birds','<image src=\"images/birds_icon.gif\"><font size=\"5\" color=\"blue\"> Birds</font>');");

    conn.update("INSERT INTO product VALUES ('FI-SW-01','FISH','Angelfish','<image src=\"images/fish1.jpg\">Salt Water fish from Australia');");
    conn.update("INSERT INTO product VALUES ('FI-SW-02','FISH','Tiger Shark','<image src=\"images/fish4.gif\">Salt Water fish from Australia');");
    conn.update("INSERT INTO product VALUES ('FI-FW-01','FISH', 'Koi','<image src=\"images/fish3.gif\">Fresh Water fish from Japan');");
    conn.update("INSERT INTO product VALUES ('FI-FW-02','FISH', 'Goldfish','<image src=\"images/fish2.gif\">Fresh Water fish from China');");
    conn.update("INSERT INTO product VALUES ('K9-BD-01','DOGS','Bulldog','<image src=\"images/dog2.gif\">Friendly dog from England');");
    conn.update("INSERT INTO product VALUES ('K9-PO-02','DOGS','Poodle','<image src=\"images/dog6.gif\">Cute dog from France');");
    conn.update("INSERT INTO product VALUES ('K9-DL-01','DOGS', 'Dalmation','<image src=\"images/dog5.gif\">Great dog for a Fire Station');");
    conn.update("INSERT INTO product VALUES ('K9-RT-01','DOGS', 'Golden Retriever','<image src=\"images/dog1.gif\">Great family dog');");
    conn.update("INSERT INTO product VALUES ('K9-RT-02','DOGS', 'Labrador Retriever','<image src=\"images/dog5.gif\">Great hunting dog');");
    conn.update("INSERT INTO product VALUES ('K9-CW-01','DOGS', 'Chihuahua','<image src=\"images/dog4.gif\">Great companion dog');");
    conn.update("INSERT INTO product VALUES ('RP-SN-01','REPTILES','Rattlesnake','<image src=\"images/lizard3.gif\">Doubles as a watch dog');");
    conn.update("INSERT INTO product VALUES ('RP-LI-02','REPTILES','Iguana','<image src=\"images/lizard2.gif\">Friendly green friend');");
    conn.update("INSERT INTO product VALUES ('FL-DSH-01','CATS','Manx','<image src=\"images/cat3.gif\">Great for reducing mouse populations');");
    conn.update("INSERT INTO product VALUES ('FL-DLH-02','CATS','Persian','<image src=\"images/cat1.gif\">Friendly house cat, doubles as a princess');");
    conn.update("INSERT INTO product VALUES ('AV-CB-01','BIRDS','Amazon Parrot','<image src=\"images/bird4.gif\">Great companion for up to 75 years');");
    conn.update("INSERT INTO product VALUES ('AV-SB-02','BIRDS','Finch','<image src=\"images/bird1.gif\">Great stress reliever');");

    conn.update("INSERT INTO supplier VALUES (1,'XYZ Pets','AC','600 Avon Way','','Los Angeles','CA','94024','212-947-0797');");
    conn.update("INSERT INTO supplier VALUES (2,'ABC Pets','AC','700 Abalone Way','','San Francisco ','CA','94024','415-947-0797');");

    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-1','FI-SW-01',16.50,10.00,1,'P','Large');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-2','FI-SW-01',16.50,10.00,1,'P','Small');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-3','FI-SW-02',18.50,12.00,1,'P','Toothless');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-4','FI-FW-01',18.50,12.00,1,'P','Spotted');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-5','FI-FW-01',18.50,12.00,1,'P','Spotless');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-6','K9-BD-01',18.50,12.00,1,'P','Male Adult');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-7','K9-BD-01',18.50,12.00,1,'P','Female Puppy');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-8','K9-PO-02',18.50,12.00,1,'P','Male Puppy');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-9','K9-DL-01',18.50,12.00,1,'P','Spotless Male Puppy');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-10','K9-DL-01',18.50,12.00,1,'P','Spotted Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-11','RP-SN-01',18.50,12.00,1,'P','Venomless');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-12','RP-SN-01',18.50,12.00,1,'P','Rattleless');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-13','RP-LI-02',18.50,12.00,1,'P','Green Adult');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-14','FL-DSH-01',58.50,12.00,1,'P','Tailless');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-15','FL-DSH-01',23.50,12.00,1,'P','With tail');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-16','FL-DLH-02',93.50,12.00,1,'P','Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-17','FL-DLH-02',93.50,12.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-18','AV-CB-01',193.50,92.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-19','AV-SB-02',15.50, 2.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-20','FI-FW-02',5.50, 2.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-21','FI-FW-02',5.29, 1.00,1,'P','Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-22','K9-RT-02',135.50, 100.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-23','K9-RT-02',145.49, 100.00,1,'P','Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-24','K9-RT-02',255.50, 92.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-25','K9-RT-02',325.29, 90.00,1,'P','Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-26','K9-CW-01',125.50, 92.00,1,'P','Adult Male');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-27','K9-CW-01',155.29, 90.00,1,'P','Adult Female');");
    conn.update("INSERT INTO  item (itemid, productid, listprice, unitcost, supplier, status, attr1) VALUES('EST-28','K9-RT-01',155.29, 90.00,1,'P','Adult Female');");

    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-1',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-2',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-3',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-4',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-5',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-6',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-7',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-8',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-9',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-10',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-11',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-12',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-13',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-14',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-15',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-16',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-17',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-18',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-19',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-20',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-21',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-22',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-23',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-24',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-25',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-26',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-27',10000);");
    conn.update("INSERT INTO inventory (itemid, qty ) VALUES ('EST-28',10000);");
    conn.close();
}

PetStore.prototype.getCategory = function(catId) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from CATEGORY where CATID = '"+catId + "'");
    conn.close();
    return result.rows[0];
}

PetStore.prototype.getCategoryList = function() {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from CATEGORY");
    conn.close();
    return result.rows;
}

PetStore.prototype.getItemListByProduct = function(prodId) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from ITEM where PRODUCTID = ?", [prodId]);
    
    conn.close();
    return result;
}

PetStore.prototype.getItem = function(itemId) {
    print("getItem: " + itemId);
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from ITEM item, INVENTORY inv where item.ITEMID = inv.ITEMID and item.ITEMID = ?", [itemId]);
    conn.close();
    result.rows[0].product = this.getProduct(result.rows[0].productId);
    return result.rows[0];
}

PetStore.prototype.getAccount = function(username, password) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from ACCOUNT, PROFILE, SIGNON, BANNERDATA where ACCOUNT.USERID = ? and SIGNON.USERNAME = ACCOUNT.USERID and SIGNON.PASSWORD = ? and PROFILE.USERID = ACCOUNT.USERID and PROFILE.FAVCATEGORY = BANNERDATA.FAVCATEGORY", [username, password]);
    
    var record = result.rows[0];
    conn.close();
    return record;
}

PetStore.prototype.updateSignon = function(username, newPassword) {
    var conn = this.getConnection(this.poolId);
    conn.update("UPDATE signon SET password = ? WHERE username = ?", [newPassword, username]);
    conn.close();
}

PetStore.prototype.updateAccount = function(model) {
    var conn = this.getConnection(this.poolId);
    conn.update("UPDATE account SET email = ? , firstname = ?, lastname = ?, addr1 = ?, addr2 = ?, city = ?, state = ?, zip = ?, country = ?, phone = ?     where ACCOUNT.USERID = ? ", [model.email, model.firstname, model.lastname, model.addr1, model.addr2, model.city, model.state, model.zip, model.country, model.phone, model.username ]);
    conn.update("UPDATE profile SET langpref = ? , favcategory = ?, mylistopt = ?, banneropt = ? WHERE profile.userid = ?", [model.langpref, model.favcategory, model.mylistopt, model.banneropt, model.username ]);
    conn.close();
}

PetStore.prototype.insertAccount = function(model) {
    var conn = this.getConnection(this.poolId);
    conn.update("INSERT INTO account (userid, email, firstname, lastname, addr1, addr2, city, state, zip, country, phone) VALUES (? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", [model.username, model.email, model.firstname, model.lastname, model.addr1, model.addr2, model.city, model.state, model.zip, model.country, model.phone]);
    conn.update("INSERT INTO profile (userid, langpref, favcategory, mylistopt, banneropt) VALUES (? , ?, ?, ?, ?)", [model.username, model.langpref, model.favcategory, model.mylistopt, model.banneropt]);
    conn.close();
}

PetStore.prototype.testDuplicateLogin = function(username) {
    var conn = this.getConnection(this.poolId);
    var rs = conn.query("select count(*) as ROWCOUNT from signon where username = ?", [username]);
    
    var result = rs.rows[0].ROWCOUNT;
    conn.close();
    return new Number(result);
}

PetStore.prototype.insertNewUser = function(model) {
    var conn = this.getConnection(this.poolId);
    conn.update("INSERT INTO signon (username, password) VALUES (?, ?)", [model.username, model.password]);
    conn.close();
}

PetStore.prototype.insertOrder = function(order, username) {
    var conn = this.getConnection(this.poolId);
    var rs = conn.query("select max(orderid) AS nextOID from orders");
    var currentOID = rs.rows[0].nextOID + 1;
        currentOID = currentOID.toFixed(0);
    conn.update("INSERT INTO orders (orderid, userid, orderdate, shipaddr1, shipaddr2, shipcity, shipstate, shipzip, shipcountry, billaddr1, billaddr2, billcity, billstate, billzip, billcountry, courier, totalprice, billtofirstname, billtolastname, shiptofirstname, shiptolastname, creditcard, exprdate, cardtype, locale) VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", [currentOID, username, order.shipAddress1, order.shipAddress2, order.shipCity, order.shipState, order.shipZip, order.shipCountry, order.billAddress1, order.billAddress2, order.billCity, order.billState, order.billZip, order.billCountry, order.courier, order.totalPrice.toFixed(2), order.billToFirstName, order.billToLastName, order.shipToFirstName, order.shipToLastName, order.creditCard, order.expiryDate, order.cardType, order.locale]);
    conn.update("INSERT INTO orderstatus (orderid, linenum, timestamp, status) VALUES (?, ?, NOW(), 'P')", [currentOID, currentOID]);
    for (var i in order.lineItems) {
        conn.update("INSERT INTO lineitem (orderid, linenum, itemid, quantity, unitprice) VALUES (?, ?, ?, ?, ?)", [currentOID, i, order.lineItems[i].item.ITEMID, order.lineItems[i].quantity.toFixed(0), order.lineItems[i].listPrice.toFixed(2)]);
    }
    conn.close();
    return currentOID;
}

PetStore.prototype.getOrder = function(OID, username) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("SELECT * FROM orders, orderstatus WHERE orders.orderid = ? AND orders.userid = ? AND orders.orderid = orderstatus.orderid", [OID, username]);
    var record = result.rows[0];
    conn.close();
    return record;
}

PetStore.prototype.getOrderList = function(username) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("SELECT orderid, orderdate, totalprice, status FROM orders, orderstatus WHERE orders.orderid = orderstatus.orderid AND orders.userid = ?", [username]);
    conn.close();
    return result.rows;
}

PetStore.prototype.getLineItems = function(OID) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("SELECT *, (quantity * unitprice) AS total FROM lineitem WHERE orderid = ? ORDER BY linenum", [OID]);
    conn.close();
    return result;
}

PetStore.prototype.getProduct = function(key, skipResults, maxResults) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from PRODUCT where PRODUCTID = ?", [key],
                            skipResults, maxResults);
    
    conn.close();
    return result.rows[0];
}

PetStore.prototype.getProductListByCategory = function(key, skipResults, maxResults) {
    var conn = this.getConnection(this.poolId);
    var result = conn.query("select * from PRODUCT where CATEGORY = ?", [key], 
                            skipResults, maxResults);
    conn.close();
    return result;
}

PetStore.prototype.getProductRowCountByCategory = function(key) {
    var conn = this.getConnection(this.poolId);
    var rs = conn.query("select count(*) as ROWCOUNT from PRODUCT where CATEGORY = ?",
                        [key]);
    var result = rs.rows[0].ROWCOUNT;
    conn.close();
    return new Number(result);
}

PetStore.prototype.getItemRowCountByProduct = function(key) {
    var conn = this.getConnection(this.poolId);
    var rs = conn.query("select count(*) as ROWCOUNT from ITEM where PRODUCTID = ?",
                        [key]);
    var result = rs.rows[0].ROWCOUNT;
    conn.close();
    return new Number(result);
}

PetStore.prototype.searchProductList = function(key, skipResults, maxResults) {
    var conn = this.getConnection(this.poolId);
    key = "%" + key + "%";
    var result = conn.query("select * from PRODUCT where lower(name) like ? or lower(category) like ? or lower(descn) like ?", [key, key, key], skipResults, maxResults);
    conn.close();
    return result;
}

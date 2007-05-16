/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
importClass(Packages.org.apache.cocoon.components.flow.ws.WebServiceLoader);

/*
 * Remote service returns an xsd string that is mapped to a Javascript string.
 */
function randomQuoteWebService() {
    var loader = null;
    var msg;
    var value;
    try {
        loader = cocoon.createObject(WebServiceLoader);
        var randomQuoteService = loader.load("http://www.boyzoid.com/comp/randomQuote.cfc?wsdl");
        var quote = randomQuoteService.getQuote(false);
        msg = "Random quote: ";
        value = quote;
    } catch(e) {
        msg = "Error invoking web service: " + e.name + ": " + e.message;
        cocoon.log.error(msg, e);
    } finally {
        cocoon.disposeObject(loader);
    }
    cocoon.sendPage("page/output", {msg:msg, value:value});
}

/*
 * Remote service returns an xsd complex type that is mapped to a Javascript object.
 */
function mortgageIndexWebService() {
    var loader = null;
    var msg;
    var value;
    try {
        loader = cocoon.createObject(WebServiceLoader);
        var mortgageService = loader.load("http://www.webservicex.net/MortgageIndex.asmx?WSDL", "MortgageIndex", "MortgageIndexSoap");
        var monthlyIndex = mortgageService.getCurrentMortgageIndexMonthly();
        msg = "Monthly index date: ";
        value = monthlyIndex.indexDate;
    } catch(e) {
        msg = "Error invoking web service: " + e.name + ": " + e.message;
        cocoon.log.error(msg, e);
    } finally {
        cocoon.disposeObject(loader);
    }
    cocoon.sendPage("page/output", {msg:msg, value:value});
}

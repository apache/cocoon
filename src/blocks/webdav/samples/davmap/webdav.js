function selectMethod() {
    var page = cocoon.parameters["page"];
    var method = cocoon.request.getMethod();
    cocoon.sendPage(method+"/"+page, {});
}

FOM_Cocoon.suicide = new Continuation();

FOM_Cocoon.prototype.sendPageAndWait = function(uri, bizData, fun, ttl) {
    this.sendPage(uri, bizData,
                  new FOM_WebContinuation(new Continuation(), 
                                          this.continuation, ttl));
    if (fun) {
        if (!(fun instanceof Function)) {
            throw "Expected a function instead of: " + fun;
        }
        fun();
    }
    FOM_Cocoon.suicide();
}

FOM_Cocoon.prototype.handleContinuation = function(k, wk) {
    k(wk);
}

FOM_Cocoon.prototype.createWebContinuation = function(ttl) {
   var wk = this.makeWebContinuation(new Continuation(), ttl);
   wk.setBookmark(true);
   return wk;
}




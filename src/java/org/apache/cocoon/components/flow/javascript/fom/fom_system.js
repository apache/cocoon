FOM_Cocoon.suicide = new Continuation();

FOM_Cocoon.prototype.sendPageAndWait = function(uri, bizData, fun) {
    this.sendPage(uri, bizData, new Continuation());
    if (fun instanceof Function) {
      fun();
    }
    FOM_Cocoon.suicide();
}

FOM_Cocoon.prototype.handleContinuation = function(k, wk) {
    k(wk);
}

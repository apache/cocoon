FOM_Cocoon.suicide = new Continuation();

FOM_Cocoon.prototype.sendPageAndWait = function(uri, bizData, fun) {
    this.sendPage(uri, bizData, new Continuation());
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

//
// CVS $Id: Database.js,v 1.1 2003/06/22 18:12:52 coliver Exp $
//
// Prototype Database API
//
// TBD: Move this Database stuff to its own library outside of flow
//

defineClass("org.apache.cocoon.components.flow.javascript.ScriptableConnection");
defineClass("org.apache.cocoon.components.flow.javascript.ScriptableResult");

Database.getConnection = function(selectorValue) {
    var selector = cocoon.componentManager.lookup(Packages.org.apache.avalon.excalibur.datasource.DataSourceComponent.ROLE + "Selector");
    try {
	var ds = selector.select(selectorValue);
	return new Database(ds.getConnection());
    } finally {
	cocoon.componentManager.release(selector);
    }
}


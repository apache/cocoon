// This is a simple flow script
// that can be used as an example for building forms
//
// The script gets the coplet id as a parameter. This id can be used
// as a unique key for the coplet.
//
// The script below doesn't use any continuations. It just checks:
// - if the user already has given some input (which is stored in
//   a session attribute)
// - if the user has just submitted some input
// - if a form should be displayed 
// - The function clear() clears the user input
//
function form() {
    // get the coplet id
    var cid = cocoon.parameters["copletId"];
    var pname = cid + "/myform";

    if ( cocoon.session.getAttribute(pname) == null ) {
        var name = cocoon.request.getParameter("name");
        if ( name == null ) {
            cocoon.sendPage("page/form", {});
        } else {
            cocoon.session.setAttribute(pname, name);
            cocoon.sendPage("page/received", {"name" : name});         
        }
    } else {
        var name = cocoon.session.getAttribute(pname);
        cocoon.sendPage("page/content", {"name" : name});         
    }
}

function clear() {
    // get the coplet id
    var cid = cocoon.parameters["copletId"];
    var pname = cid + "/myform";

    cocoon.session.removeAttribute(pname);
    form();
}

importPackage(Packages.org.apache.excalibur.source);
//importPackage(Packages.java.io);

var resolver = cocoon.getComponent(SourceResolver.ROLE);

function view() {

    var page = cocoon.request.getParameter("page");

    if (page==null)
        page = "index";

    var source = null;

    var text = "";

    if (!exists(page)) {

        cocoon.sendPageAndWait("wiki/new.html", {page:page});

        text = cocoon.request.getParameter("text");

        write(page, text);
    } else {    
        text = read(page);
    }

    cocoon.sendPage("wiki/view.html", {page:page, text:text});
}

function edit() {

    var page = cocoon.request.getParameter("page");

    if (page==null)
        page = "index";

    var text = read(page);

    cocoon.sendPageAndWait("wiki/edit.html", {page:page, text:text});

    var text = cocoon.request.getParameter("text");

    write(page, text);

    cocoon.sendPage("wiki/view.html", {page:page, text:text});
}

function source() {
                                                                                                                                                                               
    var page = cocoon.request.getParameter("page");
                                                                                                                                                                               
    if (page==null)
        page = "index";
                                                                                                                                                                               
    var text = read(page);
                                                                                                                                                                               
    cocoon.sendPage("wiki/source.xml", {page:page, text:text});
}


function read(page) {

    var text = "";
    var source = null;                                                                                                                                       
    try {
        source = resolver.resolveURI("wiki/"+page+".txt");
                                                                                                                                                             
        var input = new Packages.java.io.BufferedReader(new Packages.java.io.InputStreamReader(source.getInputStream()));
        var buffer = new Packages.java.lang.StringBuffer();
        var line;
        while (true) {
            line = input.readLine();
            if (line!=null)
                buffer.append(line+"\n");
            else
                break;
        }
        text = buffer.toString();
        input.close();
                                                                                                                                                             
    } finally {
        if (source != null) {
            resolver.release(source);
        }
    }

    return text;
}

function write(page, text) {

    var source = null;
    try {
        source = resolver.resolveURI("wiki/"+page+".txt");
                                                                                                                                                             
        var out = new Packages.java.io.PrintStream(source.getOutputStream());
        out.print(text);
        out.flush();
        out.close();
                                                                                                                                                             
    } finally {
        if (source != null) {
            resolver.release(source);
        }
    }
}

function exists(page)  {

    var exists = true;
    var source = null;
    try {
        source = resolver.resolveURI("wiki/"+page+".txt");
        exists = source.exists();
    } finally {
        if (source != null) {
            resolver.release(source);
        }
    }
    return exists;
}


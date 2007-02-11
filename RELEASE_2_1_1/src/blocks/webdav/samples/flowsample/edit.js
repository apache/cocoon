function editPage() {
    var page = cocoon.parameters["page"];
    var repoUri = cocoon.parameters["repo"];
    var editPage = "editpage/" + page;
    var docUri = "repo/" + page;
    cocoon.sendPageAndWait(editPage, {});
    writeDoc(repoUri+docUri, "getdata");
    writeDoc(repoUri+docUri+".meta", "getmetadata");
    var dir = docUri.substring(0, docUri.lastIndexOf("/")+1);
    cocoon.sendPage(dir, {});
}

function newPage() {
    var dir = cocoon.parameters["dir"];
    var repoUri = cocoon.parameters["repo"];
    cocoon.sendPageAndWait("newpage", {});
    var page = cocoon.request.getParameter("filename");
    var docUri = dir+page;
    writeDoc(repoUri+docUri, "getdata");
    writeDoc(repoUri+docUri+".meta", "getmetadata");
    cocoon.sendPage(dir, {});
}

function writeDoc(doc, pipeline) {
    var resolver = null;
    var source = null;
    var output = null;
    try {
        resolver = cocoon.getComponent(Packages.org.apache.excalibur.source.SourceResolver.ROLE);
        source = resolver.resolveURI(doc);
        if (source instanceof Packages.org.apache.excalibur.source.ModifiableSource) {
            output = source.getOutputStream();
            cocoon.processPipelineTo(pipeline, {}, output);
            output.close();
        } else {
            cocoon.log.error("Cannot write to " + doc + ": not a modifiable source");
        }
    } catch (error) {
        cocoon.log.error("Error getting output stream: " + error);
    } finally {
        if (source != null) resolver.release(source);
        cocoon.releaseComponent(resolver);
        if (output != null) {
            try {
                output.close();
            } catch (error) {
                cocoon.log.error("Error closing output stream: " + error);
            }
        }
    }
}
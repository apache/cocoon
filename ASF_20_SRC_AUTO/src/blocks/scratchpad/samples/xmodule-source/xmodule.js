
function insert() {
    var documentURI = cocoon.parameters["documentURI"];
    var outputAttributeURI = cocoon.parameters["outputAttributeURI"];

    copy( documentURI, outputAttributeURI );

    cocoon.sendPage( "test-view", {} );
}

function deletePath() {
    var uri = cocoon.parameters["uri"];

    deletePath1( uri );

    cocoon.sendPage( "test-view", {} );
}

function copy( sourceURI, destinationURI ) {
    var resolver = null;
    var source = null;
    var destination = null;
    try {
        resolver = cocoon.getComponent( Packages.org.apache.cocoon.environment.SourceResolver.ROLE );
        source = resolver.resolveURI( sourceURI );
        destination = resolver.resolveURI( destinationURI );
        return Packages.org.apache.excalibur.source.SourceUtil.copy( source, destination );
    } finally {
        if ( source != null )
            resolver.release( source );
        if (destination != null)
            resolver.release( destination );
        cocoon.releaseComponent( resolver );
    }
}

function deletePath1( uri ) {
    var resolver = null;
    var source = null;
    try {
        resolver = cocoon.getComponent( Packages.org.apache.cocoon.environment.SourceResolver.ROLE );
        source = resolver.resolveURI( uri );
        source.deleteTest();
    } finally {
        if ( source != null )
            resolver.release( source );
        cocoon.releaseComponent( resolver );
    }
}

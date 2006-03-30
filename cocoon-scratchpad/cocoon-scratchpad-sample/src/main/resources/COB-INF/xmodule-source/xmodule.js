/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

class MyPipeline extends Pipeline {
  
    void define(String requestPath) {
        if (requestPath =~ ".*\.html") {
            generate "testdata/traxtest-input.xml"
            transform "trax", "testdata/traxtest-style.xsl" 
            serialize "xml"
        }
        else {
            println("No matches for " + requestPath);
        }
    }
}
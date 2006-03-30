What this is about
==================

To manage its documentation, Cocoon uses an instance of the Daisy CMS running at:
http://cocoon.zones.apache.org/daisy/

This directory contains a tool to sync information about sitemap components to
Daisy documents.

Since you normally never use sitemap components by their Java API, but rather
just from the sitemap, it makes more sense to document these components in
Daisy. This makes it easier to manage longer and better formatted documentation,
and doesn't scare non-Java-developers away from Cocoon.

The sync tool takes a few special javadoc-style annotations into account:

@cocoon.sitemap.component.name
   default name with which this component is declared in the sitemap

@cocoon.sitemap.component.documentation.disabled
   excludes the component from the documentation

@cocoon.sitemap.component.documentation
   A short (one-paragraph) description of the component.
   Can contain HTML markup (preferably only inline tags).

@cocoon.sitemap.component.documentation.caching
   A comment about the caching of this component. The cacheability of the
   component is figured out automatially by its implemented interfaces, but
   this tag allows to provide a short comment on the chaching conditions.
   This is mapped to a field in Daisy, thus should not contain HTML markup.

The tool will not update documents unnecessarily, to avoid generating
new document versions in Daisy each time it is run.

Compiling
=========

Execute:

mvn compile


Running
=======

A script is automatically generated in the target directory to launch the tool.

The tool will interactively ask the parameters it needs (Cocoon source tree location,
Daisy host, Daisy username, Daisy password). You need to have the Administrator role
in Daisy in order to run this.

Linux, OS X, and similar:

sh target/sitemaptags_to_daisy.sh

Windows:   --- NOT TESTED YET, DON'T KNOW IF THIS WORKS ---

target/sitemaptags_to_daisy.bat
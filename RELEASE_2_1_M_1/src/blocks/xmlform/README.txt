================================================================================
NOTE: information from this document is being gradually moved into
xdocs/userdocs/concepts/xmlform.xml
================================================================================

<snip moved Ivelin Ivanov Introduction/>

<snip moved Daniel Fagerstrom Background/>


--------------------------------------------------------------------------------

Following are copies of the announcement emails send to the Cocoon development
mailing list.
cocoon-dev@xml.apache.org <cocoon-dev@xml.apache.org>


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

----- Original Message ----- 
From: "Ivelin Ivanov" <ivelin@apache.org>
To: <cocoon-dev@xml.apache.org>
Sent: Tuesday, April 16, 2002 
Subject: [Announcement] Cocoon Form Handling - XML Form Release 0.8


First, I would like to thank everyone who participates in the Form Handling discussion.
I have learned a lot from this discussion in the last few weeks.

There are plenty of great ideas coming from all directions, and some of them influenced my
thinking significantly.

As I have already mention more than once, I have a certain fear that this topic may be too large 
to handle at once and may eventually wind up as it did several times before (Schemox, ExFormular, etc.)
I would very much like this time Cocoon to end up with a better overall form handling solution, than the one that currently exists. It does not have to be perfect from the start.

With all tha said, I am presenting to anyone interested the new incarnation of the xmlform solution.
It has gone through major refactoring based on heavy influence from Torsten and Konstantin.
I will not advertsise what it is this time. I would instead encourage people who are *really* interested
in bettering Cocoon, to look at the demo and provided feedback.
This time there is only one demo, which is an extension of the survey wizard, originally offered by Torsten.

Once you build c2 with scratchpad, point to 
http://localhost:8080/cocoon/mount/xmlform/wizard.html


For those who will take the time to peek in,

I would like to request votes on the following:

1) Does this solution prohibit further extensions in directions that you might be interested?

2) Does this solution offer better overall form handling than the existing one for C2?

3) Can this solution be the base for 2.1?


Thanks everyone,

Ivelin







~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

----- Original Message ----- 
From: "Ivelin Ivanov" <ivelin@iname.com>
To: <cocoon-dev@xml.apache.org>
Cc: "Sam Robertson" <xml_freak@yahoo.com>; "Torsten Curdt" <tcurdt@dff.st>; "Dmitri Plotnikov" <dmitri@plotnix.com>; <stefano@apache.org>; <acoliver@nc.rr.com>; <KPiroumian@flagship.ru>; <cocoond@wyona.org>; <M.Homeijer@devote.nl>
Sent: Thursday, March 14, 2002 3:31 PM
Subject: [Announcement] HTML Form binding and validation arrived

===============================================================
! The HTML Form symmetry loop is closed: !
===============================================================
HTML Forms <-> XPath <-> JavaBeans <-> XML -> Schematron -> HTML Forms
===============================================================

Just released the next version of the symmetric Form binding and validation
toolkit ( a CocoonBlock wannabe :).
In addition to the form-binding it now has integrated Schematron validation
support.

The zip file can be downloaded from:
http://prdownloads.sourceforge.net/freebuilder/CocoonForm_0-6.zip

With this in place one can provide form binding with just a few lines of
code (~5 lines) and Sophisticated form validation with 0! lines of Java
code.

...

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


----- Original Message ----- 
From: "Ivelin Ivanov" <ivelin@iname.com>
To: <cocoon-dev@xml.apache.org>
Cc: "Oliver Becker" <obecker@informatik.hu-berlin.de>; "Rick Jelliffe" <ricko@allette.com.au>; "Dmitri Plotnikov" <dmitri@plotnix.com>
Sent: Sunday, March 24, 2002 7:03 AM
Subject: [Announcement] Fast Schematron Validation Here !

We've got the rainbow !

+===========================================+
+    *Fast* Java API for Schematron Validation *Ready* !      +
+===========================================+
+    Validates both JavaBeans and DOM nodes                       +
+===========================================+

It's been another long and fruitful Saturday here in Austin...

I am grateful to everyone in the Cocoon community as well as Dmitri
Plotnikov for his help with JXPath, Rick Jelliffe for his guidance with
Schematron and Oliver Becker for the clarifications on his XSLT based Java
API for Schematron.


As I mentioned already, I've decided to implement Schematron in Java using
JXPath.
The result is surprisingly little code (~1K lines) and quite exciting speed
(~20ms per validation for the demo setup).

What's more:
   - Does not need Castor mapping
   - Does not do XSL transformation
   - Direct access through the JXPath library

* Someone let me know if this can be implemented too much faster *

I hope this answers some outstanding questions like:
"How do we validate HTML Forms ?"
"How do we reuse validation rules and code for HTML Forms, Web Services and
domain validation ?"
"What language do we use or build to implement validation?"

I am not sure if the credits should all go to Schematron or more to XPath,
but
Schematron being so simple and powerful is clearly my choice:
- Schemas can grow organicly. One can start with a few simple rules and grow
the document with time.
- Native support for validation in "phases". Unlike XML Schema, one doesn't
need to provide a complete document in order to be able to perform
validation.
- Pin-points the bad elements and provides user-friendly reporting.
- Very, very simple to learn if one knows XPath. (I just can stop repeating
that.)

...

~~~~~~~~~~~~~~~~~~~~~

<end-of-file/>

<!-- ===================================================================

     Apache Common Character Entity Sets (Version 1.0)

PURPOSE:
  Common elements across all DTDs.

TYPICAL INVOCATION:

  <!ENTITY % common-charents PUBLIC
      "-//APACHE//ENTITIES Common Character Entity Sets Vx.y//EN"
      "common-charents-vxy.mod">
  %common-charents;

  where

    x := major version
    y := minor version

AUTHORS:
  David Crossley <crossley@apache.org>

FIXME:

CHANGE HISTORY:
[Version 1.0]
  20020613 Initial version. (DC)

COPYRIGHT:
  Copyright (c) 2002 The Apache Software Foundation.

  Permission to copy in any form is granted provided this notice is
  included in all copies. Permission to redistribute is granted
  provided this file is distributed untouched in all its parts and
  included files.

==================================================================== -->

<!-- =============================================================== -->
<!-- Common ISO character entity sets -->
<!-- =============================================================== -->

<!ENTITY % ISOlat1 PUBLIC
    "ISO 8879:1986//ENTITIES Added Latin 1//EN//XML"
    "ISOlat1.pen">
%ISOlat1;

<!ENTITY % ISOpub PUBLIC
    "ISO 8879:1986//ENTITIES Publishing//EN//XML"
    "ISOpub.pen">
%ISOpub;

<!ENTITY % ISOtech PUBLIC
    "ISO 8879:1986//ENTITIES General Technical//EN//XML"
    "ISOtech.pen">
%ISOtech;

<!ENTITY % ISOnum PUBLIC
    "ISO 8879:1986//ENTITIES Numeric and Special Graphic//EN//XML"
    "ISOnum.pen">
%ISOnum;

<!ENTITY % ISOdia PUBLIC
    "ISO 8879:1986//ENTITIES Diacritical Marks//EN//XML"
    "ISOdia.pen">
%ISOdia;

<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->

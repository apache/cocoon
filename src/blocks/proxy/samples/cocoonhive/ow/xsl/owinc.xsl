<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:msxsl="urn:schemas-microsoft-com:xslt"
                xmlns:ow="http://openwiki.com/2001/OW/Wiki"
                extension-element-prefixes="msxsl ow"
                exclude-result-prefixes=""
                version="1.0">

<msxsl:script language="JScript" implements-prefix="ow">
    var longMonths = new Array("January", "February", "March", "April", "May", "June",
                               "July", "August", "September", "October", "November", "December");
    var shortMonths = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    function urlencode(pData) {
        return escape(pData);
    }

    function formatLongDate(pData) {
        var year  = pData.substring(0, 4);
        var month = pData.substring(5, 7);
        var day   = pData.substring(8, 10);
        if (day.charAt(0) == '0') {
            day = day.charAt(1);
        }

        // euro-style:
        // return day + "-" + longMonths[month-1] + "-" + year;

        // us-style:
        return longMonths[month-1] + " " + day + ", " + year;
    }

    function formatShortDate(pData) {
        var year  = pData.substring(0, 4);
        var month = pData.substring(5, 7);
        var day   = pData.substring(8, 10);
        if (day.charAt(0) == '0') {
            day = day.charAt(1);
        }

        // euro-style:
        // return day + "-" + shortMonths[month-1] + "-" + year;

        // us-style:
        // return shortMonths[month-1] + " " + day + ", " + year;
        return shortMonths[month-1] + " " + day;
    }

    function formatTime(pData) {
        // euro-style
        return pData.substring(11, 16);

        // us-style
        // return 3:15 PM
    }

    function formatShortDateTime2(pData) {
        var year  = pData.substring(0, 4);
        var month = pData.substring(5, 7);
        var day   = pData.substring(8, 10);
        return formatShortDate(pData) + ", " + year + " " + formatTime(pData);
        //return day + "/" + month + "/" + year + " " + formatTime(pData);
    }


    function formatLongDateTime(pData) {
        return formatLongDate(pData) + " " + formatTime(pData);
    }

    function formatShortDateTime(pData) {
        return formatShortDate(pData) + ", " + formatTime(pData);
    }
</msxsl:script>

</xsl:stylesheet>
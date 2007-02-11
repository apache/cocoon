The LinkAlarm report for cocoon.apache.org/ is at
http://reports.linkalarm.com/373104199608/

LinkAlarm scans are run after each public release of Cocoon
to detect any issues that need to be addressed prior to the
next release.

The LinkAlarm report gives detailed HTML views of the situation
in an easy-to-read style. However, the summary file that is
explained below has concise info about the actual broken links.

One other LinkAlarm page that is of special interest is the
"mailto:" validation page (those errors are not included in
the summary listing below).

To facilitate the management of link mending by the Cocoon
community, there is a summary file in the HEAD CVS at
 documentation/xdocs/plan/linkalarm-broken.txt
This tab-delimited file has the following format ...

status problem_link referring_page response_code meaning comment

where "status" has these codes ...
 - ... not yet addressed
 F ... fixed
 ? ... has some issue (see the "comment" field)
 [1-3] ... external link broken for n runs (will be dropped soon)

To reduce duplication of effort, please update the "status"
tag for each issue that you have addressed.

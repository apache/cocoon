#!/bin/sh
# checks diffs during relicense process
# see bugzilla 27467

if [[ "$1" != "-q" ]]
then
    cat << EOF
    This is meant to check the output of

        cvs -z3 diff -r  ASF_20_BEFORE .

    To make sure diffs only include license-related stuff.

    See
        http://nagoya.apache.org/bugzilla/show_bug.cgi?id=27467
    for more info

EOF
fi

EXPR="\*.*\*|Copyright|Licensed|ou may|LICENSE-2.0|Unless|distributed|RCS file|Apache Cocoon|THIS SOFTWARE|INCLUDING|FITNESS|APACHE SOFTWARE|INDIRECT|DING,|OF USE|ANY THEORY|This software|on behalf|information on the|Software Foundation|THEORY OF LIAB|WARRANTIES|the License|Redistributions|this list|or other mat|end-user documentation included|following ack|this ack|used to  endorse|prior written|derived from|without prior|Apache Software License|Redistribution and use|are permitted|include *the following|acknowledge?ment may|apache@apache.org|Apache Software  Foundation|and wherever|retrieving revision|@version|\*|^\> *$|^< *$|=============================|---|^Index:|^diff |^[0-9]+\,[0-9]+[a-z].*|[0-9]+[a-z][0-9]+"
egrep -v "$EXPR"

#!/bin/sh
#
# $Id: build-dist.sh,v 1.1 2001/08/11 23:06:11 mdb Exp $
#
# Builds a distribution archive. This should be run from the top-level
# project directory and it will place the distribution archive into the
# directory identified by $DISTDIR.

USAGE="Usage: $0 distname (eg. samskivert-1.4)"
DISTDIR=dist

if [ -z "$1" ]; then
    echo $USAGE
    exit -1
else
    TARGET=$1
fi

# build our excludes file
cat > .excludes <<EOF
bin
CVS
dist
lib/*.jar
code
docs
.excludes
EOF

# create our distribution directory
mkdir /tmp/$TARGET

# temporarily move the distribution files into temp so that we can put
# them into a properly named directory
tar --exclude-from=.excludes -cf - * | tar -C /tmp/$TARGET -xf -

# now build the actual archive file
tar -C /tmp -czf $DISTDIR/$TARGET.tgz $TARGET

# and clean up after ourselves
rm -rf /tmp/$TARGET
rm .excludes

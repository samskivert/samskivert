#!/bin/sh
#
# $Id: install.sh,v 1.2 2002/03/18 00:17:57 mdb Exp $
#
# Builds the Debian packages for the release and installs them in the
# appropriate place on my web server for distribution.

DEBHOST=waywardgeeks.org
DEBDIR=/export/wayward/pages/code/debian

if [ -z "$1" ]; then
    echo "Usage: $0 release_version (e.g. 1.2)"
    exit -1
else
    RELEASE=$1
    shift
fi

TARGET=lookuplet-$RELEASE

# build the distribution
`dirname $0`/build-dist.sh $TARGET

# build our excludes file
cat > .excludes <<EOF
install.sh
CVS
.excludes
.cvsignore
*.tar.gz
*~
EOF

# create our distribution directory
mkdir /tmp/$TARGET

# temporarily move the distribution files into temp so that we can put
# them into a properly named directory
tar --exclude-from=.excludes -cf - * | tar -C /tmp/$TARGET -xf -

# build the debian packages
pushd /tmp/$TARGET
dpkg-buildpackage -tc -rfakeroot

# install the debian packages on the web server
if [ -f ../lookuplet_*.deb ]; then
    # remove any old files
    ssh $DEBHOST "rm $DEBDIR/pool/main/l/lookuplet/lookuplet_*"
    # copy over the new files
    scp ../lookuplet_* $DEBHOST:$DEBDIR/pool/main/l/lookuplet/
    # update the packages file
    ssh $DEBHOST "/export/wayward/bin/update_packages.pl"
    # blow away our local copy
    rm -f ../lookuplet_*
else
    echo "No files to install."
fi

# clean up after ourselves
cd ..
rm -rf $TARGET
popd
rm .excludes

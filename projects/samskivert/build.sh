#!/bin/sh
#
# $Id: build.sh,v 1.1 2001/03/03 21:21:46 mdb Exp $

# complain if JAVA_HOME isn't set
if [ -z "$JAVA_HOME" ]; then
    echo JAVA_HOME must be set to your JVM install directory.
    exit -1
fi

# complain if ANT_HOME isn't set
if [ -z "$ANT_HOME" ]; then
    echo ANT_HOME must be set to your ANT install directory.
    exit -1
fi

# set up our classpath
CP=$ANT_HOME/lib/ant.jar
CP=$CP:$ANT_HOME/../lib/jaxp.jar
CP=$CP:$ANT_HOME/../lib/crimson.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main "$@"

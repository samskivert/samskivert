The samskivert library
======================

The samskivert library (SL) aims to provide useful reusable Java routines that
do things for which I've been unable to find useful reusable implementations on
the net.

Given the emphasis on reusability, SL attempts to closely adhere to the
following principles:

* Each individual module should depend as little as possible on other SL
  modules. Obvious exceptions include modules that are a logical extension of
  other modules and modules that clearly require a service that is implemented
  by another SL module and would have to implement that service themselves in
  the absence of dependence on the other module.

* Modules should be both simple to use and as general purpose as possible. To
  meet these two competing requirements, a balance must be struck at that sweet
  spot where reusability is maximized.

* Code included in SL will freely depend on JDK packages available in the Java
  2 platform and beyond. SL is initially a repository of software useful for
  server-side or stand alone applications and therefore need not make
  compromises to function in the jungle of JVMs in commonly available web
  browsers.

* We are not here to reinvent the wheel, nor to provide a uniform interface to
  every software service under the sun. If something is available in a freely
  redistributable and reusable form from someone else, it won't be found in SL.
  If SL depends on such software from another source, it will provide clear
  documentation on how to get that software and make use of it within the scope
  of SL's particular needs. Again a balance of reusability will be struck here
  and software that is sufficiently difficult to make usable in an arbitrary
  environment will not be used by SL and may be "reinvented".

Building
--------

The library is built using Ant or Maven, pick your poison. If you use ant, you
will need to obtain the jar files enumerated in etc/libs-incl.xml and place
them in the lib/ directory. If you use Maven, the dependencies will be
automatically fetched.

Invoke Ant with any of the following targets:

    all: builds the class files and javadoc documentation
    compile: builds only the class files (dist/classes)
    javadoc: builds only the javadoc documentation (dist/docs)
    tests: builds and runs the unit tests
    dist: builds the distribution jar file (dist/samskivert.jar)

Invoke Maven with any of the following targets:

    mvn test: builds the code and runs the tests
    mvn package: builds the code and creates target/samskivert-X.X.jar
    mvn install: builds and installs samskivert into your local Maven repository

Documentation
-------------

Javadoc documentation is available at
(http://samskivert.github.com/samskivert/apidocs/).

Artifacts
---------

A jar artifact is published to Maven Central, and can be depended upon via:

    com.samskivert:samskivert:1.3

If you prefer to download a jar file, that can be done
[here](http://repo2.maven.org/maven2/com/samskivert/samskivert/).

Distribution
------------

The samskivert library is released under the LGPL. The most recent version of
the library is available [here](http://github.com/samskivert/samskivert/).

Contribution
------------

Contributions to SL are welcome. Fork the library and submit pull requests to
your heart's content. Questions about the library can be directed to the
[OOO-LIBS Google Group](https://groups.google.com/forum/#!forum/ooo-libs).

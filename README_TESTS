Overview
========

Tests are implemented using JUnit. There are classic unit tests that are
located in the ch.usi.dag.disl.test.junit package and complex tests in the
ch.usi.dag.disl.test.suite package that invoke all parts of the framework
(client, server, ...) and verify computed results.

All tests are executed by running

$ ant test


Properties given on the Ant command line starting with "-Ddisl.",
"-Ddislserver.", and "-Ddislreserver." are passed to the test.


To use the tests for debuging purposes during development, a single test
suite (for example, "after") can be executed by running

$ ant test -Dtest.name=after


To save the outputs produced by the test suites, add "-Dtest.verbose=true"
to the Ant command line.

The tests can be also run directly from the command line. To obtain the
command lines used by the test runner to execute the individual JVMs, add
"-Dtest.debug=true" to the Ant command line.


Implementation
==============

Implementation is a bit tricky in few aspects.

Firstly, building of suite test apps and instrumentations is handled by a
scripted target that lists all directories in a "ch.usi.dag.disl.test.suite"
package, and produces "app" and "inst" jars for each suite.

Secondly, when running the instances of client and server using Process API,
never forget to clear environment variables as inherited classpath could cause
serious trouble.

The manifest file in the instrumentation jars for each suite will contain an
attribute, DiSL-Classes, listing binary names of classes that are used for
snippet expansion. The comma-separated list of classes is generated
automatically by the build system based on snippet-related annotations used in
the instrumentation classes.

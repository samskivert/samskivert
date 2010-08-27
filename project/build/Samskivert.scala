import sbt._

class Samskivert (info :ProjectInfo) extends DefaultProject(info)
{
  // interfaces JUnit with SBT, also adds JUnit4 dependency
  val junitInterface = "com.novocode" % "junit-interface" % "0.4" % "test->default"

  // samskivert dependencies
  val servlet = "javax.servlet" % "servlet-api" % "2.5"
  val mail = "javax.mail" % "mail" % "1.4.1"
  val collections = "commons-collections" % "commons-collections" % "3.2.1"
  val digester = "commons-digester" % "commons-digester" % "2.0"
  val log4j = "log4j" % "log4j" % "1.2.15" intransitive() // it depends on JMS, we don't

  // exclude a test that looks like a test, but isn't
  override def testOptions = ExcludeTests("com.samskivert.util.IntSetTestBase" :: Nil) ::
    super.testOptions.toList

  // set the test_dir system property, needed by some of the unit tests
  System.setProperty("test_dir", "target/scala_2.8.0/test-resources")
}

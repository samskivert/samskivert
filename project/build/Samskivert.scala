import sbt._

class Samskivert (info :ProjectInfo) extends DefaultProject(info)
{
  val junitInterface = "com.novocode" % "junit-interface" % "0.4" % "test->default"

  System.setProperty("test_dir", "target/scala_2.8.0/test-resources")
}

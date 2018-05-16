package encrywm.common

import org.scalatest.{Matchers, PropSpec}

import scala.util.Failure

class SourceProcessorSpec extends PropSpec with Matchers {

  property("Script source processing") {
    val s =
      """
        |let a = 10
      """.stripMargin

    val procTry = SourceProcessor.process(s)

    procTry.isSuccess shouldBe true
  }

  property("Schema source processing") {
    val s =
      """
        |schema Person:Object(
        |    name:String;
        |    age:Int;
        |)
      """.stripMargin

    val procTry = SourceProcessor.process(s)

    procTry.isSuccess shouldBe false
  }

  property("Composite source processing") {
    val s =
      """
        |schema PersonBox:Object(
        |    name:String;
        |    age:Int;
        |)
        |
        |#---script---
        |
        |def checkAge(box: Box) -> Bool:
        |   match box:
        |       case personBox -> @PersonBox:
        |           return personBox.body.age > 20
        |       case _:
        |           pass
      """.stripMargin

    val procTry = SourceProcessor.process(s)

    procTry.isSuccess shouldBe true
  }

  property("Composite source processing (Invalid schema)") {
    val s =
      """
        |schema PersonBox:Object(
        |    name:String;
        |    age:Undef;
        |)
        |
        |#---script---
        |
        |def checkAge(box: Box) -> Bool:
        |   match box:
        |       case personBox -> @PersonBox:
        |           return personBox.body.age > 20
        |       case _:
        |           pass
      """.stripMargin

    val procTry = SourceProcessor.process(s)

    procTry.isSuccess shouldBe false

    (procTry match {
      case Failure(_: encrytl.core.Interpreter.UnresolvedRefError) => true
      case _ => false
    }) shouldBe true
  }

  property("Composite source processing (Invalid script)") {
    val s =
      """
        |schema PersonBox:Object(
        |    name:String;
        |    age:Int;
        |)
        |
        |#---script---
        |
        |def checkAge(box: Undef) -> Bool:
        |   match box:
        |       case personBox -> @PersonBox:
        |           return personBox.body.age > 20
        |       case _:
        |           pass
      """.stripMargin

    val procTry = SourceProcessor.process(s)

    procTry.isSuccess shouldBe false

    (procTry match {
      case Failure(_: encrywm.lang.frontend.semantics.error.UnresolvedSymbolError) => true
      case _ => false
    }) shouldBe true
  }
}

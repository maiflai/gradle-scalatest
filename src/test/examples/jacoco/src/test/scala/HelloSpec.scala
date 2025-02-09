import org.scalatest.funsuite.AnyFunSuite

class HelloSpec extends AnyFunSuite {
  test("it should cover") {
    assert(new Hello().say() === "world")
  }
  test("main resource") {
    assert(Option(getClass.getResourceAsStream("/main.txt")).isDefined)
  }
  test("test resource") {
    assert(Option(getClass.getResourceAsStream("/test.txt")).isDefined)
  }
}
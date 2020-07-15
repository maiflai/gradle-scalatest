import org.scalatest.funsuite.AnyFunSuite

class HelloSpec extends AnyFunSuite {
  test("it should cover") {
    assert(new Hello().say() === "world")
  }
}
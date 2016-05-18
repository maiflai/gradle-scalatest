import org.scalatest.FunSuite

class HelloSpec extends FunSuite {
  test("it should cover") {
    assert(new Hello().say() === "world")
  }
}
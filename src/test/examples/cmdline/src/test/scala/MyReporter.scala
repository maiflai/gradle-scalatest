import org.scalatest.Reporter
import org.scalatest.events.Event

class MyReporter extends Reporter {
  def apply(event: Event): Unit = ()
}
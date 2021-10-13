package shopping.cart

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

object ShoppingCartSpec {
  val config = ConfigFactory
    .parseString("""
      akka.actor.serialization-bindings {
        "shopping.cart.CborSerializable" = jackson-cbor
      }
      """)
    .withFallback(EventSourcedBehaviorTestKit.config)
}

class ShoppingCartSpec extends ScalaTestWithActorTestKit(ShoppingCartSpec.config) with AnyWordSpecLike with BeforeAndAfterEach {

  private val cartId = "testCart"
  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[
      ShoppingCart.Command,
      ShoppingCart.Event,
      ShoppingCart.State](system, ShoppingCart(cartId))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  "The Shopping Cart" should {
    "add item" in {
      val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AddItem("foo", 42, replyTo))
      println(commandResult)
      commandResult.reply should === (StatusReply.Success(ShoppingCart.Summary(Map("foo" -> 42), checkedOut = false)))
      commandResult.event should === (ShoppingCart.ItemAdded(cartId, "foo", 42))
    }

    "reject already added item" in {
      val result1 = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.AddItem("foo", 42, _))
      result1.reply.isSuccess should === (true)
      val result2 = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.AddItem("foo", 13, _))
      result2.reply.isError should === (true)
    }

    "remove item that is not in cart" in {
      val commandResult = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.RemoveItem("foo", _))
      commandResult.reply.isError should === (true)
      println(commandResult)
    }

    "add and remove item success" in {
      val result1 = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.AddItem("foo", 42, _))
      result1.reply.isSuccess should === (true)

      val result2 = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.RemoveItem("foo", _))
      result2.reply.isSuccess should === (true)
      result2.reply.getValue should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    }
  }
}

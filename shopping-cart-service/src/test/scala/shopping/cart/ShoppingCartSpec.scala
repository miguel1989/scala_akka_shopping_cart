package shopping.cart

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

object ShoppingCartSpec {
  val config: Config = ConfigFactory
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

  def addItemAndAssertSuccess(itemId: String, count: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AddItem(itemId, count, replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.reply.getValue should === (ShoppingCart.Summary(Map(itemId -> count), checkedOut = false))
    commandResult.event should === (ShoppingCart.ItemAdded(cartId, itemId, count))
  }

  def addItemAndAssertFail(itemId: String, count: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AddItem(itemId, count, replyTo))
    commandResult.reply.isError should === (true)
//    println(commandResult)
  }

  def removeItemAndAssertSuccess(itemId: String): Unit = {
//    val commandResult = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.RemoveItem(itemId, _))
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.RemoveItem(itemId, replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.reply.getValue should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    commandResult.event should === (ShoppingCart.ItemRemoved(cartId, itemId))
  }

  def removeItemAndAssertFail(itemId: String): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.RemoveItem(itemId, replyTo))
    commandResult.reply.isError should === (true)
  }

  "The Shopping Cart" should {
    "add item success" in {
      addItemAndAssertSuccess("medved", 1)
    }

    "reject already added item" in {
      addItemAndAssertSuccess("medved", 1)
      addItemAndAssertFail("medved", 2)
    }

    "add and remove item success" in {
      addItemAndAssertSuccess("medved", 1)
      removeItemAndAssertSuccess("medved")
    }

    "remove item that is not in cart" in {
      removeItemAndAssertFail("medved")
    }
  }
}

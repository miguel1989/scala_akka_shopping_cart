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
      ShoppingCart.State](system, ShoppingCart(cartId, "someTag"))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  def addItemAndAssertSuccess(itemId: String, count: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AddItem(itemId, count, replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.reply.getValue.checkedOut should === (false)
    commandResult.reply.getValue.items.contains(itemId) should === (true)
    commandResult.reply.getValue.items.get(itemId) should === (Some(count))
//    commandResult.reply.getValue should === (ShoppingCart.Summary(Map(itemId -> count), checkedOut = false))
    commandResult.event should === (ShoppingCart.ItemAdded(cartId, itemId, count))
  }

  def addItemAndAssertFail(itemId: String, count: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AddItem(itemId, count, replyTo))
    commandResult.reply.isError should === (true)
//    println(commandResult)
  }

  def removeItemAndAssertSuccess(itemId: String, expectedOldQuantity: Int): Unit = {
//    val commandResult = eventSourcedTestKit.runCommand[StatusReply[ShoppingCart.Summary]](ShoppingCart.RemoveItem(itemId, _))
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.RemoveItem(itemId, replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.reply.getValue should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    commandResult.event should === (ShoppingCart.ItemRemoved(cartId, itemId, expectedOldQuantity))
  }

  def removeItemAndAssertFail(itemId: String): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.RemoveItem(itemId, replyTo))
    commandResult.reply.isError should === (true)
  }

  def getCurrentState(): ShoppingCart.Summary = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.Get(replyTo))
//    println(commandResult)
    commandResult.reply
  }

  def checkOutAndAssetSuccess(): ShoppingCart.Summary = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.Checkout(replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.event.isInstanceOf[ShoppingCart.CheckedOut] should === (true)
    commandResult.event.cartId should  === (cartId)
    commandResult.reply.getValue
  }

  def checkOutAndAssertFail(): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.Checkout(replyTo))
    commandResult.reply.isError should === (true)
  }

  def adjustItemQuantityAndAssertSuccess(itemId: String, count: Int, expectedOldQuantity: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AdjustItemQuantity(itemId, count, replyTo))
    commandResult.reply.isSuccess should === (true)
    commandResult.reply.getValue.checkedOut should === (false)
    commandResult.reply.getValue.items.contains(itemId) should === (true)
    commandResult.reply.getValue.items.get(itemId) should === (Some(count))
    commandResult.event should === (ShoppingCart.ItemQuantityAdjusted(cartId, itemId, count, expectedOldQuantity))
  }

  def adjustItemQuantityAndAssertFail(itemId: String, count: Int): Unit = {
    val commandResult = eventSourcedTestKit.runCommand(replyTo => ShoppingCart.AdjustItemQuantity(itemId, count, replyTo))
    commandResult.reply.isError should === (true)
  }

  "The Shopping Cart" should {
    "add item success" in {
      addItemAndAssertSuccess("medved", 1)

      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map("medved" -> 1), checkedOut = false))
    }

    "add item, adjust quantity and checkout" in {
      addItemAndAssertSuccess("medved", 1)
      addItemAndAssertSuccess("krevedko", 1)

      adjustItemQuantityAndAssertFail("medved", -1)
      adjustItemQuantityAndAssertFail("iphone", 2)
      adjustItemQuantityAndAssertSuccess("medved", 3, 1)

      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map("medved" -> 3, "krevedko" -> 1), checkedOut = false))

      val summary2 = checkOutAndAssetSuccess()
      summary2 should === (ShoppingCart.Summary(Map("medved" -> 3, "krevedko" -> 1), checkedOut = true))

      //can not add/remove/adjust with checked out cart
      addItemAndAssertFail("iphone", 1)
      removeItemAndAssertFail("medved")
      adjustItemQuantityAndAssertFail("medved", 2)
      adjustItemQuantityAndAssertFail("iphone", 2)
      checkOutAndAssertFail()

      val summary3 = getCurrentState()
      summary3 should === (ShoppingCart.Summary(Map("medved" -> 3, "krevedko" -> 1), checkedOut = true))
    }

    "add item fail with negative count" in {
      addItemAndAssertFail("medved", -1)

      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    }

    "reject already added item" in {
      addItemAndAssertSuccess("medved", 1)
      addItemAndAssertFail("medved", 2)

      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map("medved" -> 1), checkedOut = false))
    }

    "add and remove item success" in {
      addItemAndAssertSuccess("medved", 1)
      removeItemAndAssertSuccess("medved", 1)

      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    }

    "remove item that is not in cart" in {
      removeItemAndAssertFail("medved")
    }

    "adjust item quantity on empty cart" in {
      adjustItemQuantityAndAssertFail("medved", 4)
    }

    "checkout on empty cart" in {
      val summary = getCurrentState()
      summary should === (ShoppingCart.Summary(Map.empty, checkedOut = false))

      checkOutAndAssertFail()

      val summary2 = getCurrentState()
      summary2 should === (ShoppingCart.Summary(Map.empty, checkedOut = false))
    }
  }
}

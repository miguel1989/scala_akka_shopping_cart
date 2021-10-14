package shopping.cart

import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}

import java.time.Instant
import scala.concurrent.duration.DurationInt

object ShoppingCart {
  /**
   * This interface defines all the commands (messages) that the ShoppingCart actor supports.
   */
  sealed trait Command extends CborSerializable //{def replyTo: ActorRef[StatusReply[Summary]]}

  /**
   * A command to add an item to the cart.
   *
   * It replies with `StatusReply[Summary]`, which is sent back to the caller when
   * all the events emitted by this command are successfully persisted.
   */
  final case class AddItem(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class RemoveItem(itemId: String, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class Checkout(replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class Get(replyTo: ActorRef[Summary]) extends Command

  final case class AdjustItemQuantity(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  /**
   * Summary of the shopping cart state, used in reply messages.
   */
  final case class Summary(items: Map[String, Int], checkedOut: Boolean) extends CborSerializable

  /**
   * This interface defines all the events that the ShoppingCart supports.
   */
  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class ItemAdded(cartId: String, itemId: String, quantity: Int) extends Event
  final case class ItemRemoved(cartId: String, itemId: String, oldQuantity: Int) extends Event
  final case class CheckedOut(cartId: String, eventTime: Instant) extends Event
  final case class ItemQuantityAdjusted(cartId: String, itemId: String, quantity: Int, oldQuantity: Int) extends Event

  final case class State(items: Map[String, Int], checkoutDate: Option[Instant]) extends CborSerializable {

    def isCheckedOut: Boolean = checkoutDate.isDefined

    def checkout(now: Instant): State = copy(checkoutDate = Some(now)) //TODO why copy?

    def toSummary: Summary = Summary(items, isCheckedOut)

    def hasItem(itemId: String): Boolean = items.contains(itemId)

    def isEmpty: Boolean = items.isEmpty

    def updateItem(itemId: String, quantity: Int): State = {
      quantity match {
        case 0 => copy(items = items - itemId)
        case _ => copy(items = items + (itemId -> quantity))
      }
    }
  }
  //companion object
  object State {
    val empty: State = State(items = Map.empty, checkoutDate = None)
  }

  private def handleCommand(cartId: String, state: State, command: Command): ReplyEffect[Event, State] = {
    if (state.isCheckedOut) {
      handleCommandForCheckedOutShoppingCart(cartId, state, command)
    } else {
      handleCommandForOpenShoppingCart(cartId, state, command)
    }
  }

  private def handleCommandForCheckedOutShoppingCart(cartId: String, state: State, command: Command): ReplyEffect[Event, State] = {
//    Effect.reply(command.replyTo)(StatusReply.Error("Cant perform any action on checked out shopping cart"))
    command match {
      case cmd: Get => {
        Effect.reply(cmd.replyTo)(state.toSummary)
      }
      case cmd: AddItem => {
        Effect.reply(cmd.replyTo)(StatusReply.Error("Cant add item, shopping cart is checked out"))
      }
      case cmd: RemoveItem => {
        Effect.reply(cmd.replyTo)(StatusReply.Error("Cant remove item, shopping cart is checked out"))
      }
      case cmd: Checkout => {
        Effect.reply(cmd.replyTo)(StatusReply.Error("Cant checkout, shopping cart is checked out"))
      }
      case cmd: AdjustItemQuantity => {
        Effect.reply(cmd.replyTo)(StatusReply.Error("Cant adjust item quantity, shopping cart is checked out"))
      }
    }
  }

  private def handleCommandForOpenShoppingCart(cartId: String, state: State, command: Command): ReplyEffect[Event, State] = {
    command match {
      case Get(replyTo) =>
        Effect.reply(replyTo)(state.toSummary)

      case AddItem(itemId, quantity, replyTo) => {
        if (state.hasItem(itemId)) {
          Effect.reply(replyTo)(StatusReply.Error(s"Item '$itemId' was already added to this shopping cart"))
        } else if (quantity <= 0) {
          Effect.reply(replyTo)(StatusReply.Error("Quantity must be greater than zero"))
        } else {
          Effect
            .persist(ItemAdded(cartId, itemId, quantity))
            .thenReply(replyTo) { updatedCartState =>
              StatusReply.Success(updatedCartState.toSummary)
            }
        }
      }

      case RemoveItem(itemId, replyTo) => {
        if (!state.hasItem(itemId)) {
          Effect.reply(replyTo)(StatusReply.Error(s"Item '$itemId' is not in shopping cart"))
        } else {
          Effect.persist(ItemRemoved(cartId, itemId, state.items(itemId))).thenReply(replyTo) { updatedCartState =>
            StatusReply.Success(updatedCartState.toSummary)
          }
        }
      }

      case Checkout(replyTo) => {
        if (state.isEmpty) {
          Effect.reply(replyTo)(StatusReply.Error("Shopping cart is empty, can't checkout"))
        } else {
          Effect.persist(CheckedOut(cartId, Instant.now())).thenReply(replyTo) { updatedCartState =>
            StatusReply.Success(updatedCartState.toSummary)
          }
        }
      }

      case AdjustItemQuantity(itemId, quantity, replyTo) => {
        if (!state.hasItem(itemId)) {
          Effect.reply(replyTo)(StatusReply.Error(s"Item '$itemId' does not exist in the cart"))
        } else if (quantity <= 0) {
          Effect.reply(replyTo)(StatusReply.Error("Quantity must be greater than zero"))
        } else {
          Effect
            .persist(ItemQuantityAdjusted(cartId, itemId, quantity, state.items(itemId)))
            .thenReply(replyTo) { updatedCartState =>
              StatusReply.Success(updatedCartState.toSummary)
            }
        }
      }
    }
  }

  private def handleEvent(state: State, event: Event) = {
    event match {
      case ItemAdded(_, itemId, quantity) => {
        state.updateItem(itemId, quantity)
      }
      case ItemRemoved(_, itemId, _) => {
        state.updateItem(itemId, 0)
      }
      case CheckedOut(_, eventTime) => {
        state.checkout(eventTime)
      }
      case ItemQuantityAdjusted(_, itemId, quantity, _) => {
        state.updateItem(itemId, quantity)
      }
    }
  }

  val EntityKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ShoppingCart")

  val tags: Seq[String] = Vector.tabulate(5)(i => s"carts-$i")
  def init(system: ActorSystem[_]): Unit = {
    val behaviorFactory: EntityContext[Command] => Behavior[Command] = {
      entityContext =>
        val i = math.abs(entityContext.entityId.hashCode % tags.size)
        val selectedTag = tags(i)
        ShoppingCart(entityContext.entityId, selectedTag)
    }
    ClusterSharding(system).init(Entity(EntityKey)(behaviorFactory))
  }

  def apply(cartId: String, projectionTag: String): Behavior[Command] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = PersistenceId(EntityKey.name, cartId),
        emptyState = State.empty,
        commandHandler = (state, command) => handleCommand(cartId, state, command),
        eventHandler = (state, event) => handleEvent(state, event))
      .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }
}

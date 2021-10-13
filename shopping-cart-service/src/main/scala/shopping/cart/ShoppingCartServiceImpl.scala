package shopping.cart
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.grpc.GrpcServiceException
import akka.util.Timeout
import io.grpc.Status
import org.slf4j.LoggerFactory
import shopping.cart.proto.{AddItemRequest, Cart, CheckoutRequest, RemoveItemRequest}

import scala.concurrent.{ExecutionContext, Future, TimeoutException}

class ShoppingCartServiceImpl(system: ActorSystem[_]) extends proto.ShoppingCartService {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit val ec: ExecutionContext = system.executionContext

  implicit private val timeout: Timeout = Timeout.create(
    system.settings.config.getDuration("shopping-cart-service.ask-timeout"))
  private val sharding = ClusterSharding(system)

  override def addItem(in: AddItemRequest): Future[Cart] = {
    logger.info("addItem {} with quantity {} to cart {}", in.itemId, in.quantity, in.cartId)
//    Future.successful(Cart(items = List(Item(in.itemId, in.quantity))))
    val entityRef = sharding.entityRefFor(ShoppingCart.EntityKey, in.cartId)
    val reply: Future[ShoppingCart.Summary] = entityRef.askWithStatus(ShoppingCart.AddItem(in.itemId, in.quantity, _))
    val response = reply.map(cart => toProtoCart(cart))
    convertError(response)
  }

  override def removeItem(in: RemoveItemRequest): Future[Cart] = {
    logger.info("removeItem {} to cart {}", in.itemId, in.cartId)

    val entityRef = sharding.entityRefFor(ShoppingCart.EntityKey, in.cartId)
    val reply: Future[ShoppingCart.Summary] = entityRef.askWithStatus(ShoppingCart.RemoveItem(in.itemId, _))
    val response = reply.map(cart => toProtoCart(cart))
    convertError(response)
  }

  override def checkout(in: CheckoutRequest): Future[Cart] = {
    logger.info("checkout cart {}", in.cartId)

    val entityRef = sharding.entityRefFor(ShoppingCart.EntityKey, in.cartId)
//    val reply: Future[ShoppingCart.Summary] = entityRef.askWithStatus(ShoppingCart.Checkout(_))
    val reply: Future[ShoppingCart.Summary] = entityRef.askWithStatus(ShoppingCart.Checkout)
    val response = reply.map(cart => toProtoCart(cart))
    convertError(response)
  }

  override def getCart(in: proto.GetCartRequest): Future[proto.Cart] = {
    logger.info("getCart {}", in.cartId)
    val entityRef = sharding.entityRefFor(ShoppingCart.EntityKey, in.cartId)
    val response =
      entityRef.ask(ShoppingCart.Get).map { cart =>
        if (cart.items.isEmpty)
          throw new GrpcServiceException(Status.NOT_FOUND.withDescription(s"Cart ${in.cartId} not found"))
        else
          toProtoCart(cart)
      }
    convertError(response)
  }

  private def toProtoCart(cart: ShoppingCart.Summary): proto.Cart = {
    proto.Cart(
      cart.items.iterator.map { case (itemId, quantity) =>
        proto.Item(itemId, quantity)
      }.toSeq,
      cart.checkedOut)
  }

  private def convertError[T](response: Future[T]): Future[T] = {
    response.recoverWith {
      case _: TimeoutException =>
        Future.failed(
          new GrpcServiceException(
            Status.UNAVAILABLE.withDescription("Operation timed out")))
      case exc =>
        Future.failed(
          new GrpcServiceException(
            Status.INVALID_ARGUMENT.withDescription(exc.getMessage)))
    }
  }
}

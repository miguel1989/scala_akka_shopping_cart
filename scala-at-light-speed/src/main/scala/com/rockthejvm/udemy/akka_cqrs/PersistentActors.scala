package com.rockthejvm.udemy.akka_cqrs

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

import java.util.Date

object PersistentActors extends App {

  //COMMANDS
  case class Invoice(recipient: String, date: java.util.Date, amount: Int)

  //EVENTS
  case class InvoiceRecorder(id: Int, recipient: String, date: java.util.Date, amount: Int)
  case class TaxRecord(taxId: String, recordId: Int, date: Date, amount: Int)

  object Accountant {
    def props(taxId: String, taxAuthRef: ActorRef): Props = Props(new Accountant(taxId, taxAuthRef))
  }
  class Accountant(taxId: String, taxAuthRef: ActorRef) extends PersistentActor with ActorLogging {
    var latestInvoiceId = 0
    var latestTaxRecordId = 0
    var totalAmount = 0

    override def receiveRecover: Receive = {
      case InvoiceRecorder(id, _, _, amount) =>
        latestInvoiceId = id
        totalAmount += amount
    }

    override def receiveCommand: Receive = {
      case Invoice(recipient, date, amount) =>
        log.info("received invoice for amount {}", amount)
        persist(TaxRecord(taxId, latestTaxRecordId, date, amount / 3)) { ev =>
          taxAuthRef ! ev
          latestTaxRecordId += 1
        }
        persist(InvoiceRecorder(latestInvoiceId, recipient, date, amount)) { ev =>
          taxAuthRef ! ev
          latestInvoiceId += 1
          totalAmount += amount
          log.info("persisted {} with amount {}", ev.id, ev.amount)
        }
    }

    override def persistenceId: String = "simple-account"
  }
  class TaxAuthority extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("PersistDemo")
  val taxAuth = system.actorOf(Props[TaxAuthority])
  val actor = system.actorOf(Accountant.props("LV-123", taxAuth), "simple")

  for (i <- 1 to 10) {
    actor ! Invoice("medved", new Date(), i * 2)
  }
}

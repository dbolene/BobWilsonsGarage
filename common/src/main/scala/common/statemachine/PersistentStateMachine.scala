package common.statemachine

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

/**
 * Orchestration support for a PersistentActor.
 *
 * @author dbolene
 */
trait PersistentStateMachine extends ActorLogging {

  // extender must be a PersistentActor
  this: PersistentActor =>

  // current position in process orchestration
  var state: PersistentStateMachineState = InitialState

  // constructor established behavior - overridden during state change by changeBehavior
  def receiveCommand = initialStateCommandBehavior orElse extenderDefaultCommandBehavior.getOrElse(noOpCommandBehavior) orElse defaultCommandBehavior

  // establish new state and side-effect on state change
  def changeState(newState: Option[PersistentStateMachineState]): Unit = {
    newState.foreach(ns => {
      state = ns
      changeBehavior(ns.commandBehavior)
      log.info(s"State Transitioned to state: [${ns.name}] id: $persistenceId")
    })
  }

  // swap out receive with new behavior chained to optional extenderDefaultBehavior then to defaultBehavior
  private def changeBehavior(behavior: PartialFunction[Any,Unit]): Unit = {
    context.become(behavior orElse extenderDefaultCommandBehavior.getOrElse(noOpCommandBehavior) orElse defaultCommandBehavior)
  }

  // fall back behavior
  def defaultCommandBehavior: PartialFunction[Any,Unit] = {
    case msg =>
      log.error(s"Received unknown msg: $msg in state: $state")
  }

  // do nothing
  def noOpCommandBehavior: PartialFunction[Any,Unit] = {
    case "should-never-match-anything" =>
      log.warning("doing nothing")
  }

  // initial State
  object InitialState extends PersistentStateMachineState {
    val name = "InitialState"
    def commandBehavior = initialStateCommandBehavior
  }

  // extender must supply initial actor command behavior
  def initialStateCommandBehavior: PartialFunction[Any,Unit]

  // extender may supply its own default actor behavior
  def extenderDefaultCommandBehavior: Option[PartialFunction[Any,Unit]]

  // map from PersistentStateMachineStateRef to PersistentStateMachineState
  def dereferenceState(reference: PersistentStateMachineStateRef): Option[PersistentStateMachineState]

}

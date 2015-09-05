package common.statemachine

import common.util.Logging

/**
 *
 * @author dbolene
 */
trait PersistentStateMachineState {

  def name: String

  def commandBehavior: PartialFunction[Any,Unit]

}

/*
 * A serializable reference to a PersistentStateMachineState.
 */
case class PersistentStateMachineStateRef(@transient state: PersistentStateMachineState) extends Logging {

  val name = state.name

  override def toString = s"StateMachineStateRef($name)"
}


package common.fsm

/**
 * Generic FSM States.
 *
 * @author dbolene
 */
trait State
case object Offline extends State
case object Online extends State

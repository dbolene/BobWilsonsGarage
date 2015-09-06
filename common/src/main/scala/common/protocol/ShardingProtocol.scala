package common.protocol

/**
 * Sharded Entry Interaction Protocol.
 *
 * @author dbolene
 */
object ShardingProtocol {

  case class EntryEnvelope(id: String, payload: Any)

}

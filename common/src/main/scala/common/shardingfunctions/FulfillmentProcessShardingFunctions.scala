package common.shardingfunctions

import akka.contrib.pattern.ShardRegion
import common.protocol.ShardingProtocol.EntryEnvelope

/**
 * Shard identity and mapping functions for FulfillmentProcess.
 *
 * @author dbolene
 */
object FulfillmentProcessShardingFunctions {

  val idExtractor: ShardRegion.IdExtractor = {
    case EntryEnvelope(id, payload) ⇒ (id, payload)
  }

  val shardResolver: ShardRegion.ShardResolver = {
    case EntryEnvelope(id, _) ⇒ id
  }
}

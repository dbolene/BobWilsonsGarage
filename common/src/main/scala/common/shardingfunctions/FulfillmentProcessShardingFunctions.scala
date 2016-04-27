package common.shardingfunctions

import akka.cluster.sharding.ShardRegion
import common.protocol.ShardingProtocol.EntryEnvelope

/**
 * Shard identity and mapping functions for FulfillmentProcess.
 *
 * @author dbolene
 */
object FulfillmentProcessShardingFunctions {

  val idExtractor: ShardRegion.ExtractEntityId = {
    case EntryEnvelope(id, payload) ⇒ (id, payload)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case EntryEnvelope(id, _) ⇒ id
  }
}

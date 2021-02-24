package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.WorkerLayer
import edu.uci.ics.amber.engine.architecture.sendsemantics.datatransferpolicy.{
  DataSendingPolicy,
  OneToOnePolicy
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class OneToOne(from: WorkerLayer, to: WorkerLayer, batchSize: Int)
    extends LinkStrategy(from, to, batchSize) {
  override def getPolicies()
      : Iterable[(ActorVirtualIdentity, DataSendingPolicy, Seq[ActorVirtualIdentity])] = {
    assert(from.isBuilt && to.isBuilt && from.numWorkers == to.numWorkers)
    from.identifiers.indices.map(i =>
      (
        from.identifiers(i),
        new OneToOnePolicy(id, batchSize, Array(to.identifiers(i))),
        Seq(to.identifiers(i))
      )
    )
  }

  override def getMappingFromDownstreamToUpstream
      : Map[ActorVirtualIdentity, Iterable[ActorVirtualIdentity]] = {
    assert(from.isBuilt && to.isBuilt && from.numWorkers == to.numWorkers)
    from.identifiers.indices.map(x => from.identifiers(x) -> Iterable(to.identifiers(x))).toMap
  }
}

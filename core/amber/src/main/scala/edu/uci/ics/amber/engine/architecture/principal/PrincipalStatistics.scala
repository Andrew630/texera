package edu.uci.ics.amber.engine.architecture.principal

import edu.uci.ics.amber.engine.architecture.principal.PrincipalState.PrincipalState
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import edu.uci.ics.amber.engine.common.tuple.ITuple

class PrincipalStateType extends TypeReference[PrincipalState.type]

case class PrincipalStatistics(
    @JsonScalaEnumeration(classOf[PrincipalStateType]) operatorState: PrincipalState,
    aggregatedInputRowCount: Long,
    aggregatedOutputRowCount: Long,
    aggregatedOutputResults: Option[List[ITuple]] // in case of a sink operator
)

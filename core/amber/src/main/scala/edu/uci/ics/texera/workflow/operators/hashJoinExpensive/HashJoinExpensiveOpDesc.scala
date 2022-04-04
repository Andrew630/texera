package edu.uci.ics.texera.workflow.operators.hashJoinExpensive

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.operators.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameOnPort1}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, Schema}
import edu.uci.ics.texera.workflow.operators.hashJoin.HashJoinOpDesc

class HashJoinExpensiveOpDesc[K] extends HashJoinOpDesc[K] {

  override def operatorExecutor: OpExecConfig = {
    opExecConfig = new HashJoinExpensiveOpExecConfig[K](
      this.operatorIdentifier,
      probeAttributeName,
      buildAttributeName
    )
    opExecConfig
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Hash Join Expensive",
      "join two inputs",
      OperatorGroupConstants.JOIN_GROUP,
      inputPorts = List(InputPort("small"), InputPort("large")),
      outputPorts = List(OutputPort())
    )
}

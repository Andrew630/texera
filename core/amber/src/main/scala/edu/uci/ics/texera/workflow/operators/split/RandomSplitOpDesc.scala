package edu.uci.ics.texera.workflow.operators.split

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.operators.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}

import javax.validation.constraints.{Max, Min}
import scala.util.Random

class RandomSplitOpDesc extends OperatorDescriptor {

  @JsonProperty(value = "out1Percentage", required = true)
  @JsonPropertyDescription("percentage of split data on first output")
  @Min(0)
  @Max(100)
  var k: Int = 0

  // Store random seeds for each executor to satisfy the fault tolerance requirement.
  @JsonIgnore
  val seeds: Array[Int] = Array.fill(Constants.currentWorkerNum)(Random.nextInt)

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo): OpExecConfig = {
    new SplitOpExecConfig(operatorIdentifier, this)
  }

  override def operatorInfo: OperatorInfo = {
    OperatorInfo(
      userFriendlyName = "Random Split",
      operatorDescription = "Randomly split data to two subsets",
      operatorGroupName = OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort("out1"), OutputPort("out2"))
    )
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = throw new NotImplementedError()

  override def getOutputSchemas(schemas: Array[Schema]): Array[Schema] = {
    Preconditions.checkArgument(schemas.length == 1)
    Array(schemas(0), schemas(0))
  }
}

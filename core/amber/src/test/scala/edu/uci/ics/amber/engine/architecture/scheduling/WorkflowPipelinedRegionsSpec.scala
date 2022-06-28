package edu.uci.ics.amber.engine.architecture.scheduling

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.common.virtualidentity.{OperatorIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.e2e.TestOperators
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.{
  BreakpointInfo,
  OperatorLink,
  OperatorPort,
  WorkflowCompiler,
  WorkflowInfo
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.mutable

class WorkflowPipelinedRegionsSpec extends AnyFlatSpec with MockFactory {

  def buildWorkflow(
      operators: mutable.MutableList[OperatorDescriptor],
      links: mutable.MutableList[OperatorLink]
  ): Workflow = {
    val context = new WorkflowContext
    context.jobId = "workflow-test"

    val texeraWorkflowCompiler = new WorkflowCompiler(
      WorkflowInfo(operators, links, mutable.MutableList[BreakpointInfo]()),
      context
    )
    texeraWorkflowCompiler.amberWorkflow(WorkflowIdentity("workflow-test"), new OpResultStorage())
  }

  "Pipelined Regions" should "correctly find regions in headerlessCsv->keyword->sink workflow" in {
    val headerlessCsvOpDesc = TestOperators.headerlessSmallCsvScanOpDesc()
    val keywordOpDesc = TestOperators.keywordSearchOpDesc("column-1", "Asia")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      mutable.MutableList[OperatorDescriptor](headerlessCsvOpDesc, keywordOpDesc, sink),
      mutable.MutableList[OperatorLink](
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc.operatorID, 0),
          OperatorPort(keywordOpDesc.operatorID, 0)
        ),
        OperatorLink(OperatorPort(keywordOpDesc.operatorID, 0), OperatorPort(sink.operatorID, 0))
      )
    )

    val pipelinedRegions = new WorkflowPipelinedRegions(workflow)
    assert(pipelinedRegions.idToPipelinedRegions.size == 1)
  }

  "Pipelined Regions" should "correctly find regions in csv->(csv->)->join->sink workflow" in {
    val headerlessCsvOpDesc1 = TestOperators.headerlessSmallCsvScanOpDesc()
    val headerlessCsvOpDesc2 = TestOperators.headerlessSmallCsvScanOpDesc()
    val joinOpDesc = TestOperators.joinOpDesc("column-1", "column-1")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      mutable.MutableList[OperatorDescriptor](
        headerlessCsvOpDesc1,
        headerlessCsvOpDesc2,
        joinOpDesc,
        sink
      ),
      mutable.MutableList[OperatorLink](
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc1.operatorID, 0),
          OperatorPort(joinOpDesc.operatorID, 0)
        ),
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc2.operatorID, 0),
          OperatorPort(joinOpDesc.operatorID, 1)
        ),
        OperatorLink(
          OperatorPort(joinOpDesc.operatorID, 0),
          OperatorPort(sink.operatorID, 0)
        )
      )
    )
    val pipelinedRegions = new WorkflowPipelinedRegions(workflow)
    assert(pipelinedRegions.idToPipelinedRegions.size == 2)

    val buildRegion = pipelinedRegions.idToPipelinedRegions.values
      .find(p =>
        p.getOperators()
          .contains(OperatorIdentity(workflow.getWorkflowId().id, headerlessCsvOpDesc1.operatorID))
      )
      .get
    val probeRegion = pipelinedRegions.idToPipelinedRegions.values
      .find(p =>
        p.getOperators()
          .contains(OperatorIdentity(workflow.getWorkflowId().id, headerlessCsvOpDesc2.operatorID))
      )
      .get
    assert(probeRegion.dependsOn.size == 1)
    assert(probeRegion.dependsOn.contains(buildRegion.getId()))
    assert(buildRegion.blockingDowstreamOperatorsInOtherRegions.size == 1)
    assert(
      buildRegion.blockingDowstreamOperatorsInOtherRegions.contains(
        OperatorIdentity(workflow.getWorkflowId().id, joinOpDesc.operatorID)
      )
    )
  }

  "Pipelined Regions" should "correctly find regions in csv->->filter->join->sink workflow" in {
    val headerlessCsvOpDesc1 = TestOperators.headerlessSmallCsvScanOpDesc()
    val keywordOpDesc = TestOperators.keywordSearchOpDesc("column-1", "Asia")
    val joinOpDesc = TestOperators.joinOpDesc("column-1", "column-1")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      mutable.MutableList[OperatorDescriptor](
        headerlessCsvOpDesc1,
        keywordOpDesc,
        joinOpDesc,
        sink
      ),
      mutable.MutableList[OperatorLink](
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc1.operatorID, 0),
          OperatorPort(joinOpDesc.operatorID, 0)
        ),
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc1.operatorID, 0),
          OperatorPort(keywordOpDesc.operatorID, 0)
        ),
        OperatorLink(
          OperatorPort(keywordOpDesc.operatorID, 0),
          OperatorPort(joinOpDesc.operatorID, 1)
        ),
        OperatorLink(
          OperatorPort(joinOpDesc.operatorID, 0),
          OperatorPort(sink.operatorID, 0)
        )
      )
    )
    val pipelinedRegions = new WorkflowPipelinedRegions(workflow)
    assert(pipelinedRegions.idToPipelinedRegions.size == 1)
  }

}
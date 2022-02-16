package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor

import scala.collection.mutable

case class DBWorkflowToLogicalPlan(workflowContent: String) {
  val operators: mutable.MutableList[OperatorDescriptor] = mutable.MutableList()
  val links: mutable.MutableList[OperatorLink] = mutable.MutableList()
  val breakpoints: mutable.MutableList[BreakpointInfo] = mutable.MutableList()
  val content: String = workflowContent

  def serialize(): Unit = {

  }

  def createLogicalPlan(): Unit = {

  }

  def getWorkflowLogicalPlan(): WorkflowInfo = {
    WorkflowInfo(operators, links, breakpoints)
  }
}

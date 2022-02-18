package edu.uci.ics.texera.workflow.common.workflow

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import edu.uci.ics.texera.Utils.objectMapper
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.workflow.DBWorkflowToLogicalPlan.{getAllEnabledBreakpoints, getAllEnabledLinks, getAllEnabledOperators}

import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.collection.mutable

object DBWorkflowToLogicalPlan {

  def flattenOperatorProperties(operatorNode: JsonNode) : OperatorDescriptor = {
    objectMapper.readValue(objectMapper.writeValueAsString(operatorNode), classOf[OperatorDescriptor])
  }

def getAllEnabledOperators(operators: JsonNode): mutable.MutableList[OperatorDescriptor] = {
  var mappedOperators: mutable.MutableList[OperatorDescriptor] = mutable.MutableList()
  // filter out disabled
  var disabledFlag: Boolean = false
  // then insert its id, type, and map all other properties (in the mapping, if value of something is null then make it NONE)
operators.foreach(op => {
  disabledFlag = false
  if(op.has("isDisabled")) {
    disabledFlag = op.get("isDisabled").asBoolean()
  }
  if(! disabledFlag) {
    mappedOperators += flattenOperatorProperties(op)
  }
  })
  mappedOperators

}

  def getAllEnabledLinks(links: JsonNode): mutable.MutableList[OperatorLink] = {
    mutable.MutableList()
  }

  def getAllEnabledBreakpoints(breakpoints: JsonNode): mutable.MutableList[BreakpointInfo] = {
    mutable.MutableList()
  }
}

case class DBWorkflowToLogicalPlan(workflowContent: String) {
  var operators: mutable.MutableList[OperatorDescriptor] = mutable.MutableList()
  var links: mutable.MutableList[OperatorLink] = mutable.MutableList()
  var breakpoints: mutable.MutableList[BreakpointInfo] = mutable.MutableList()
  val content: String = workflowContent

  def serialize(): Unit = {

  }

  def createLogicalPlan(): Unit = {
    // parse the json tree
    val jsonMapper = new ObjectMapper()
    val jsonTree = jsonMapper.readTree(content)
    val operators = jsonTree.get("operators")
    this.operators = getAllEnabledOperators(operators)
    val links = jsonTree.get("links")
    this.links = getAllEnabledLinks(links)
    val breakpoints = jsonTree.get("breakpoints")
    this.breakpoints = getAllEnabledBreakpoints(breakpoints)

  }

  def getWorkflowLogicalPlan(): WorkflowInfo = {
    WorkflowInfo(operators, links, breakpoints)
  }
}

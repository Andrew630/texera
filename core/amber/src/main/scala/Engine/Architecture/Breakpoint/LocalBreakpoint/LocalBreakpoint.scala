package Engine.Architecture.Breakpoint.LocalBreakpoint

import Engine.Common.tuple.Tuple

abstract class LocalBreakpoint(val id: String, val version: Long) extends Serializable {

  def accept(tuple: Tuple)

  def isTriggered: Boolean

  def needUserFix: Boolean = isTriggered

  var isReported = false

  var triggeredTuple: Tuple = _

  def isDirty: Boolean

  var isInput = false

  var triggeredTupleId: Long = -1

  def reset(): Unit = {
    isReported = false
    triggeredTuple = null
    isInput = false
    triggeredTupleId = -1
  }
}
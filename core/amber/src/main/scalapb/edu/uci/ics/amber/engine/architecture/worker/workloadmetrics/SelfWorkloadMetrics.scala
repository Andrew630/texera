// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.workloadmetrics

/** TODO: change the names to remove "self"
  *
  * @param unprocessedDataInputQueueSize
  *   TODO: change the names to remove "queue_size" which is implicit
  */
@SerialVersionUID(0L)
final case class SelfWorkloadMetrics(
    unprocessedDataInputQueueSize: _root_.scala.Long,
    unprocessedControlInputQueueSize: _root_.scala.Long,
    stashedDataInputQueueSize: _root_.scala.Long,
    stashedControlInputQueueSize: _root_.scala.Long
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[SelfWorkloadMetrics] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = unprocessedDataInputQueueSize
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(1, __value)
        }
      };
      
      {
        val __value = unprocessedControlInputQueueSize
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(2, __value)
        }
      };
      
      {
        val __value = stashedDataInputQueueSize
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(3, __value)
        }
      };
      
      {
        val __value = stashedControlInputQueueSize
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(4, __value)
        }
      };
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = unprocessedDataInputQueueSize
        if (__v != 0L) {
          _output__.writeInt64(1, __v)
        }
      };
      {
        val __v = unprocessedControlInputQueueSize
        if (__v != 0L) {
          _output__.writeInt64(2, __v)
        }
      };
      {
        val __v = stashedDataInputQueueSize
        if (__v != 0L) {
          _output__.writeInt64(3, __v)
        }
      };
      {
        val __v = stashedControlInputQueueSize
        if (__v != 0L) {
          _output__.writeInt64(4, __v)
        }
      };
    }
    def withUnprocessedDataInputQueueSize(__v: _root_.scala.Long): SelfWorkloadMetrics = copy(unprocessedDataInputQueueSize = __v)
    def withUnprocessedControlInputQueueSize(__v: _root_.scala.Long): SelfWorkloadMetrics = copy(unprocessedControlInputQueueSize = __v)
    def withStashedDataInputQueueSize(__v: _root_.scala.Long): SelfWorkloadMetrics = copy(stashedDataInputQueueSize = __v)
    def withStashedControlInputQueueSize(__v: _root_.scala.Long): SelfWorkloadMetrics = copy(stashedControlInputQueueSize = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = unprocessedDataInputQueueSize
          if (__t != 0L) __t else null
        }
        case 2 => {
          val __t = unprocessedControlInputQueueSize
          if (__t != 0L) __t else null
        }
        case 3 => {
          val __t = stashedDataInputQueueSize
          if (__t != 0L) __t else null
        }
        case 4 => {
          val __t = stashedControlInputQueueSize
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PLong(unprocessedDataInputQueueSize)
        case 2 => _root_.scalapb.descriptors.PLong(unprocessedControlInputQueueSize)
        case 3 => _root_.scalapb.descriptors.PLong(stashedDataInputQueueSize)
        case 4 => _root_.scalapb.descriptors.PLong(stashedControlInputQueueSize)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.amber.engine.architecture.worker.SelfWorkloadMetrics])
}

object SelfWorkloadMetrics extends scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics = {
    var __unprocessedDataInputQueueSize: _root_.scala.Long = 0L
    var __unprocessedControlInputQueueSize: _root_.scala.Long = 0L
    var __stashedDataInputQueueSize: _root_.scala.Long = 0L
    var __stashedControlInputQueueSize: _root_.scala.Long = 0L
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __unprocessedDataInputQueueSize = _input__.readInt64()
        case 16 =>
          __unprocessedControlInputQueueSize = _input__.readInt64()
        case 24 =>
          __stashedDataInputQueueSize = _input__.readInt64()
        case 32 =>
          __stashedControlInputQueueSize = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics(
        unprocessedDataInputQueueSize = __unprocessedDataInputQueueSize,
        unprocessedControlInputQueueSize = __unprocessedControlInputQueueSize,
        stashedDataInputQueueSize = __stashedDataInputQueueSize,
        stashedControlInputQueueSize = __stashedControlInputQueueSize
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics(
        unprocessedDataInputQueueSize = __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        unprocessedControlInputQueueSize = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        stashedDataInputQueueSize = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L),
        stashedControlInputQueueSize = __fieldsMap.get(scalaDescriptor.findFieldByNumber(4).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkloadmetricsProto.javaDescriptor.getMessageTypes().get(0)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkloadmetricsProto.scalaDescriptor.messages(0)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics(
    unprocessedDataInputQueueSize = 0L,
    unprocessedControlInputQueueSize = 0L,
    stashedDataInputQueueSize = 0L,
    stashedControlInputQueueSize = 0L
  )
  implicit class SelfWorkloadMetricsLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics](_l) {
    def unprocessedDataInputQueueSize: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.unprocessedDataInputQueueSize)((c_, f_) => c_.copy(unprocessedDataInputQueueSize = f_))
    def unprocessedControlInputQueueSize: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.unprocessedControlInputQueueSize)((c_, f_) => c_.copy(unprocessedControlInputQueueSize = f_))
    def stashedDataInputQueueSize: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.stashedDataInputQueueSize)((c_, f_) => c_.copy(stashedDataInputQueueSize = f_))
    def stashedControlInputQueueSize: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.stashedControlInputQueueSize)((c_, f_) => c_.copy(stashedControlInputQueueSize = f_))
  }
  final val UNPROCESSED_DATA_INPUT_QUEUE_SIZE_FIELD_NUMBER = 1
  final val UNPROCESSED_CONTROL_INPUT_QUEUE_SIZE_FIELD_NUMBER = 2
  final val STASHED_DATA_INPUT_QUEUE_SIZE_FIELD_NUMBER = 3
  final val STASHED_CONTROL_INPUT_QUEUE_SIZE_FIELD_NUMBER = 4
  def of(
    unprocessedDataInputQueueSize: _root_.scala.Long,
    unprocessedControlInputQueueSize: _root_.scala.Long,
    stashedDataInputQueueSize: _root_.scala.Long,
    stashedControlInputQueueSize: _root_.scala.Long
  ): _root_.edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics = _root_.edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.SelfWorkloadMetrics(
    unprocessedDataInputQueueSize,
    unprocessedControlInputQueueSize,
    stashedDataInputQueueSize,
    stashedControlInputQueueSize
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.amber.engine.architecture.worker.SelfWorkloadMetrics])
}

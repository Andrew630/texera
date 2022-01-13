// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlreturns

object ControlreturnsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.statistics.StatisticsProto,
    edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.WorkloadmetricsProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.CurrentInputTupleInfo,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlException,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.TypedValue,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.EvaluatedValue,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlReturnV2
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkFlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9scmV0dXJucy5wcm90bxIsZWR1L
  nVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIaPWVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY
  3R1cmUvd29ya2VyL3N0YXRpc3RpY3MucHJvdG8aQmVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvd29ya2VyL
  3dvcmtsb2FkbWV0cmljcy5wcm90bxoVc2NhbGFwYi9zY2FsYXBiLnByb3RvIhcKFUN1cnJlbnRJbnB1dFR1cGxlSW5mbyIuChBDb
  250cm9sRXhjZXB0aW9uEhoKA21zZxgBIAEoCUII4j8FEgNtc2dSA21zZyL1AQoKVHlwZWRWYWx1ZRIvCgpleHByZXNzaW9uGAEgA
  SgJQg/iPwwSCmV4cHJlc3Npb25SCmV4cHJlc3Npb24SKgoJdmFsdWVfcmVmGAIgASgJQg3iPwoSCHZhbHVlUmVmUgh2YWx1ZVJlZ
  hIqCgl2YWx1ZV9zdHIYAyABKAlCDeI/ChIIdmFsdWVTdHJSCHZhbHVlU3RyEi0KCnZhbHVlX3R5cGUYBCABKAlCDuI/CxIJdmFsd
  WVUeXBlUgl2YWx1ZVR5cGUSLwoKZXhwYW5kYWJsZRgFIAEoCEIP4j8MEgpleHBhbmRhYmxlUgpleHBhbmRhYmxlItcBCg5FdmFsd
  WF0ZWRWYWx1ZRJaCgV2YWx1ZRgBIAEoCzI4LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlR5c
  GVkVmFsdWVCCuI/BxIFdmFsdWVSBXZhbHVlEmkKCmF0dHJpYnV0ZXMYAiADKAsyOC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY
  XJjaGl0ZWN0dXJlLndvcmtlci5UeXBlZFZhbHVlQg/iPwwSCmF0dHJpYnV0ZXNSCmF0dHJpYnV0ZXMiyQYKD0NvbnRyb2xSZXR1c
  m5WMhKEAQoRY29udHJvbF9leGNlcHRpb24YASABKAsyPi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvc
  mtlci5Db250cm9sRXhjZXB0aW9uQhXiPxISEGNvbnRyb2xFeGNlcHRpb25IAFIQY29udHJvbEV4Y2VwdGlvbhKEAQoRd29ya2VyX
  3N0YXRpc3RpY3MYAiABKAsyPi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJTdGF0a
  XN0aWNzQhXiPxISEHdvcmtlclN0YXRpc3RpY3NIAFIQd29ya2VyU3RhdGlzdGljcxJwCgx3b3JrZXJfc3RhdGUYAyABKA4yOS5lZ
  HUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJTdGF0ZUIQ4j8NEgt3b3JrZXJTdGF0ZUgAU
  gt3b3JrZXJTdGF0ZRKaAQoYY3VycmVudF9pbnB1dF90dXBsZV9pbmZvGAQgASgLMkMuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lL
  mFyY2hpdGVjdHVyZS53b3JrZXIuQ3VycmVudElucHV0VHVwbGVJbmZvQhriPxcSFWN1cnJlbnRJbnB1dFR1cGxlSW5mb0gAUhVjd
  XJyZW50SW5wdXRUdXBsZUluZm8SfAoPZXZhbHVhdGVkX3ZhbHVlGAUgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY
  2hpdGVjdHVyZS53b3JrZXIuRXZhbHVhdGVkVmFsdWVCE+I/EBIOZXZhbHVhdGVkVmFsdWVIAFIOZXZhbHVhdGVkVmFsdWUSkQEKF
  XNlbGZfd29ya2xvYWRfbWV0cmljcxgGIAEoCzJBLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyL
  lNlbGZXb3JrbG9hZE1ldHJpY3NCGOI/FRITc2VsZldvcmtsb2FkTWV0cmljc0gAUhNzZWxmV29ya2xvYWRNZXRyaWNzQgcKBXZhb
  HVlQgniPwZIAFgAeAFiBnByb3RvMw=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.worker.statistics.StatisticsProto.javaDescriptor,
      edu.uci.ics.amber.engine.architecture.worker.workloadmetrics.WorkloadmetricsProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}
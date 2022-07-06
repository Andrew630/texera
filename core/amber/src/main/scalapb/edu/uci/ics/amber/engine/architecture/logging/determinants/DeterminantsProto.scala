// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.logging.determinants

object DeterminantsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    scalapb.options.ScalapbProto,
    edu.uci.ics.amber.engine.common.ambermessage.AmbermessageProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.logging.determinants.DeterminantMessage,
      edu.uci.ics.amber.engine.architecture.logging.determinants.DataOrderDeterminant,
      edu.uci.ics.amber.engine.architecture.logging.determinants.ControlDeterminant,
      edu.uci.ics.amber.engine.architecture.logging.determinants.TimeStampDeterminant
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkBlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL2xvZ2dpbmcvZGV0ZXJtaW5hbnRzLnByb3RvEi1lZHUud
  WNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLmxvZ2dpbmcaFXNjYWxhcGIvc2NhbGFwYi5wcm90bxoyZWR1L3VjaS9pY
  3MvYW1iZXIvZW5naW5lL2NvbW1vbi9hbWJlcm1lc3NhZ2UucHJvdG8aNWVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9jb21tb24vd
  mlydHVhbGlkZW50aXR5LnByb3RvIv8BCgtEZXRlcm1pbmFudBJzCglkYXRhT3JkZXIYASABKAsyQy5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLmxvZ2dpbmcuRGF0YU9yZGVyRGV0ZXJtaW5hbnRCDuI/CxIJZGF0YU9yZGVySABSCWRhdGFPc
  mRlchJrCgdjb250cm9sGAIgASgLMkEuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5sb2dnaW5nLkNvbnRyb
  2xEZXRlcm1pbmFudEIM4j8JEgdjb250cm9sSABSB2NvbnRyb2xCDgoMc2VhbGVkX3ZhbHVlIpcBChREYXRhT3JkZXJEZXRlcm1pb
  mFudBJdCgZzZW5kZXIYASABKAsyNS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkFjdG9yVmlydHVhbElkZW50aXR5Q
  g7iPwsSBnNlbmRlcvABAVIGc2VuZGVyEiAKBWNvdW50GAIgASgDQgriPwcSBWNvdW50UgVjb3VudCLLAQoSQ29udHJvbERldGVyb
  WluYW50ElwKB3BheWxvYWQYASABKAsyMS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkNvbnRyb2xQYXlsb2FkVjJCD
  +I/DBIHcGF5bG9hZPABAVIHcGF5bG9hZBJXCgRmcm9tGAIgASgLMjUuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5BY
  3RvclZpcnR1YWxJZGVudGl0eUIM4j8JEgRmcm9t8AEBUgRmcm9tIjUKFFRpbWVTdGFtcERldGVybWluYW50Eh0KBHRpbWUYASABK
  ANCCeI/BhIEdGltZVIEdGltZUIJ4j8GSABYAHgBYgZwcm90bzM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      scalapb.options.ScalapbProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.ambermessage.AmbermessageProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}
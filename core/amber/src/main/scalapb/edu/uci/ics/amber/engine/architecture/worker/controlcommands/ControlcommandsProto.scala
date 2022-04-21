// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

object ControlcommandsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.StartWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PauseWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ResumeWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.OpenOperatorV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.UpdateInputLinkingV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.AddPartitioningV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.WorkerExecutionCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryStatisticsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryCurrentInputTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LocalOperatorExceptionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.InitializeOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ModifyOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ReplayCurrentTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonPrintV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.EvaluateExpressionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QuerySelfWorkloadMetricsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.InitializePortMappingV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlCommandV2Message
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkJlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9sY29tbWFuZHMucHJvdG8SLGVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyGkdlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0Z
  WN0dXJlL3NlbmRzZW1hbnRpY3MvcGFydGl0aW9uaW5ncy5wcm90bxo1ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi92a
  XJ0dWFsaWRlbnRpdHkucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90byIPCg1TdGFydFdvcmtlclYyIg8KDVBhdXNlV29ya2VyV
  jIiEAoOUmVzdW1lV29ya2VyVjIiEAoOT3Blbk9wZXJhdG9yVjIi4gEKFFVwZGF0ZUlucHV0TGlua2luZ1YyEmkKCmlkZW50aWZpZ
  XIYASABKAsyNS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkFjdG9yVmlydHVhbElkZW50aXR5QhLiPw8SCmlkZW50a
  WZpZXLwAQFSCmlkZW50aWZpZXISXwoKaW5wdXRfbGluaxgCIAEoCzItLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uT
  Glua0lkZW50aXR5QhHiPw4SCWlucHV0TGlua/ABAVIJaW5wdXRMaW5rIt4BChFBZGRQYXJ0aXRpb25pbmdWMhJMCgN0YWcYASABK
  AsyLS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkxpbmtJZGVudGl0eUIL4j8IEgN0YWfwAQFSA3RhZxJ7CgxwYXJ0a
  XRpb25pbmcYAiABKAsyQS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnNlbmRzZW1hbnRpY3MuUGFydGl0a
  W9uaW5nQhTiPxESDHBhcnRpdGlvbmluZ/ABAVIMcGFydGl0aW9uaW5nIhwKGldvcmtlckV4ZWN1dGlvbkNvbXBsZXRlZFYyIhMKE
  VF1ZXJ5U3RhdGlzdGljc1YyIhoKGFF1ZXJ5Q3VycmVudElucHV0VHVwbGVWMiJCChhMb2NhbE9wZXJhdG9yRXhjZXB0aW9uVjISJ
  goHbWVzc2FnZRgBIAEoCUIM4j8JEgdtZXNzYWdlUgdtZXNzYWdlIoYEChlJbml0aWFsaXplT3BlcmF0b3JMb2dpY1YyEh0KBGNvZ
  GUYASABKAlCCeI/BhIEY29kZVIEY29kZRIqCglpc19zb3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlEokBCg5vd
  XRwdXRfc2NoZW1hcxgDIAMoCzJOLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkluaXRpYWxpe
  mVPcGVyYXRvckxvZ2ljVjIuU2NoZW1hQhLiPw8SDW91dHB1dFNjaGVtYXNSDW91dHB1dFNjaGVtYXMagQEKCUF0dHJpYnV0ZRI5C
  g5hdHRyaWJ1dGVfbmFtZRgBIAEoCUIS4j8PEg1hdHRyaWJ1dGVOYW1lUg1hdHRyaWJ1dGVOYW1lEjkKDmF0dHJpYnV0ZV90eXBlG
  AIgASgJQhLiPw8SDWF0dHJpYnV0ZVR5cGVSDWF0dHJpYnV0ZVR5cGUajQEKBlNjaGVtYRKCAQoKYXR0cmlidXRlcxgBIAMoCzJRL
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkluaXRpYWxpemVPcGVyYXRvckxvZ2ljVjIuQXR0c
  mlidXRlQg/iPwwSCmF0dHJpYnV0ZXNSCmF0dHJpYnV0ZXMiYgoVTW9kaWZ5T3BlcmF0b3JMb2dpY1YyEh0KBGNvZGUYASABKAlCC
  eI/BhIEY29kZVIEY29kZRIqCglpc19zb3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlIhYKFFJlcGxheUN1cnJlb
  nRUdXBsZVYyIjcKDVB5dGhvblByaW50VjISJgoHbWVzc2FnZRgBIAEoCUIM4j8JEgdtZXNzYWdlUgdtZXNzYWdlIkcKFEV2YWx1Y
  XRlRXhwcmVzc2lvblYyEi8KCmV4cHJlc3Npb24YASABKAlCD+I/DBIKZXhwcmVzc2lvblIKZXhwcmVzc2lvbiIcChpRdWVyeVNlb
  GZXb3JrbG9hZE1ldHJpY3NWMiJpCg9MaW5rQ29tcGxldGVkVjISVgoHbGlua19pZBgBIAEoCzItLmVkdS51Y2kuaWNzLmFtYmVyL
  mVuZ2luZS5jb21tb24uTGlua0lkZW50aXR5Qg7iPwsSBmxpbmtJZPABAVIGbGlua0lkIuUFChdJbml0aWFsaXplUG9ydE1hcHBpb
  mdWMhKnAQoYaW5wdXRfdG9fb3JkaW5hbF9tYXBwaW5nGAEgAygLMlIuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuSW5pdGlhbGl6ZVBvcnRNYXBwaW5nVjIuUG9ydEluZm9QYWlyQhriPxcSFWlucHV0VG9PcmRpbmFsTWFwcGluZ
  1IVaW5wdXRUb09yZGluYWxNYXBwaW5nEqoBChlvdXRwdXRfdG9fb3JkaW5hbF9tYXBwaW5nGAIgAygLMlIuZWR1LnVjaS5pY3MuY
  W1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuSW5pdGlhbGl6ZVBvcnRNYXBwaW5nVjIuUG9ydEluZm9QYWlyQhviPxgSF
  m91dHB1dFRvT3JkaW5hbE1hcHBpbmdSFm91dHB1dFRvT3JkaW5hbE1hcHBpbmca8gIKDFBvcnRJbmZvUGFpchJoCg1saW5rX2lkZ
  W50aXR5GAEgASgLMi0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5MaW5rSWRlbnRpdHlCFOI/ERIMbGlua0lkZW50a
  XR58AEBUgxsaW5rSWRlbnRpdHkSigEKCXBvcnRfaW5mbxgCIAEoCzJbLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY
  3R1cmUud29ya2VyLkluaXRpYWxpemVQb3J0TWFwcGluZ1YyLlBvcnRJbmZvUGFpci5Qb3J0SW5mb0IQ4j8NEghwb3J0SW5mb/ABA
  VIIcG9ydEluZm8aawoIUG9ydEluZm8SMwoMcG9ydF9vcmRpbmFsGAEgASgFQhDiPw0SC3BvcnRPcmRpbmFsUgtwb3J0T3JkaW5hb
  BIqCglwb3J0X25hbWUYAiABKAlCDeI/ChIIcG9ydE5hbWVSCHBvcnROYW1lIp0UChBDb250cm9sQ29tbWFuZFYyEnIKDHN0YXJ0X
  3dvcmtlchgBIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlN0YXJ0V29ya2VyVjJCE
  OI/DRILc3RhcnRXb3JrZXJIAFILc3RhcnRXb3JrZXIScgoMcGF1c2Vfd29ya2VyGAIgASgLMjsuZWR1LnVjaS5pY3MuYW1iZXIuZ
  W5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUGF1c2VXb3JrZXJWMkIQ4j8NEgtwYXVzZVdvcmtlckgAUgtwYXVzZVdvcmtlchJ2C
  g1yZXN1bWVfd29ya2VyGAMgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUmVzdW1lV
  29ya2VyVjJCEeI/DhIMcmVzdW1lV29ya2VySABSDHJlc3VtZVdvcmtlchKCAQoQYWRkX3BhcnRpdGlvbmluZxgEIAEoCzI/LmVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkFkZFBhcnRpdGlvbmluZ1YyQhTiPxESD2FkZFBhcnRpd
  GlvbmluZ0gAUg9hZGRQYXJ0aXRpb25pbmcSjwEKFHVwZGF0ZV9pbnB1dF9saW5raW5nGAUgASgLMkIuZWR1LnVjaS5pY3MuYW1iZ
  XIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuVXBkYXRlSW5wdXRMaW5raW5nVjJCF+I/FBISdXBkYXRlSW5wdXRMaW5raW5nS
  ABSEnVwZGF0ZUlucHV0TGlua2luZxKbAQoXaW5pdGlhbGl6ZV9wb3J0X21hcHBpbmcYBiABKAsyRS5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Jbml0aWFsaXplUG9ydE1hcHBpbmdWMkIa4j8XEhVpbml0aWFsaXplUG9ydE1hc
  HBpbmdIAFIVaW5pdGlhbGl6ZVBvcnRNYXBwaW5nEnYKDW9wZW5fb3BlcmF0b3IYCyABKAsyPC5lZHUudWNpLmljcy5hbWJlci5lb
  mdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5PcGVuT3BlcmF0b3JWMkIR4j8OEgxvcGVuT3BlcmF0b3JIAFIMb3Blbk9wZXJhdG9yE
  noKDmxpbmtfY29tcGxldGVkGAwgASgLMj0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTGlua
  0NvbXBsZXRlZFYyQhLiPw8SDWxpbmtDb21wbGV0ZWRIAFINbGlua0NvbXBsZXRlZBKjAQoZaW5pdGlhbGl6ZV9vcGVyYXRvcl9sb
  2dpYxgVIAEoCzJHLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkluaXRpYWxpemVPcGVyYXRvc
  kxvZ2ljVjJCHOI/GRIXaW5pdGlhbGl6ZU9wZXJhdG9yTG9naWNIAFIXaW5pdGlhbGl6ZU9wZXJhdG9yTG9naWMSkwEKFW1vZGlme
  V9vcGVyYXRvcl9sb2dpYxgWIAEoCzJDLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLk1vZGlme
  U9wZXJhdG9yTG9naWNWMkIY4j8VEhNtb2RpZnlPcGVyYXRvckxvZ2ljSABSE21vZGlmeU9wZXJhdG9yTG9naWMScgoMcHl0aG9uX
  3ByaW50GBcgASgLMjsuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUHl0aG9uUHJpbnRWMkIQ4
  j8NEgtweXRob25QcmludEgAUgtweXRob25QcmludBKPAQoUcmVwbGF5X2N1cnJlbnRfdHVwbGUYGCABKAsyQi5lZHUudWNpLmljc
  y5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5SZXBsYXlDdXJyZW50VHVwbGVWMkIX4j8UEhJyZXBsYXlDdXJyZW50V
  HVwbGVIAFIScmVwbGF5Q3VycmVudFR1cGxlEo4BChNldmFsdWF0ZV9leHByZXNzaW9uGBkgASgLMkIuZWR1LnVjaS5pY3MuYW1iZ
  XIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuRXZhbHVhdGVFeHByZXNzaW9uVjJCF+I/FBISZXZhbHVhdGVFeHByZXNzaW9uS
  ABSEmV2YWx1YXRlRXhwcmVzc2lvbhKCAQoQcXVlcnlfc3RhdGlzdGljcxgpIAEoCzI/LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZ
  S5hcmNoaXRlY3R1cmUud29ya2VyLlF1ZXJ5U3RhdGlzdGljc1YyQhTiPxESD3F1ZXJ5U3RhdGlzdGljc0gAUg9xdWVyeVN0YXRpc
  3RpY3MSoAEKGXF1ZXJ5X2N1cnJlbnRfaW5wdXRfdHVwbGUYKiABKAsyRi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0Z
  WN0dXJlLndvcmtlci5RdWVyeUN1cnJlbnRJbnB1dFR1cGxlVjJCG+I/GBIWcXVlcnlDdXJyZW50SW5wdXRUdXBsZUgAUhZxdWVye
  UN1cnJlbnRJbnB1dFR1cGxlEqgBChtxdWVyeV9zZWxmX3dvcmtsb2FkX21ldHJpY3MYKyABKAsySC5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5RdWVyeVNlbGZXb3JrbG9hZE1ldHJpY3NWMkId4j8aEhhxdWVyeVNlbGZXb3Jrb
  G9hZE1ldHJpY3NIAFIYcXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzEp8BChhsb2NhbF9vcGVyYXRvcl9leGNlcHRpb24YYyABKAsyR
  i5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Mb2NhbE9wZXJhdG9yRXhjZXB0aW9uVjJCG+I/G
  BIWbG9jYWxPcGVyYXRvckV4Y2VwdGlvbkgAUhZsb2NhbE9wZXJhdG9yRXhjZXB0aW9uEqcBChp3b3JrZXJfZXhlY3V0aW9uX2Nvb
  XBsZXRlZBhlIAEoCzJILmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLldvcmtlckV4ZWN1dGlvb
  kNvbXBsZXRlZFYyQh3iPxoSGHdvcmtlckV4ZWN1dGlvbkNvbXBsZXRlZEgAUhh3b3JrZXJFeGVjdXRpb25Db21wbGV0ZWRCDgoMc
  2VhbGVkX3ZhbHVlQgniPwZIAFgAeAFiBnByb3RvMw=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}
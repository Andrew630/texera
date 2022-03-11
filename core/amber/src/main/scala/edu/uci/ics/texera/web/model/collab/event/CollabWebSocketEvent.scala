package edu.uci.ics.texera.web.model.collab.event
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import edu.uci.ics.texera.web.model.collab.response.HeartBeatResponse
import com.google.api.ResourceDescriptor.History

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new Type(value = classOf[CommandEvent]),
    new Type(value = classOf[HistoryEvent]),
    new Type(value = classOf[LockGrantedEvent]),
    new Type(value = classOf[ReleaseLockEvent]),
    new Type(value = classOf[LockRejectedEvent]),
    new Type(value = classOf[RestoreVersionEvent]),
    new Type(value = classOf[HeartBeatResponse]),
    new Type(value = classOf[WorkflowAccessEvent])
  )
)
trait CollabWebSocketEvent {}

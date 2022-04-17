import { Serializable } from "src/app/common/util/serializable-tree";
import { SerializableCallEvent } from "../service/undo-redo/EventSerializer";
import { YDocUpdate } from "../service/undo-redo/YDoc.service";

export interface WIdRequest
  extends Readonly<{
    wId: number;
  }> {}

export interface InformWIdEvent extends Readonly<{ message: string }> {}

export interface CommandRequest
  extends Readonly<{
    commandMessage: string;
  }> {}

export interface CommandEvent
  extends Readonly<{
    commandMessage: string;
  }> {}

export interface HistoryRequest extends Readonly<{
  payload: string;
  }> {}

export interface HistoryEvent extends Readonly<{
  payload: string;
  }> {}

export interface YDocRequest  extends Readonly<{
  payload: string;
  }> {}

export interface YDocEvent extends Readonly<{
  payload: string;
  }> {}


export interface HistoryMessage extends Readonly<SerializableCallEvent> {}

export interface YDocMessage extends Readonly<YDocUpdate> {}
export interface WorkflowAccessEvent
  extends Readonly<{
    workflowReadonly: boolean;
  }> {}

export type CollabWebsocketRequestTypeMap = {
  WIdRequest: WIdRequest;
  HeartBeatRequest: {};
  CommandRequest: CommandRequest;
  AcquireLockRequest: {};
  TryLockRequest: {};
  RestoreVersionRequest: {};
  HistoryRequest: HistoryRequest;
  YDocRequest: YDocRequest;
};

export type CollabWebsocketEventTypeMap = {
  InformWIdResponse: InformWIdEvent;
  HeartBeatResponse: {};
  CommandEvent: CommandEvent;
  ReleaseLockEvent: {};
  LockGrantedEvent: {};
  LockRejectedEvent: {};
  RestoreVersionEvent: {};
  WorkflowAccessEvent: WorkflowAccessEvent;
  HistoryEvent: HistoryEvent;
  YDocEvent: YDocEvent;
};

// helper type definitions to generate the request and event types
type ValueOf<T> = T[keyof T];
type CustomUnionType<T> = ValueOf<{
  [P in keyof T]: {
    type: P;
  } & T[P];
}>;

export type CollabWebsocketRequestTypes = keyof CollabWebsocketRequestTypeMap;
export type CollabWebsocketRequest = CustomUnionType<CollabWebsocketRequestTypeMap>;

export type CollabWebsocketEventTypes = keyof CollabWebsocketEventTypeMap;
export type CollabWebsocketEvent = CustomUnionType<CollabWebsocketEventTypeMap>;

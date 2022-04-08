import { Injectable } from "@angular/core";
import { Observable, Subject } from "rxjs";
import { asType, isNull, isType, nonNull } from "../../../common/util/assert";
import { Command, CommandMessage } from "../../types/command.interface";
import { WorkflowCollabService } from "../workflow-collab/workflow-collab.service";
import { FunctionWithSerializableArgs, getRestArgTypeFromFunction, JSONparse, JSONstringify, RemoveFirstArgFromFunction, Serializable } from "src/app/common/util/serializable-tree";
import { ObservableContextManager } from "src/app/common/util/context";
import { DescriptionsConfig } from "ng-zorro-antd/core/config";
import { HistoryEvent, HistoryMessage } from "../../types/collab-websocket.interface";

interface Context {
  recordHistory: boolean;
}
const DEFAULT_CONTEXT: Context = {
  recordHistory: true
};


export class History {

}

interface MethodMap {
  [className: string]: {
    [methodName: string] : Function
  }
}

interface InstanceMap {
  [className: string]: object[]
}

interface HistoryEnabledDescriptor<T> extends PropertyDescriptor {
  value?: FunctionWithSerializableArgs<T>
}
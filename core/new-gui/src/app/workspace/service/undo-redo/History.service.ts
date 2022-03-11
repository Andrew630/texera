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

  public static contextManager = ObservableContextManager<Context>(DEFAULT_CONTEXT)

  public static workflowCollabService: WorkflowCollabService | null = null;

  public static method_map: MethodMap = {}
  public static instance_map: InstanceMap = {}

  // decorate class methods to record method calls in history
  public static recordEvent() {
    return function <T>(target_class: any, methodName: string, descriptor: HistoryEnabledDescriptor<T>) {

      const className = target_class.constructor.name;
      const originalmethod = asType(descriptor.value, Function);
      console.log (`intercept ${className}.${methodName}`);  

      // create method map for class if it doesn't yet exist
      if (className in History.method_map == false) History.method_map[className] = {};
      History.method_map[className][methodName] = originalmethod;

      const newDescriptor: PropertyDescriptor = {
        configurable: descriptor.configurable,
        enumerable: descriptor.enumerable,
        writable: descriptor.writable,
        value: function (this: any, ...args: Serializable[]): any {

          console.log (`intercept call ${className}.${methodName}`);  
    
          // serialize classname, func name and args, instance no. if applicable
          
          const instanceNumber = History.getInstanceNumber(className, this);
          const serializedArgs = JSONstringify(args);
          
          // TODO: propagate stringified args
          console.log(`workflowColabService ${History.workflowCollabService}`);
          if (History.workflowCollabService != null) {
            console.log('broadcast');
            const historyMessage: HistoryMessage = {
              className: className, 
              instanceNumber: instanceNumber,
              methodName: methodName,
              serializedArgs: serializedArgs
            };
  
            const historyEvent: HistoryEvent = {
              payload: JSON.stringify(historyMessage)
            };
            History.workflowCollabService?.send("HistoryRequest", historyEvent);
          }
  
          History.execMethod(className, instanceNumber, methodName, serializedArgs);
            
        } as FunctionWithSerializableArgs<T>
      };

      return newDescriptor;
    };
  }

  public static registerClass<T extends Function>(_class: T): T {
    console.log(`registerClass ${_class.name}`);
    console.log(_class);
    History.instance_map[_class.name] = [];
    return new Proxy(_class, {
      construct(cls, args){
        console.log(`registerinstance ${_class.name}`);
        const instance = Reflect.construct(cls,args);
        console.log(instance);
        History.instance_map[_class.name].push(instance);
        return instance;
      }
    });
  }

  public static bindWorkflowCollabService(workflowCollabService: WorkflowCollabService) {
    History.workflowCollabService = workflowCollabService;
    console.log('bind bindWorkflowCollabService');
    History.workflowCollabService.getHistStream().subscribe((historyMessage: HistoryMessage) => {
      console.log(`recieve ${String(historyMessage.methodName)}(${historyMessage.serializedArgs})`);
      History.execMethod(historyMessage.className, historyMessage.instanceNumber, historyMessage.methodName, historyMessage.serializedArgs);
    });
  }

  private static getInstanceNumber(className: string, instance: any) {
    if (className in History.instance_map == false) throw Error(`${className} must be registered via History.registerClass before history service can listen to it's function calls`);
    const instance_no = History.instance_map[className].findIndex((elem) => elem === instance);
    if (instance_no === -1) throw Error(`instance ${instance} of ${className} not registered in History.instance_map`);
  
    return instance_no;
  }

  private static getMethod(className: string, methodName: string) {
    if (className in History.method_map == false) throw Error(`${className} must be registered via History.registerClass before history service can listen to it's function calls`);
    if (methodName in History.method_map[className] == false) throw Error(`${className}.${methodName} not registered in History.method_map`);
  
    return History.method_map[className][methodName];
  }

  private static execMethod<T>(className: string, instanceNumber: number, methodName: string, args: string): T {
    if (className in History.instance_map == false) throw Error(`${className} must be registered via History.registerClass before history service can listen to it's function calls`);
    if (methodName in History.method_map[className] == false) throw Error(`${className}.${methodName} not registered in History.method_map`);
    if (History.instance_map[className].length <= instanceNumber || instanceNumber < 0) throw Error(`Invalid instanceNumber ${instanceNumber} for class ${className}`);

    const parsedArgs = JSONparse(args);
    const instance = History.instance_map[className][instanceNumber];
    const method =History.method_map[className][methodName];

    return method.call(instance, ...parsedArgs);
  }
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
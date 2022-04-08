import { Serializable } from "src/app/common/util/serializable-tree";
import { CallEvent } from "./Recorder";

export class EventSerializer {
    public static methodMap: MethodMap = {}
    public static instanceMap: InstanceMap = {}

    public static serializeEvent(event: SerializableMethodCallEvent) {
        const serializableEvent: SerializableCallEvent = {
            className: event.className,
            methodName: event.methodName,
            instanceId: EventSerializer.getInstanceNumber(event.className, event.instance),
            args: event.args
        };

        return serializableEvent;
    }

    public static deserializeEvent(serializableEvent: SerializableCallEvent) {
        const event: CallEvent = {
            className: serializableEvent.className,
            methodName: serializableEvent.methodName,
            instance: EventSerializer.getInstance(serializableEvent.className, serializableEvent.instanceId, EventSerializer.instanceMap),
            method: EventSerializer.getMethod(serializableEvent.className, serializableEvent.methodName, EventSerializer.methodMap),
            args: serializableEvent.args
        };

        return event;
    }

    public static registerClass(className: string) {
        console.log(`registerClass ${className}`);
        if (className in EventSerializer.methodMap) return;
        EventSerializer.methodMap[className] = {};
        EventSerializer.instanceMap[className] = [];
    }

    public static registerInstance(className: string, instance: any) {
        console.log(`registerInstance ${className}`);
        EventSerializer.instanceMap[className].push(instance);
    }

    public static registerMethod(className: string, methodName: string, method: Function) {
        console.log(`registerMethod ${className}.${methodName}`);
        if (methodName in EventSerializer.methodMap[className]) return;
        EventSerializer.methodMap[className][methodName] = method;
    }

    private static getInstance(className: string, instanceId: number, instanceMap: InstanceMap) {
        return instanceMap[className][instanceId];
    }
    
    private static getInstanceNumber(className: string, instance: any) {
        if (!(className in EventSerializer.instanceMap)) throw Error(`class ${className} must be registered by EventSerializer.registerClass`);
        const instance_no = EventSerializer.instanceMap[className].findIndex((elem) => elem === instance);
        if (instance_no === -1) throw Error(`instance ${instance} of ${className} not registered by registered by EventSerializer.registerInstance`);
      
        return instance_no;
    }

    private static getMethod(className: string, methodName: string, methodMap: MethodMap) {
        return methodMap[className][methodName];
    }
}

export interface SerializableMethodCallEvent extends CallEvent {
    args: Serializable[]
}

export interface SerializableCallEvent {
    "className": string
    "methodName": string
    "instanceId": number
    "args": Serializable[]
}

export interface MethodMap {
    [className: string]: {
        [methodName: string] : Function
    }
}

export interface InstanceMap {
    [className: string]: object[]
}
  
type x = SerializableCallEvent;
type xx = x extends { [x: string] : any} ? true : false
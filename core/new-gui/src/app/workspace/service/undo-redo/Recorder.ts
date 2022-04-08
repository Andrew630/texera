import { ReplaySubject } from "rxjs";
import { asType } from "src/app/common/util/assert";

// Records method calls and outputs an event stream
export class Recorder {

    protected static callEventStream = new ReplaySubject<CallEvent>(undefined)

    public static getCallEventStream() {
        return Recorder.callEventStream.asObservable();
    }

    public static captureMethod() {
        return function <T>(targetClass: any, methodName: string, descriptor: PropertyDescriptor) {

            const className = targetClass.constructor.name;
            const originalmethod = asType(descriptor.value, Function);
            console.log (`Recording ${className}.${methodName}`);  
      
            const wrappedFunction = function (this: any, ...args: any[]) {
                Recorder.recordEvent(className, methodName, this, originalmethod, args);
                originalmethod.call(this, ...args);
            };
      
            const newDescriptor: PropertyDescriptor = {
              configurable: descriptor.configurable,
              enumerable: descriptor.enumerable,
              writable: descriptor.writable,
              value: wrappedFunction
            };
      
            return newDescriptor;
        };
    }

    protected static recordEvent(className: string, methodName: string, instance: Object, method: Function, args: any[]) {
        const callEvent: CallEvent = {
            className: className,
            methodName: methodName,
            instance: instance,
            method: method,
            args: args
        };
        
        Recorder.callEventStream.next(callEvent);
    }
}

export interface CallEvent {
    className: string
    methodName: string
    instance: Object
    method: Function
    args: any[]
}

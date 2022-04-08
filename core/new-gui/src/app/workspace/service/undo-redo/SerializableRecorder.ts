import { map, ReplaySubject } from "rxjs";
import { nonNull } from "src/app/common/util/assert";
import { FunctionWithSerializableArgs } from "src/app/common/util/serializable-tree";
import { SerializableMethodCallEvent, EventSerializer } from "./EventSerializer";
import { CallEvent, Recorder } from "./Recorder";

export class SerializableRecorder extends Recorder {

    protected static callEventStream = new ReplaySubject<SerializableMethodCallEvent>(undefined)

    public static getSerializableCallEventStream() {
        return this.getCallEventStream().pipe(map(EventSerializer.serializeEvent));
    }

    public static captureMethod() {
        const parentDecorator = super.captureMethod();

        const overrideDecorator = function <T>(targetClass: any, methodName: string, descriptor: SerializableArgsMethodDescriptor<T>) {
            
            const className = targetClass.constructor.name;
            
            EventSerializer.registerClass(className);
            EventSerializer.registerMethod(className, methodName, nonNull(descriptor.value));

            return parentDecorator(targetClass, methodName, descriptor);
        };
        
        return overrideDecorator;
    }

    public static captureClass<T extends { new (...args: any[]): {} }> (constructor: T) {
        const className  = constructor.name;
    
        EventSerializer.registerClass(className);

        return new Proxy(constructor, {
            construct(cls, args){
              const instance = Reflect.construct(cls,args);
              EventSerializer.registerInstance(className, instance);
              return instance;
            }
          });
    }
}

interface SerializableArgsMethodDescriptor<T> extends PropertyDescriptor {
    value?: FunctionWithSerializableArgs<T>
}
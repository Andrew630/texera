import { map, Observable } from "rxjs";
import { SerializableCallEvent, MethodMap, InstanceMap, EventSerializer } from "./EventSerializer";
import { CallEvent } from "./Recorder";
import { Replayer } from "./Replayer";

export class SerializableReplayer extends Replayer {
    public static replaySerializable(serializableEvent: SerializableCallEvent) {
        const event = EventSerializer.deserializeEvent(serializableEvent);
        console.log(event, "asdf");
        super.replay(event);
    }

    public static replaySerializableCallEventStream(stream: Observable<SerializableCallEvent>) {
        stream.subscribe((event) => {
            SerializableReplayer.replaySerializable(event);
        });
    }
}
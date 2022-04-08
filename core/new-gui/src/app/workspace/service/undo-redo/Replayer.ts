import { Observable } from "rxjs";
import { CallEvent } from "./Recorder";

export class Replayer {
    public static replay(event: CallEvent) {
        event.method.call(event.instance, ...event.args);
    }

    public static replayCallEventStream(stream: Observable<CallEvent>) {
        stream.subscribe((event) => {
            Replayer.replay(event);
        });
    }
}
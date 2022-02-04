import { Command, CommandMessage } from "../workflow-graph/model/workflow-action.service";

export abstract class Event implements Command {
    public modifiesWorkflow: boolean;

    private _dependents: Event[];
    private _requirements: Event[];

    constructor(modifiesWorkflow: boolean) {
        this.modifiesWorkflow = modifiesWorkflow;
        this._dependents = [];
        this._requirements = [];
    }

    public abstract execute(): void;
    public abstract undo(): void;
    public abstract redo?(): void;

    public get dependents(): Readonly<Event[]> {
        return this._dependents;
    }
    public get requirements(): Readonly<Event[]> {
        return this._requirements;
    }

    public addRequirements(...events: Event[]) {
        this.addRequirementsInternal(...events);
        events.forEach(event => {
            event.addDependentsInternal(this)
        });
    }
    
    public addDependents(...events: Event[]) {
        this._dependents = this._dependents.concat(events);
        events.forEach(event => {
            event.addRequirementsInternal(this)
        });
    }

    protected addRequirementsInternal(...events: Event[]){
        this._requirements = this._requirements.concat(events);
    }

    protected addDependentsInternal(...events: Event[]){
        this._requirements = this._requirements.concat(events);
    }

}
import { isPlainObject } from "lodash";
import { Point } from "src/app/workspace/types/workflow-common.interface";
import { isType } from "./assert";

export class Node<T extends Serializable> {
    public value: T
    public children: Node<T>[]
    public parent: Node<T> | null
    constructor(value: T, parent: Node<T> | null = null, children: Node<T>[] = []) {
        this.value = value;
        this.parent = parent;
        this.children = children;
    }

    public getPathToChild(child: Node<T>): Node<T>[]  {
        const path: Node<T>[] = [];

        while (child.parent != null && child.parent != this) {
            path.push(child);
            child = child.parent;
        }

        if (child.parent != this) {
            throw Error("can't get path to child because node isn't a child of this tree");
        }

        return path;
    }
}
  
type Primitive = null | boolean | number | string

type SerializableMap = Map<string, Serializable>

export type JsonObject =  {
    [x: string]: Serializable;
}

type JsonArray = Serializable[] | readonly Serializable[];


export type Serializable = Primitive | JsonArray | JsonObject | SerializableMap;

// given a function type T, returns a new function type T without the first argument
// or type never (if T isnt a function or T has no args)
// i.e RemoveFirstArgFromFunction< (arg0: boolean, arg1: boolean) => boolean > == (arg1: boolean) => boolean
export type RemoveFirstArgFromFunction<T> = T extends (arg0: infer U) => any
    ? () => any
    : T extends (arg0: infer U, ...rest: infer V) => any
    ? (...rest: V) => any
    : never

export type getRestArgTypeFromFunction<T> = T extends (...args: infer V) => any
    ? V
    : never

// Given a function type T, returns type never if the function's arguments aren't serializable
// ex. FunctionWithSerializableArgs<(arg0: boolean) => void> == (arg0: boolean) => void
// ex. FunctionWithSerializableArgs<(arg0: object) => void> == never
export type FunctionWithSerializableArgs<T> = T extends () => any
    ? T
    : T extends (arg0: infer U, ...rest: any[]) => any
    ? U extends Serializable
        ? RemoveFirstArgFromFunction<T> extends FunctionWithSerializableArgs<RemoveFirstArgFromFunction<T>>
            ? T
            : never
        : never
    : never

export function isSerializable(val: any): val is Serializable {
    if (val === undefined) return false;
    if (val === null) return true;
    if (typeof val === "boolean") return true;
    if (typeof val === "number") return true;
    if (typeof val === "string") return true;

    if (Array.isArray(val)) return val.every(isSerializable);
    if (isPlainObject(val)) {
        for (const property in val) {
            if (isSerializable(val) == false) {
                return false;
            }
        }
        return true;
    }
    return false;
}

// decorator that makes sure a function or method has serializable arguments
export function serializableArgs<T>(func: FunctionWithSerializableArgs<T>) {
    return func;
}

export function JSONstringify(val: Serializable) {
    return JSON.stringify(val, (k, v) => {
        if (isType(v, Map) == false) return v;

        let map: Map<string, Serializable> = v as Map<string, Serializable>;
        let obj: {[x: string]: Serializable} = {};
        for (const [key, value] of map) {
            obj[key] = value;
        }
        return {
            "___type": "map",
            "___value": obj,
        };
    });
}

export function JSONparse(val: string) {

    return JSON.parse(val, (k, v) => {
        if (isType(v, Object) == false) return v;
        if ("___type" in v == false) return v;
        if (v["___type"] != "map") return v;

        let obj: {[x: string]: Serializable} = v["___value"];
        let map: Map<string, Serializable> = new Map<string, Serializable>();
        for (const property in obj) {
            map.set(property, obj[property]);
        }
        return map;
    });
}




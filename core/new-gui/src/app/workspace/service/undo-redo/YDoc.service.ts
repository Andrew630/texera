import { Injectable } from "@angular/core";
import { WorkflowCollabService } from "../workflow-collab/workflow-collab.service";
import { v4 as uuid } from "uuid";
import * as Y from 'yjs'

@Injectable({
    providedIn: "root",
  })
export class YDocService {
    public readonly doc = new Y.Doc();
    private id = uuid();
    constructor(private workflowCollabService: WorkflowCollabService) {
      this.registerSendUpdateHandler();
      this.registerRecieveUpdateHandler();
    }

    private registerSendUpdateHandler() {
      this.doc.on('update', (update: Uint8Array, origin: any) => {
        if (origin !== this.id){
          this.workflowCollabService.send("YDocRequest", {
            payload: serialize({
              update: update,
              transactionOrigin: this.id
            })
          })
        }
      });
    }

    private registerRecieveUpdateHandler(){
      this.workflowCollabService.getYDocStream().subscribe(
        (yDocMessage) => {
          Y.applyUpdate(this.doc, yDocMessage.update, yDocMessage.transactionOrigin)
        }
      )
    }
}

export interface YDocUpdate{
    update: Uint8Array
    transactionOrigin?: any
}

export function serialize(yDocUpdate: YDocUpdate){
  return JSON.stringify({
    update: arrayToBase64(yDocUpdate.update),
    transactionOrigin: yDocUpdate.transactionOrigin
  })
}

export function deserialize(yDocUpdate: string): YDocUpdate{
  const serialized = JSON.parse(yDocUpdate)
  return {
    update: base64ToArray(serialized.update),
    transactionOrigin: serialized.transactionOrigin
  }
}

export function arrayToBase64(arr: Uint8Array) {
  // My IDE shows deprecation warnings for btoa()
  // Pretty sure btoa isn't being deprecated though, since this we aren't running on nodejs
  return btoa(String.fromCharCode.apply(null, arr as any as number[]));
}

export function base64ToArray(base64: string) {
  // My IDE shows deprecation warnings for atob()
  // Pretty sure btoa isn't being deprecated though, since this we aren't running on nodejs
  return new Uint8Array(atob(base64).split("").map(function(c) {
    return c.charCodeAt(0); }));
}
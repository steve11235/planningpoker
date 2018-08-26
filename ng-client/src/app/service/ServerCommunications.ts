import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { HttpErrorResponse } from "@angular/common/http/src/response";
import { Observable } from "rxjs";
import { webSocket, WebSocketSubject } from 'rxjs/webSocket'

import { ClientRequest } from "../shared/ClientRequest";
import { ServerResponse } from "../shared/ServerResponse";
import { ServerUpdate } from "../shared/ServerUpdate";

@Injectable( {providedIn: 'root'})
export class ServerCommunications {
	private subject: WebSocketSubject<ServerUpdate> = null;

	constructor(private http: HttpClient) {
		// Do nothing
	}

	retrieveEmptyServerUpdate(message: String): ServerUpdate {
		const serverUpdate: ServerUpdate = new ServerUpdate({ "message": message })
		return serverUpdate;
	}

	/*
	 * Send a request to the server.
	 * request: ClientRequest required
	 * callback?: () => boolean optional, indicates whether the call was successful or not upon completion
	 */
	sendRequest(request: ClientRequest, callback?: (joinedSuccess: boolean) => void): void {
		let success: boolean = true;

		// Use a relative URI in order to avoid CORS issues
		this.http.post<ServerResponse>("/request", request)
			.subscribe(
			(response) => {
				if (response.error) {
					success = false;
					alert("Request failed! " + response.message);
				}
			},
			(errorResponse: HttpErrorResponse) => {
				success = false;
				alert("POST failed: " + errorResponse.message);
			},
			() => {
				if (callback) {
					callback(success);
				}
			}
			);
	}

	createWebSocket(voterName: string, receiveServerUpdateCallback: (serverUpdate: ServerUpdate) => void): void {
		const hostName = window.location.hostname + ":" + window.location.port;
		this.subject = webSocket("ws:" + hostName + "/webSocket/" + voterName);
		this.subject.subscribe(
			(serverUpdate) => {
				receiveServerUpdateCallback(serverUpdate);
			},
			(err) => {
				if (typeof err === "object" && err.constructor === CloseEvent) {
					return;
				}
				if (err instanceof Error) {
					alert("Error on WebSocket. Please 'Leave' and re'Join': " + err.message);
				}
			},
			() => {
				this.subject = null;
			}
		);
	}
}
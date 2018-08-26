import { Component } from "@angular/core";
import { ServerCommunications } from "./service/ServerCommunications";
import { ServerUpdate } from "./shared/ServerUpdate";
import { Voter } from "./shared/Voter";
import { ClientRequest } from "./shared/ClientRequest";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"]
})
export class AppComponent {
  voteLabels: string[] = [
    "?",
    ".5",
    "1",
    "2",
    "3",
    "5",
    "8",
    "13",
    "20",
    "40",
    "100"
  ];
  isJoined: boolean = false;
  voterName: string = null;
  serverUpdate: ServerUpdate;

  constructor(private serverCommunications: ServerCommunications) {
    this.serverUpdate = serverCommunications.retrieveEmptyServerUpdate(
      "Waiting to join."
    );
  }

  isReadyToVote(): boolean {
    if (!this.isJoined) {
      return false;
    }

    return this.serverUpdate.voteStatus != 1;
  }

  isVoteInProgress(): boolean {
    if (!this.isJoined) {
      return false;
    }

    return this.serverUpdate.voteStatus == 1;
  }

  onJoinLeave() {
    if (!this.isJoined) {
      // If the voterName is blank, ignore the button push
      if (!this.voterName) {
        return;
      }

      const clientRequest: ClientRequest = new ClientRequest(
        ClientRequest.JOIN,
        this.voterName
      );
      this.serverCommunications.sendRequest(
        clientRequest,
        (joinedSuccess: boolean) => {
          this.doAfterJoin(joinedSuccess);
        }
      );

      return;
    }

    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.LEAVE,
      this.voterName
    );
    this.serverCommunications.sendRequest(
      clientRequest,
      (leftSuccess: boolean) => {
        this.doAfterLeave(leftSuccess);
      }
    );
  }

  private doAfterJoin(joinedSuccess: boolean): void {
    if (!joinedSuccess) {
      return;
    }

    this.isJoined = true;

    this.serverCommunications.createWebSocket(
      this.voterName,
      (serverUpdate: ServerUpdate) => this.receiveServerUpdate(serverUpdate)
    );
  }

  private doAfterLeave(leftSuccess: boolean): void {
    if (!leftSuccess) {
      alert(
        "Failed to leave the vote. Please use the browser Refresh button to reload the application."
      );

      return;
    }

    this.isJoined = false;
  }

  private receiveServerUpdate(serverUpdate: ServerUpdate) {
    this.serverUpdate = serverUpdate;
  }

  onStartVote() {
    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.START_VOTE,
      this.voterName
    );
    this.serverCommunications.sendRequest(clientRequest);
  }

  onCloseVote() {
    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.END_VOTE,
      this.voterName
    );
    this.serverCommunications.sendRequest(clientRequest);
  }

  onCancelVote() {
    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.CANCEL_VOTE,
      this.voterName
    );
    this.serverCommunications.sendRequest(clientRequest);
  }

  onRefresh() {
    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.REFRESH,
      this.voterName
    );
    this.serverCommunications.sendRequest(clientRequest);
  }

  onVote(vote: number) {
    const clientRequest: ClientRequest = new ClientRequest(
      ClientRequest.VOTE,
      this.voterName,
      vote
    );
    this.serverCommunications.sendRequest(clientRequest);
  }

  // Helper method called by view
  isYetToVote(voter: Voter): boolean {
    // Applies only to vote in progress, complete
    if (this.serverUpdate.voteStatus == 0) {
      return false;
    }

    return !voter.hasVoted;
  }

  // Helper method called by view
  calculateOffByClass(vote: number, index: number): string {
    if (this.serverUpdate.voteStatus != 2) {
      return "";
    }

    // Handle situation where vote was forced closed with this voter not voting
    if (vote == -1) {
      return "";
    }

    // Don't update voter/button cells unless the voter/button voted for that cell
    if (vote != index) {
      return "";
    }

    // Handle situation where there is no average
    if (this.serverUpdate.averageVote == -1) {
      return "offBy3";
    }

    if (vote == 0) {
      return "offBy3";
    }

    const offBy: number = Math.abs(vote - this.serverUpdate.averageVote);

    if (offBy > 3) {
      return "offBy3";
    }

    return "offBy" + offBy;
  }
}

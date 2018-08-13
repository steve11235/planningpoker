webpackJsonp(["main"],{

/***/ "../../../../../src/$$_lazy_route_resource lazy recursive":
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncatched exception popping up in devtools
	return Promise.resolve().then(function() {
		throw new Error("Cannot find module '" + req + "'.");
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = "../../../../../src/$$_lazy_route_resource lazy recursive";

/***/ }),

/***/ "../../../../../src/app/ClientRequest.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ClientRequest; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_util__ = __webpack_require__("../../../../util/util.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_util___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_util__);

var ClientRequest = (function () {
    function ClientRequest(requestTypeParam, voterNameParam, voteParam) {
        if (voteParam === void 0) { voteParam = -1; }
        this.requestType = requestTypeParam;
        this.voterName = voterNameParam;
        this.vote = voteParam;
        var requestTypeValid = false;
        for (var i = 0; i < ClientRequest.requestTypes.length; i++) {
            if (ClientRequest.requestTypes[i] === this.requestType) {
                requestTypeValid = true;
                break;
            }
        }
        if (!requestTypeValid) {
            throw new __WEBPACK_IMPORTED_MODULE_0_util__["error"]("ClientRequest invalid requestType: " + this.requestType);
        }
        if (!this.voterName) {
            throw new __WEBPACK_IMPORTED_MODULE_0_util__["error"]("ClientRequest voterName is blank.");
        }
    }
    ClientRequest.BUMP = "bump";
    ClientRequest.CANCEL_VOTE = "cancelVote";
    ClientRequest.END_VOTE = "endVote";
    ClientRequest.JOIN = "join";
    ClientRequest.LEAVE = "leave";
    ClientRequest.REFRESH = "refresh";
    ClientRequest.START_VOTE = "startVote";
    ClientRequest.VOTE = "vote";
    ClientRequest.requestTypes = [ClientRequest.BUMP, ClientRequest.CANCEL_VOTE, ClientRequest.END_VOTE,
        ClientRequest.JOIN, ClientRequest.LEAVE, ClientRequest.REFRESH, ClientRequest.START_VOTE, ClientRequest.VOTE];
    return ClientRequest;
}());



/***/ }),

/***/ "../../../../../src/app/ServerCommunications.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ServerCommunications; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__("../../../core/esm5/core.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_rxjs_Rx__ = __webpack_require__("../../../../rxjs/_esm5/Rx.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_common_http__ = __webpack_require__("../../../common/esm5/http.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__ServerUpdate__ = __webpack_require__("../../../../../src/app/ServerUpdate.ts");
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};




var ServerCommunications = (function () {
    function ServerCommunications(http) {
        this.http = http;
        this.subject = null;
        // Do nothing
    }
    ServerCommunications.prototype.retrieveEmptyServerUpdate = function (message) {
        var serverUpdate = new __WEBPACK_IMPORTED_MODULE_3__ServerUpdate__["a" /* ServerUpdate */]({ "message": message });
        return serverUpdate;
    };
    /*
     * Send a request to the server.
     * request: ClientRequest required
     * callback?: () => boolean optional, indicates whether the call was successful or not upon completion
     */
    ServerCommunications.prototype.sendRequest = function (request, callback) {
        var success = true;
        // Use a relative URI in order to avoid CORS issues
        this.http.post("/request", request)
            .subscribe(function (response) {
            if (response.error) {
                success = false;
                alert("Request failed! " + response.message);
            }
        }, function (errorResponse) {
            success = false;
            alert("POST failed: " + errorResponse.message);
        }, function () {
            if (callback) {
                callback(success);
            }
        });
    };
    ServerCommunications.prototype.createWebSocket = function (voterName, receiveServerUpdateCallback) {
        var _this = this;
        var hostName = window.location.hostname + ":" + window.location.port;
        console.log("Host name: " + hostName);
        this.subject = __WEBPACK_IMPORTED_MODULE_1_rxjs_Rx__["a" /* Observable */].webSocket("ws:" + hostName + "/webSocket/" + voterName);
        this.subject.subscribe(function (serverUpdate) {
            receiveServerUpdateCallback(serverUpdate);
        }, function (err) {
            if (typeof err === "object" && err.constructor === CloseEvent) {
                return;
            }
            if (err instanceof Error) {
                alert("Error on WebSocket. Please 'Leave' and re'Join': " + err.message);
            }
        }, function () {
            _this.subject = null;
        });
    };
    ServerCommunications = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["w" /* Injectable */])(),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_2__angular_common_http__["a" /* HttpClient */]])
    ], ServerCommunications);
    return ServerCommunications;
}());



/***/ }),

/***/ "../../../../../src/app/ServerUpdate.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return ServerUpdate; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Voter__ = __webpack_require__("../../../../../src/app/Voter.ts");

var ServerUpdate = (function () {
    /*
     * Invalid/missing values are ignored; defaults reflect proper error values
     */
    function ServerUpdate(rawServerUpdate) {
        this.rawServerUpdate = rawServerUpdate;
        this.message = "Received an invalid server update!";
        // 0: no vote (waiting to join, waiting for first vote, previous vote canceled, refresh selected)
        // 1: vote in progress (multiple updates as each voter votes)
        // 2: vote complete (previous voting session was closed and results are provided)
        this.voteStatus = 0;
        this.averageVote = -1;
        this.voters = [];
        if (!rawServerUpdate) {
            return;
        }
        if (typeof rawServerUpdate.message === "string" && rawServerUpdate.message.length > 0) {
            this.message = rawServerUpdate.message;
        }
        if (typeof rawServerUpdate.voteStatus === "number" && rawServerUpdate.voteStatus >= 0 && rawServerUpdate.voteStatus <= 2) {
            this.voteStatus = rawServerUpdate.voteStatus;
        }
        if (rawServerUpdate.averageVote >= 0 && rawServerUpdate.averageVote <= 11) {
            this.averageVote = rawServerUpdate.averageVote;
        }
        // Cannot check typeof array
        if (rawServerUpdate.voters && rawServerUpdate.voters.length) {
            var voter = null;
            for (var i = 0; i < rawServerUpdate.voters.length; i++) {
                voter = new __WEBPACK_IMPORTED_MODULE_0__Voter__["a" /* Voter */](rawServerUpdate.voters[i]);
                this.voters.push(voter);
            }
        }
    }
    return ServerUpdate;
}());



/***/ }),

/***/ "../../../../../src/app/Voter.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return Voter; });
var Voter = (function () {
    /*
     * Invalid/missing values are ignored; defaults reflect proper error values
     */
    function Voter(rawVoter) {
        this.rawVoter = rawVoter;
        this.name = "!error!";
        this.hasVoted = false;
        this.vote = -1;
        if (!rawVoter) {
            return;
        }
        if (typeof rawVoter.name === "string") {
            this.name = rawVoter.name;
        }
        if (typeof rawVoter.hasVoted === "boolean") {
            this.hasVoted = rawVoter.hasVoted;
        }
        if (typeof rawVoter.vote === "number" && rawVoter.vote >= 0 && rawVoter.vote <= 11) {
            this.vote = rawVoter.vote;
        }
    }
    return Voter;
}());



/***/ }),

/***/ "../../../../../src/app/app.component.css":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("../../../../css-loader/lib/css-base.js")(false);
// imports


// module
exports.push([module.i, "@-webkit-keyframes blink {\n\t0% { color: inherit }\n\t50% { color: transparent }\n}\n\n@keyframes blink {\n\t0% { color: inherit }\n\t50% { color: transparent }\n}\n\n.blink {\n\t-webkit-animation-name: blink;\n\t        animation-name: blink;\n\t-webkit-animation-duration: 2000ms;\n\t        animation-duration: 2000ms;\n\t-webkit-animation-iteration-count: infinite;\n\t        animation-iteration-count: infinite;\n}\n\n.yetToVote {\n\tcolor: #AAF;\n}\n\n.offBy0 {\n\tcolor: black;\n\tbackground-color: #0F0;\n}\n\n.offBy1 {\n\tcolor: black;\n\tbackground-color: #0FF;\n}\n\n.offBy2 {\n\tbackground-color: #05F;\n}\n\n.offBy3 {\n\tbackground-color: #00F;\n}", ""]);

// exports


/*** EXPORTS FROM exports-loader ***/
module.exports = module.exports.toString();

/***/ }),

/***/ "../../../../../src/app/app.component.html":
/***/ (function(module, exports) {

module.exports = "<img src=\"assets/pp.png\" style=\"float: left; margin-right: 2vh; height: 25vh;\">\n<div style=\"float:left;\">\n  <h1>Time to Vote!</h1>\n  <p><span class=\"blink\">&gt;&gt;</span>&nbsp;{{serverUpdate.message}}</p>\n  <p>\n    <label>Voter name:</label>\n    <input class=\"w10\" [(ngModel)]=\"voterName\" [disabled]=\"isJoined\">\n    <button (click)=\"onJoinLeave()\">{{isJoined ? \"Leave\" : \"Join\"}}</button>\n  </p>\n  <p>\n    <button (click)=\"onStartVote()\" [disabled]=\"!isReadyToVote()\">Start Voting</button>\n    <button (click)=\"onCloseVote()\" [disabled]=\"!isVoteInProgress()\">Close Voting</button>\n    <button (click)=\"onCancelVote()\" [disabled]=\"!isVoteInProgress()\">Cancel Voting</button>\n    <button (click)=\"onRefresh()\" [disabled]=\"!isReadyToVote()\">Refresh</button>\n  </p>\n  <div class=\"floatWrapper\">\n    <span class=\"cell w10\"><i>Voters</i></span>\n    <button class=\"cell\" [ngClass]=\"calculateOffByClass(i, i)\" [disabled]=\"!isVoteInProgress()\" (click)=\"onVote(i)\" *ngFor=\"let voteLabel of voteLabels; let i = index;\">{{voteLabel}}</button>\n  </div>\n  <div class=\"floatWrapper\" *ngFor=\"let voter of serverUpdate.voters\">\n    <span class=\"cell w10\" [ngClass]=\"isYetToVote(voter) ? 'yetToVote' : ''\">{{voter.name}}</span>\n    <span class=\"cell\" [ngClass]=\"calculateOffByClass(voter.vote, i)\" *ngFor=\"let voteLabel of voteLabels; let i = index;\">&nbsp;</span>\n  </div>\n</div>\n"

/***/ }),

/***/ "../../../../../src/app/app.component.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppComponent; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__("../../../core/esm5/core.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__ServerCommunications__ = __webpack_require__("../../../../../src/app/ServerCommunications.ts");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__ClientRequest__ = __webpack_require__("../../../../../src/app/ClientRequest.ts");
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var AppComponent = (function () {
    function AppComponent(serverCommunications) {
        this.serverCommunications = serverCommunications;
        this.voteLabels = ["?", ".5", "1", "2", "3", "5", "8", "13", "20", "40", "100"];
        this.isJoined = false;
        this.voterName = null;
        this.serverUpdate = serverCommunications.retrieveEmptyServerUpdate("Waiting to join.");
    }
    AppComponent.prototype.isReadyToVote = function () {
        if (!this.isJoined) {
            return false;
        }
        return this.serverUpdate.voteStatus != 1;
    };
    AppComponent.prototype.isVoteInProgress = function () {
        if (!this.isJoined) {
            return false;
        }
        return this.serverUpdate.voteStatus == 1;
    };
    AppComponent.prototype.onJoinLeave = function () {
        var _this = this;
        if (!this.isJoined) {
            // If the voterName is blank, ignore the button push
            if (!this.voterName) {
                return;
            }
            var clientRequest_1 = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].JOIN, this.voterName);
            this.serverCommunications.sendRequest(clientRequest_1, function (joinedSuccess) { _this.doAfterJoin(joinedSuccess); });
            return;
        }
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].LEAVE, this.voterName);
        this.serverCommunications.sendRequest(clientRequest, function (leftSuccess) { _this.doAfterLeave(leftSuccess); });
    };
    AppComponent.prototype.doAfterJoin = function (joinedSuccess) {
        var _this = this;
        if (!joinedSuccess) {
            return;
        }
        this.isJoined = true;
        this.serverCommunications.createWebSocket(this.voterName, function (serverUpdate) { return _this.receiveServerUpdate(serverUpdate); });
    };
    AppComponent.prototype.doAfterLeave = function (leftSuccess) {
        if (!leftSuccess) {
            alert("Failed to leave the vote. Please use the browser Refresh button to reload the application.");
            return;
        }
        this.isJoined = false;
    };
    AppComponent.prototype.receiveServerUpdate = function (serverUpdate) {
        this.serverUpdate = serverUpdate;
    };
    AppComponent.prototype.onStartVote = function () {
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].START_VOTE, this.voterName);
        this.serverCommunications.sendRequest(clientRequest);
    };
    AppComponent.prototype.onCloseVote = function () {
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].END_VOTE, this.voterName);
        this.serverCommunications.sendRequest(clientRequest);
    };
    AppComponent.prototype.onCancelVote = function () {
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].CANCEL_VOTE, this.voterName);
        this.serverCommunications.sendRequest(clientRequest);
    };
    AppComponent.prototype.onRefresh = function () {
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].REFRESH, this.voterName);
        this.serverCommunications.sendRequest(clientRequest);
    };
    AppComponent.prototype.onVote = function (vote) {
        var clientRequest = new __WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */](__WEBPACK_IMPORTED_MODULE_2__ClientRequest__["a" /* ClientRequest */].VOTE, this.voterName, vote);
        this.serverCommunications.sendRequest(clientRequest);
    };
    // Helper method called by view
    AppComponent.prototype.isYetToVote = function (voter) {
        // Applies only to vote in progress, complete
        if (this.serverUpdate.voteStatus == 0) {
            return false;
        }
        return !voter.hasVoted;
    };
    // Helper method called by view
    AppComponent.prototype.calculateOffByClass = function (vote, index) {
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
        var offBy = Math.abs(vote - this.serverUpdate.averageVote);
        if (offBy > 3) {
            return "offBy3";
        }
        return "offBy" + offBy;
    };
    AppComponent = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'app-root',
            template: __webpack_require__("../../../../../src/app/app.component.html"),
            styles: [__webpack_require__("../../../../../src/app/app.component.css")]
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1__ServerCommunications__["a" /* ServerCommunications */]])
    ], AppComponent);
    return AppComponent;
}());



/***/ }),

/***/ "../../../../../src/app/app.module.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AppModule; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__ = __webpack_require__("../../../platform-browser/esm5/platform-browser.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_core__ = __webpack_require__("../../../core/esm5/core.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__angular_forms__ = __webpack_require__("../../../forms/esm5/forms.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__angular_common_http__ = __webpack_require__("../../../common/esm5/http.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__ServerCommunications__ = __webpack_require__("../../../../../src/app/ServerCommunications.ts");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__app_component__ = __webpack_require__("../../../../../src/app/app.component.ts");
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};






var AppModule = (function () {
    function AppModule() {
    }
    AppModule = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_1__angular_core__["E" /* NgModule */])({
            declarations: [
                __WEBPACK_IMPORTED_MODULE_5__app_component__["a" /* AppComponent */]
            ],
            imports: [
                __WEBPACK_IMPORTED_MODULE_0__angular_platform_browser__["a" /* BrowserModule */],
                __WEBPACK_IMPORTED_MODULE_2__angular_forms__["a" /* FormsModule */],
                __WEBPACK_IMPORTED_MODULE_3__angular_common_http__["b" /* HttpClientModule */]
            ],
            providers: [
                __WEBPACK_IMPORTED_MODULE_4__ServerCommunications__["a" /* ServerCommunications */]
            ],
            bootstrap: [__WEBPACK_IMPORTED_MODULE_5__app_component__["a" /* AppComponent */]]
        })
    ], AppModule);
    return AppModule;
}());



/***/ }),

/***/ "../../../../../src/environments/environment.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return environment; });
// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.
var environment = {
    production: false
};


/***/ }),

/***/ "../../../../../src/main.ts":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__("../../../core/esm5/core.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__ = __webpack_require__("../../../platform-browser-dynamic/esm5/platform-browser-dynamic.js");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__app_app_module__ = __webpack_require__("../../../../../src/app/app.module.ts");
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__environments_environment__ = __webpack_require__("../../../../../src/environments/environment.ts");




if (__WEBPACK_IMPORTED_MODULE_3__environments_environment__["a" /* environment */].production) {
    Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["_7" /* enableProdMode */])();
}
Object(__WEBPACK_IMPORTED_MODULE_1__angular_platform_browser_dynamic__["a" /* platformBrowserDynamic */])().bootstrapModule(__WEBPACK_IMPORTED_MODULE_2__app_app_module__["a" /* AppModule */])
    .catch(function (err) { return console.log(err); });


/***/ }),

/***/ 0:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__("../../../../../src/main.ts");


/***/ })

},[0]);
//# sourceMappingURL=main.bundle.js.map
sap.ui.define([], function () {
	"use strict";

	return {

		/**
		 * Rounds the number unit value to 2 digits
		 * @public
		 * @param {string} sValue the number string to be rounded
		 * @returns {string} sValue with 2 digits rounded
		 */
		numberUnit: function (sValue) {
			if (!sValue) {
				return "";
			}
			return parseFloat(sValue).toFixed(2);
		},
		config: function (key) {
			if(key=="performance") {
				var model = this.getModel("config");
				return model.getProperty("/performanceTraceEnabled");
			}else {
				return true
			}
		},
		batchDuration: function (startTime, endTime) {
			if(startTime && endTime) {
				var duration = endTime - startTime;
				var milliseconds = parseInt((duration%1000)/100)
				, seconds = parseInt((duration/1000)%60)
				, minutes = parseInt((duration/(1000*60))%60)
				, hours = parseInt((duration/(1000*60*60))%24);
		
				hours = (hours < 10) ? "0" + hours : hours;
				minutes = (minutes < 10) ? "0" + minutes : minutes;
				seconds = (seconds < 10) ? "0" + seconds : seconds;
			
				return hours + ":" + minutes + ":" + seconds + "." + milliseconds;
			} else {
				return "";
			}
			
		},

		batchStopAction: function(status, exitStatus) {
			if(status == "STARTED" && exitStatus == "UNKNOWN") {
				return true;
			} else {
				return false;
			}
		},

		batchRestartAction: function(status, exitStatus) {
			if(exitStatus == "FAILED" || exitStatus == "STOPPED") {
				return true;
			} else {
				return false;
			}
		},

		batchState: function (status) {
			switch (status) {
				case "COMPLETED":					
					return "Success";
					break;
				case "FAILED":
					return "Error";
					break;
				case "RESTARTED":
					return "Warning";
					break;
				default:
					return "None";
			}
		},

		batchStateIcon: function (status) {
			switch (status) {
				case "COMPLETED":	
					return "sap-icon://status-positive";
					break;
				case "FAILED":
					return "sap-icon://status-critical";
					break;
				case "RESTARTED":
					return "sap-icon://alert";
					break;
				case "STOPPED":
					return "sap-icon://stop";
					break;					
				default:
					return "sap-icon://status-inactive";
			}
		},
		repState: function (status) {
			switch (status) {
				case "true":					
					return "Success";
					break;
				case "false":
					return "Error";
					break;
				default:
					return "None";
			}
		},

		repStateIcon: function (replicationEnabled,region) {
			switch (replicationEnabled) {
				case true:	
					if(region == "secondary")
					return "sap-icon://arrow-right";
					else 
					return "sap-icon://arrow-left";
					break;
				case false:
					return "sap-icon://status-critical";
					break;
				default:
					return "sap-icon://status-inactive";
			}
		},
		avgTimeTaken: function(avgTimeTakenSeconds) {
			return new Date(avgTimeTakenSeconds * 1000).toISOString().substr(11, 8)
		},
		secondsToMinute: function (d) {
			d = Number(d);
			var m = Math.floor(d % 3600 / 60);
			var s = Math.floor(d % 3600 % 60);
			return ('0' + m).slice(-2) + "." + ('0' + s).slice(-2);
		},
		moreDetailsVisible: function(d) {
			if(d == 'tm'){
				return false;
			} else {
				return true;
			}
		}
	};
});
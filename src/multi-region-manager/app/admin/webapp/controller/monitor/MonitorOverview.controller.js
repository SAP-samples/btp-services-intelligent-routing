sap.ui.define([
	"../common/BaseController",
	"sap/ui/model/json/JSONModel",
	"../../model/formatter",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"sap/ui/core/Fragment",
	"sap/m/MessageToast",
	"sap/ui/model/Sorter",
	"sap/m/MessageBox",
], function (BaseController, JSONModel, formatter, Filter, FilterOperator,Fragment,MessageToast,Sorter,MessageBox) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.monitor.MonitorOverview", {
		formatter: formatter,
		onInit : function () {
			var oTable = this.byId("table");
			this.region = "primary";
			var jsonModel = new JSONModel();
			jsonModel.attachRequestCompleted(function() {
				oTable.setBusy(false);
			});
			this.setModel(jsonModel);
			this.getRouter().getRoute("monitorOverview").attachPatternMatched(this._onObjectMatched, this);			
		},
		_onObjectMatched: function (oEvent) {
			//this.onRefresh();
		},

		onRefresh : function () {
			this.byId("table").setBusy(true);
			this.getModel().loadData("/api/monitoring");
		},
		onRtrReplication: function(oEvent) {
			this.getRouter().navTo("hanaMonitorReplication");
		},
		onAemReplication: function(oEvent) {
			this.getRouter().navTo("aemMonitorReplication");
		},
		onIflowStatus: function(oEvent) {
			this.getRouter().navTo("ciMonitorArtifacts");
		},
	});
});
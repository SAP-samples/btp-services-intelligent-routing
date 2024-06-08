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

	return BaseController.extend("com.sap.region.manager.ui.controller.monitor.CIMonitorArtifacts", {
		formatter: formatter,
		onInit : function () {
			var oViewModel, oTableModel;
			this.region = "primary";
			this.getRouter().getRoute("ciMonitorArtifacts").attachPatternMatched(this._onObjectMatched, this);

			oViewModel = new JSONModel({
				saveAsTileTitle: this.getResourceBundle().getText("saveAsTileTitle", this.getResourceBundle().getText("oViewModelTitle")),
				shareOnJamTitle: this.getResourceBundle().getText("worklistTitle"),
				shareSendEmailSubject: this.getResourceBundle().getText("shareSendEmailWorklistSubject"),
				shareSendEmailMessage: this.getResourceBundle().getText("shareSendEmailWorklistMessage", [location.href]),
			});
			this.setModel(oViewModel, "oViewModel");
			oTableModel = new JSONModel();
			this.setModel(oTableModel);
			this.addHistoryEntry({
				title: this.getResourceBundle().getText("oViewModelTitle"),
				icon: "sap-icon://table-view",
				intent: "#batch-display"
			}, true);
		},
		_onObjectMatched: function (oEvent) {
			this.onRefresh();
		},			
		onRefresh : function () {
			this.getModel().loadData("/api/ciProcessingArtifacts?region="+this.region);
		},
		onFilterSelect : function (oEvent) {
			this.region = oEvent.getParameter("key");
			this.onRefresh();
		}	
	});
});
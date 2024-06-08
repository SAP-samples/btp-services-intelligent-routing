sap.ui.define([
	"./common/BaseController",
	"sap/ui/model/json/JSONModel",
	"../model/formatter",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"sap/ui/core/Fragment",
	"sap/m/MessageToast",
	"sap/ui/model/Sorter",
	"sap/m/MessageBox",
], function (BaseController, JSONModel, formatter, Filter, FilterOperator,Fragment,MessageToast,Sorter,MessageBox) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.Home", {
		formatter: formatter,
		onInit : function () {
			var oTable = this.byId("table");
			this.region = "primary";
			var jsonModel = new JSONModel();
			jsonModel.attachRequestCompleted(function() {
				oTable.setBusy(false);
			});
			this.setModel(jsonModel);
			this.onRefresh();
			//this.getRouter().getRoute("monitorOverview").attachPatternMatched(this._onObjectMatched, this);			
		},
		_onObjectMatched: function (oEvent) {
			this.onRefresh();
		},

		onRefresh : function () {
			this.byId("table").setBusy(true);
			this.getModel().loadData("/api/monitoring");
		},
		onMoreDetails: function(oEvent){
			var serviceKey = this.getModel().getObject(oEvent.getSource().getParent().getBindingContextPath()).serviceKey;
			if(serviceKey == "sda") {
				this.getRouter().navTo("hanaMonitorReplication");
			}else if(serviceKey == "aem") {
				this.getRouter().navTo("aemMonitorReplication");
			}else if(serviceKey == "ci") {
				this.getRouter().navTo("ciMonitorArtifacts");
			}
		},
	});
});
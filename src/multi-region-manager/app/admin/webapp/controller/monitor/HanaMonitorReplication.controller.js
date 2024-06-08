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

	return BaseController.extend("com.sap.region.manager.ui.controller.monitor.HanaMonitorReplication", {
		formatter: formatter,
		onInit : function () {
			var oViewModel, oTableModel,
				iOriginalBusyDelay,
				oTable = this.byId("table");
			iOriginalBusyDelay = oTable.getBusyIndicatorDelay();
			this._aTableSearchState = [];
			this.region = "primary";
			this.getRouter().getRoute("hanaMonitorReplication").attachPatternMatched(this._onObjectMatched, this);

			oViewModel = new JSONModel({
				worklistTableTitle : this.getResourceBundle().getText("worklistTableTitle"),
				saveAsTileTitle: this.getResourceBundle().getText("saveAsTileTitle", this.getResourceBundle().getText("oViewModelTitle")),
				shareOnJamTitle: this.getResourceBundle().getText("worklistTitle"),
				shareSendEmailSubject: this.getResourceBundle().getText("shareSendEmailWorklistSubject"),
				shareSendEmailMessage: this.getResourceBundle().getText("shareSendEmailWorklistMessage", [location.href]),
				tableNoDataText : this.getResourceBundle().getText("tableNoDataText"),
				tableBusyDelay : 0
			});
			this.setModel(oViewModel, "oViewModel");
			oTableModel = new JSONModel();
			oTableModel.attachRequestCompleted(function() {
				oTable.setBusy(false);
			});
			this.setModel(oTableModel);
			

			oTable.attachEventOnce("updateFinished", function(){
				oViewModel.setProperty("/tableBusyDelay", iOriginalBusyDelay);
			});

			
			this.addHistoryEntry({
				title: this.getResourceBundle().getText("oViewModelTitle"),
				icon: "sap-icon://table-view",
				intent: "#batch-display"
			}, true);
		},
		_onObjectMatched: function (oEvent) {
			this.byId("table").setBusy(true);
			this.getModel().loadData("/api/hanaReplicationStatus?region="+this.region+"&rowSize=500&offset=0");
		},
		onUpdateFinished : function (oEvent) {
			var sTitle,
				oTable = oEvent.getSource(),
				iTotalItems = oEvent.getParameter("total");
			if (iTotalItems && oTable.getBinding("items").isLengthFinal()) {
				sTitle = this.getResourceBundle().getText("statusTableTitleCount", [iTotalItems]);
			} else {
				sTitle = this.getResourceBundle().getText("worklistTableTitle");
			}
			this.getModel("oViewModel").setProperty("/worklistTableTitle", sTitle);
		},

		onObjectShow : function (oEvent) {
			this._showObject(oEvent.getSource());
		},


		/**
		 * Event handler when the share in JAM button has been clicked
		 * @public
		 */
		onShareInJamPress : function () {
			var oViewModel = this.getModel("oViewModel"),
				oShareDialog = sap.ui.getCore().createComponent({
					name: "sap.collaboration.components.fiori.sharing.dialog",
					settings: {
						object:{
							id: location.href,
							share: oViewModel.getProperty("/shareOnJamTitle")
						}
					}
				});
			oShareDialog.open();
		},

		onSearch : function (oEvent) {
			if (oEvent.getParameters().refreshButtonPressed) {
				// Search field's 'refresh' button has been pressed.
				// This is visible if you select any master list item.
				// In this case no new search is triggered, we only
				// refresh the list binding.
				this.onRefresh();
			} else {
				var aTableSearchState = [];
				var sQuery = oEvent.getParameter("query");

				if (sQuery && sQuery.length > 0) {
					aTableSearchState = [new Filter("fileName", FilterOperator.Contains, sQuery)];
				}
				this._applySearch(aTableSearchState);
			}

		},
		
		onFilterSearch: function (oEvent) {
			this._applySearch(oEvent);
		},
		onFilterSelect : function (oEvent) {
			this.region = oEvent.getParameter("key");
			this.onRefresh();
		},		
		onRefresh : function () {
			this.byId("table").setBusy(true);
			this.getModel().loadData("/api/hanaReplicationStatus?region="+this.region+"&rowSize=500&offset=0");
			
		},		
		onUpdateStarted: function(oEvent) {
			//this.getModel().loadData("/api/hanaReplicationStatus?region="+this.region+"&rowSize=5&offset=3");
		},

		_applySearch: function(aTableSearchState) {
			var oTable = this.byId("batchSmartTable"),
				oViewModel = this.getModel("oViewModel");
			this.byId("batchSmartTable").rebindTable(true);
		},

		_parseJSON : function(string){
			try {
			  return JSON.parse(string);
			} catch(ex){
				console.error("ERROR IN PARSING JSON", ex);
			  	return null;
			}
		},

		_showErrorMessage : function(message){
			MessageBox.error(message, 
							{	
							actions: [MessageBox.Action.CANCEL],
							emphasizedAction: MessageBox.Action.CANCEL,
							initialFocus: MessageBox.Action.CANCEL,
							styleClass: "sapUiResponsivePadding--header sapUiResponsivePadding--content sapUiResponsivePadding--footer"
						});
		},
	});
});
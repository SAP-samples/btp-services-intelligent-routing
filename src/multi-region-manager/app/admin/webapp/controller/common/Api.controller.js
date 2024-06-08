sap.ui.define([
	"./BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/model/Sorter",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"../../model/formatter"
], function (BaseController, JSONModel, Sorter, Filter, FilterOperator, formatter) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.common.Api", {

		formatter: formatter,

		
		onInit: function () {
			// Model used to manipulate control states. The chosen values make sure,
			// detail page is busy indication immediately so there is no break in
			// between the busy indication for loading the view's meta data
			var iOriginalBusyDelay,
				oViewModel = new JSONModel({
					busy: true,
					delay: 0,
					invoiceType: ""
				});

			this.getRouter().getRoute("api").attachPatternMatched(this._onObjectMatched, this);

			// Store original busy indicator delay, so it can be restored later on
			iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
			this.setModel(oViewModel, "objectView");
			this.getOwnerComponent().getModel().metadataLoaded().then(function () {
				// Restore original busy indicator delay for the object view
				oViewModel.setProperty("/delay", iOriginalBusyDelay);
			}
			);

		},

		onLinkPressed : function () {
			this.getRouter().navTo("jobList");
		},
		
		_onObjectMatched: function (oEvent) {
			var oArguments = oEvent.getParameter("arguments");
			var message = oArguments.message;
			this.byId("page").setText("Background job is triggered with Job Id: "+message);
		},
	});

});
sap.ui.define([
	"../common/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/model/Sorter",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"../../model/formatter"
], function (BaseController, JSONModel, Sorter, Filter, FilterOperator, formatter) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.batch.Job", {
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

			this.getRouter().getRoute("job").attachPatternMatched(this._onObjectMatched, this);

			// Store original busy indicator delay, so it can be restored later on
			iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
			this.setModel(oViewModel, "jobView");
			this.getOwnerComponent().getModel().metadataLoaded().then(function () {
				// Restore original busy indicator delay for the object view
				oViewModel.setProperty("/delay", iOriginalBusyDelay);
			}
			);

		},

		/* =========================================================== */
		/* event handlers                                              */
		/* =========================================================== */

		onRebindInvTable : function(oEvent){
			var mBindingParams = oEvent.getParameter("bindingParams");
			mBindingParams.sorter.push(new Sorter("STEP_EXECUTION_ID", true));
			this.oBatchStepSmartTable = oEvent.getSource(); 
			// Assign the Smart Table instance
			this.oBatchStepSmartTable = oEvent.getSource();  

			if(this.JOB_EXECUTION_ID) {
				var filters = [];
				var jobExecId = this.JOB_EXECUTION_ID;
				if(jobExecId.length > 0) {
					filters.push(new Filter("JOB_EXECUTION_ID", FilterOperator.EQ, jobExecId));
				}
				if(filters.length >0)
					mBindingParams.filters.push(new Filter(filters, true));
				
			}
		},
		onRefresh : function () {
			this.getView().getElementBinding().refresh();
			this.byId("jobContext").getElementBinding().refresh();
			this.oBatchStepSmartTable.getTable().getBinding("items").refresh();
		},
		onObjectShow : function (oEvent) {
			this._showObject(oEvent.getSource());
		},

		_showObject : function (oItem) {
			this.getRouter().navTo("step", {
				id: oItem.getBindingContext().getProperty("STEP_EXECUTION_ID")
			});
		},

		_onObjectMatched: function (oEvent) {
			var oArguments = oEvent.getParameter("arguments");
			this.JOB_EXECUTION_ID = oArguments.id;
			this.getModel().metadataLoaded().then(function () {
				var sObjectPath = this.getModel().createKey("BatchJobExecutionEx", {
					JOB_EXECUTION_ID: oArguments.id,
				});
				this._bindView("/" + sObjectPath);
				

				var sObjectPath1 = "/"+this.getModel().createKey("BatchJobExecutionContext", {
					JOB_EXECUTION_ID: oArguments.id,
				});
				this.byId("jobContext").bindElement({path: sObjectPath1})
				this.oBatchStepSmartTable.rebindTable(true);
			}.bind(this));



		},

		/**
		 * Binds the view to the object path.
		 * @function
		 * @param {string} sObjectPath path to the object to be bound
		 * @private
		 */
		_bindView: function (sObjectPath) {
			var oViewModel = this.getModel("jobView"),
				oDataModel = this.getModel();
			
			this.getView().bindElement({
				path: sObjectPath,
				parameters: {
					expand: "JOB_INSTANCE"
				},
				events: {
					change: this._onBindingChange.bind(this),
					dataRequested: function () {
						oDataModel.metadataLoaded().then(function () {
							// Busy indicator on view should only be set if metadata is loaded,
							// otherwise there may be two busy indications next to each other on the
							// screen. This happens because route matched handler already calls '_bindView'
							// while metadata is loaded.
							oViewModel.setProperty("/busy", true);
						});
					},
					dataReceived: function () {
						oViewModel.setProperty("/busy", false);
					}
				}
			});
		},

		_onBindingChange: function () {
			var oView = this.getView(),
				oViewModel = this.getModel("jobView"),
				oElementBinding = oView.getElementBinding();

			// No data for the binding
			if (!oElementBinding.getBoundContext()) {
				this.getRouter().getTargets().display("objectNotFound");
				return;
			}

			var oResourceBundle = this.getResourceBundle(),
				oObject = oView.getBindingContext().getObject();

			oViewModel.setProperty("/busy", false);
			oViewModel.setProperty("/shareSendEmailSubject", oResourceBundle.getText("shareSendEmailObjectSubject", [oObject.Vendor_name, oObject.RecordType, oObject.Document_no, oObject.Document_year]));
			oViewModel.setProperty("/shareSendEmailMessage", oResourceBundle.getText("shareSendEmailObjectMessage", [oObject.Vendor_name, location.href]));
		},

		


		

	});

});
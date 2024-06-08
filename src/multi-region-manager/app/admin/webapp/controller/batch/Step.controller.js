sap.ui.define([
	"../common/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/model/Sorter",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"../../model/formatter"
], function (BaseController, JSONModel, Sorter, Filter, FilterOperator, formatter) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.batch.Step", {

		formatter: formatter,

		/* =========================================================== */
		/* lifecycle methods                                           */
		/* =========================================================== */

		/**
		 * Called when the worklist controller is instantiated.
		 * @public
		 */
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

			this.getRouter().getRoute("step").attachPatternMatched(this._onObjectMatched, this);

			// Store original busy indicator delay, so it can be restored later on
			iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
			this.setModel(oViewModel, "objectView");
			this.getOwnerComponent().getModel().metadataLoaded().then(function () {
				// Restore original busy indicator delay for the object view
				oViewModel.setProperty("/delay", iOriginalBusyDelay);
			}
			);

		},

		
		onRefresh : function () {
			this.getView().getElementBinding().refresh();
			this.byId("jobContext").getElementBinding().refresh();
		},
		
		
		_onObjectMatched: function (oEvent) {
			var oArguments = oEvent.getParameter("arguments");
			this.JOB_EXECUTION_ID = oArguments.id;
			this.getModel().metadataLoaded().then(function () {
				var sObjectPath = this.getModel().createKey("BatchStepExecution", {
					STEP_EXECUTION_ID: oArguments.id,
				});
				this._bindView("/" + sObjectPath);
				

				var sObjectPath1 = "/"+this.getModel().createKey("BatchStepExecutionContext", {
					STEP_EXECUTION_ID: oArguments.id,
				});
				this.byId("jobContext").bindElement({path: sObjectPath1})
				
			}.bind(this));



		},

		/**
		 * Binds the view to the object path.
		 * @function
		 * @param {string} sObjectPath path to the object to be bound
		 * @private
		 */
		_bindView: function (sObjectPath) {
			var oViewModel = this.getModel("objectView"),
				oDataModel = this.getModel();
			
			this.getView().bindElement({
				path: sObjectPath,
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
				oViewModel = this.getModel("objectView"),
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
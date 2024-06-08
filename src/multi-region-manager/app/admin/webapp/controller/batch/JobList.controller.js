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

	return BaseController.extend("com.sap.region.manager.ui.controller.batch.JobList", {
		formatter: formatter,

		onInit : function () {
			this.getRouter().getRoute("jobList").attachPatternMatched(this._onObjectMatched, this);			
		},
		_onObjectMatched: function (oEvent) {
			this.onRefresh();
		},		

		onObjectShow : function (oEvent) {
			this.getRouter().navTo("job", {
				id: oEvent.getSource().getBindingContext().getProperty("JOB_EXECUTION_ID")
			});
		},

		onRefresh : function () {
			var oTable = this.byId("jobListTable");
			if(oTable.getBinding("items"))
				oTable.getBinding("items").refresh();
		},

		onBeforeRebindTable: function (oEvent) {
			var mBindingParams = oEvent.getParameter("bindingParams");
			mBindingParams.sorter.push(new Sorter("JOB_EXECUTION_ID", true));
			mBindingParams.parameters.expand = "JOB_INSTANCE";
		},			
		
		onStop : function(oEvent){
			MessageBox.warning("Please be aware that proceeding with this action will pause the current job, resulting in a temporary interruption. \nAre you sure you want to proceed?",
			{
				actions: [MessageBox.Action.OK, MessageBox.Action.CANCEL],
				emphasizedAction: MessageBox.Action.OK,
				onClose: function (sAction) {
					if(sAction == MessageBox.Action.OK) {
						var jobId = oEvent.getSource().getBindingContext().getObject().JOB_EXECUTION_ID;
						var url = "/job/stopJob"+"?jobId="+jobId
						var aData = jQuery.ajax({
							context: this,
							type : "GET",
							contentType : "text/plain",
							url : url,
							async: false, 
							success : function(data,textStatus, jqXHR) {
								sap.m.MessageToast.show(data, {duration: 3000});
								this.onRefresh();			
							}
						});
					}
				}.bind(this)
			}
		,this);				
		},
		onRestart : function(oEvent){
			MessageBox.warning("Please note that proceeding with this action will resume the existing job from its last point, potentially causing a temporary interruption. \nAre you sure you want to proceed?",
				{
					actions: [MessageBox.Action.OK, MessageBox.Action.CANCEL],
					emphasizedAction: MessageBox.Action.OK,
					onClose: function (sAction) {
						if(sAction == MessageBox.Action.OK) {
							var jobId = oEvent.getSource().getBindingContext().getObject().JOB_EXECUTION_ID;
							var url = "/job/restartJob"+"?jobId="+jobId
							var aData = jQuery.ajax({
								context: this,
								type : "GET",
								contentType : "text/plain",
								url : url,
								async: false, 
								success : function(data,textStatus, jqXHR) {
									MessageToast.show(data, {duration: 3000});	
									this.onRefresh();	
								}
							});
						}
					}.bind(this)
				}
			,this);	
		}		
	});
});
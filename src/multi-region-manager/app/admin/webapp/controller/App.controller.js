sap.ui.define([
	"./common/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageBox",
	'sap/ui/core/Fragment',
	"../model/formatter",
], function (BaseController, JSONModel, MessageBox, Fragment, formatter) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.App", {
		formatter: formatter,
		onInit : function () {
			var oViewModel,
				fnSetAppNotBusy,
				iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
			oViewModel = new JSONModel({
				replicationRegion :"primary",
				switchRegion:"secondary",
				notes :"",
				busy : true,
				delay : 0
			});
			this.setModel(oViewModel, "appView");

			
			this.oModel = new JSONModel();
			this.oModel.loadData(sap.ui.require.toUrl("com/sap/region/manager/ui/model/sidebar.json"), null, false);
			this.setModel(this.oModel, "sidebar");
			// var regionsModel = new JSONModel();
			// regionsModel.loadData("/api/regionNames");
			// sap.ui.getCore().setModel(regionsModel,"regions");
			// this.setModel(regionsModel,"regions")
			fnSetAppNotBusy = function() {
				oViewModel.setProperty("/busy", false);
				oViewModel.setProperty("/delay", iOriginalBusyDelay);
			};

			// disable busy indication when the metadata is loaded and in case of errors
			this.getOwnerComponent().getModel().metadataLoaded().
				then(fnSetAppNotBusy);
			this.getOwnerComponent().getModel().attachMetadataFailed(fnSetAppNotBusy);

			// apply content density mode to root view
			this.getView().addStyleClass(this.getOwnerComponent().getContentDensityClass());
		},
		
		onItemSelect : function(oEvent) {
			var item = oEvent.getParameter('item');
			var parentKey = item.getKey();
			var key = item.getKey();
			if(item.getLevel()!=0) {
				parentKey = item.getParent().getKey();
			}else {
				if(item.getExpanded()) {
					item.setExpanded(false);
				}else {
					item.setExpanded(true);
				}
			}
			switch(parentKey){
				case "home":
					this.getRouter().navTo("home")
				break;
				case "job":
					this.getRouter().navTo("jobList")
				break;
				case "switch":
					if(item.getLevel()!=0) {
						if(key == "switchRegion") {
							if (!this.switchRegion) {
								var oView = this.getView();
								var oModel = this.getModel("appView");								
								this.switchRegion = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.SwitchRegion",
									controller: { 
										onOk: function(oEvent) { 
											var switchRegion = oModel.getProperty("/switchRegion");
											var notes = oModel.getProperty("/notes");
											var switchRegionMonitor = oView.byId("switchRegionMonitor").getSelected();
											var url = "";
											if(switchRegion=="primary") {
												url = "/job/"+key+"?region="+switchRegion+"&priority=1"; 
												oModel.setProperty("/replicationRegion","secondary")
											}else {
												url = "/job/"+key+"?region="+switchRegion+"&priority=3"; 
												oModel.setProperty("/replicationRegion","primary")
											}
											if(switchRegionMonitor){
												url = url+"&monitor=true"
											} else {
												url = url+"&monitor=false"
											}
											url = url+"&notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.switchRegion.then(function (oDialog) {
								oDialog.open();
							});		
						}else if(key == "stopAllOperations"){
							if (!this.stopAllOperations) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.stopAllOperations = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.StopAllOperations",
									controller: { 
										onOk: function(oEvent) { 
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.stopAllOperations.then(function (oDialog) {
								oDialog.open();
							});
						} else if(key == "startAllOperations") {
							if (!this.startAllOperations) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.startAllOperations = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.StartAllOperations",
									controller: { 
										onOk: function(oEvent) { 
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.startAllOperations.then(function (oDialog) {
								oDialog.open();
							});
						} else if(key == "startReplication") {
							if (!this.startReplication) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.startReplication = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.StartReplication",
									controller: { 
										onOk: function(oEvent) { 
											var notes = oModel.getProperty("/notes");
											var replicationRegion = oModel.getProperty("/replicationRegion");
											var url = "/job/"+key+"?notes="+notes;
											var url = "/job/hanaRemoteSubscription?region="+replicationRegion+"&action=create&notes="+notes;

											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.startReplication.then(function (oDialog) {
								oDialog.open();
							});
						}else {
							this.getRouter().navTo(key);
						}
					}
				break;	
				case "traffic":
					if(item.getLevel()!=0) {
						if(key == "tmRoutingPriority") {
							if (!this.tmRoutingPriority) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.tmRoutingPriority = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.TrafficRoutingPriorityChange",
									controller: { 
										onOk: function(oEvent) { 
											var region = oView.byId("trafficRegion").getSelectedItem().getKey();
											var priority = 3
											if(region == "primary") {
												priority = 1;
											}
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?priority="+priority+"&notes="+notes; 
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.tmRoutingPriority.then(function (oDialog) {
								oDialog.open();
							});						
						} else if(key == "tmAllRoutingChange") {
							if (!this.trafficRoutingChange) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.trafficRoutingChange = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.TrafficRoutingStatusChange",
									controller: { 
										onOk: function(oEvent) { 
											var trafficStatus = oView.byId("trafficStatus").getSelectedItem().getKey();
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?trafficStatus="+trafficStatus+"&notes="+notes; 
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.trafficRoutingChange.then(function (oDialog) {
								oDialog.open();
							});		
						} 
				}
				break;				
				case "hana":
					if(item.getLevel()!=0) {
						if(key == "hanaRemoteSubscription") {
							if (!this.hanaRemoteSubscription) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.hanaRemoteSubscription = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.HanaRemoteSubscription",
									controller: { 
										onOk: function(oEvent) { 
											var region = oView.byId("hanaRegion").getSelectedItem().getKey();
											var action = oView.byId("hanaAction").getSelectedItem().getKey();
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?region="+region+"&action="+action+"&notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							
							this.hanaRemoteSubscription.then(function (oDialog) {
								oDialog.open();
							});						
						} else if(key == "hanaMonitorReplication") {
							this.getRouter().navTo("hanaMonitorReplication");
						}
					}
				break;
				case "ci":
					if(item.getLevel()!=0) {
						if(key == "ciArtifacts") {
							if (!this.ciArtifacts) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.ciArtifacts = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.CIArtifacts",
									controller: { 
										onOk: function(oEvent) { 
											var region = oView.byId("ciRegion").getSelectedItem().getKey();
											var action = oView.byId("ciAction").getSelectedItem().getKey();
											var packageIds = oView.byId("ciPackageIds").getValue();
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?region="+region+"&action="+action+"&notes="+notes+"&packageIds="+packageIds;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							
							this.ciArtifacts.then(function (oDialog) {
								oDialog.open();
							});						
						} else {
							this.getRouter().navTo(key);
						}
					}
				break;
				case "aem":
					if(item.getLevel()!=0) {
						if(key == "aemReplicationRoleChange") {
							if (!this.aemReplicationState) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.aemReplicationState = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.AEMReplicationRole",
									controller: { 
										onOk: function(oEvent) { 
											var region = oView.byId("aemRegion").getSelectedItem().getKey();
											var replicationRole = oView.byId("aemReplicationRole").getSelectedItem().getKey();
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?region="+region+"&replicationRole="+replicationRole+"&notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							
							this.aemReplicationState.then(function (oDialog) {
								oDialog.open();
							});
						} else if(key == "aemAllStandby") {
							if (!this.aemAllStandby) {
								var oView = this.getView();
								var oModel = this.getModel("appView");
								this.aemAllStandby = Fragment.load({
									id: oView.getId(),
									name: "com.sap.region.manager.ui.view.fragment.AemAllStandby",
									controller: { 
										onOk: function(oEvent) { 
											var notes = oModel.getProperty("/notes");
											var url = "/job/"+key+"?notes="+notes;
											oEvent.getSource().getParent().close();
											oView.getController().callApi(url)
										},
										onCancel: function(oEvent) {
											oEvent.getSource().getParent().close()
										} 
									}
								}).then(function (oDialog) {
									oView.addDependent(oDialog);
									return oDialog;
								});
							}				
							this.aemAllStandby.then(function (oDialog) {
								oDialog.open();
							});							   	
						} else if(key == "aemMonitorReplication") {
							this.getRouter().navTo("aemMonitorReplication");
						 }
					}
				break;	
				case "performance":
					if(item.getLevel()!=0) {
						if(key == "hanaPerformance") {
							this.getRouter().navTo("hanaPerformance");
						} else {
							this.getRouter().navTo(key);
						}
					}
				break;	
			}			
		},

		callApi: function(url) {
			var router = this.getRouter();
			var aData = jQuery.ajax({
				type : "GET",
				contentType : "text/plain",
				url : url,
				async: false, 
				success : function(data,textStatus, jqXHR) {
					router.navTo("api",{
						message : data
					})
				},
				error: function(data,textStatus, jqXHR) {
					MessageBox.error("Oops! Something went wrong. Please contact the administrator for assistance.",{
						title: "API Call Error",
						details: data.responseText
					},this);	
						
					
				}
			});
			return "";  
		},

		onMenuButtonPress : function() {
			var toolPage = this.byId("toolPage");
			toolPage.setSideExpanded(!toolPage.getSideExpanded());
		},

		navBack :function(oEvent) {
			window.history.go(-1);
		}
	});

});
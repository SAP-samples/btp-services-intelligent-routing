sap.ui.define([
	"./BaseController"
], function (BaseController) {
	"use strict";

	return BaseController.extend("com.sap.region.manager.ui.controller.common.NotFound", {
		onLinkPressed : function () {
			this.getRouter().navTo("jobList");
		}
	});
});
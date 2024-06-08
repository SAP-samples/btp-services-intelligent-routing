sap.ui.define(
  [
    "../common/BaseController",
    "sap/ui/model/json/JSONModel",
    "sap/viz/ui5/data/FlattenedDataset",
    "sap/viz/ui5/controls/common/feeds/FeedItem",
    "sap/m/Label",
    "sap/m/ColumnListItem",
    "sap/m/library",
    "sap/m/Column",
    "sap/viz/ui5/controls/common/feeds/AnalysisObject",
    "sap/viz/ui5/controls/VizFrame",
    "sap/m/Table",
    "sap/ui/core/Item",
    "sap/suite/ui/commons/ChartContainerContent",
    "../../model/performance",
    "../../model/formatter",
  ],
  function (
    BaseController,
    JSONModel,
    FlattenedDataset,
    FeedItem,
    Label,
    ColumnListItem,
    MobileLibrary,
    Column,
    AnalysisObject,
    VizFrame,
    Table,
    Item,
    ChartContainerContent,
    performance,
    formatter
  ) {
    "use strict";

    return BaseController.extend(
      "com.sap.region.manager.ui.controller.performance.AllPerformance",
      {
        formatter: formatter,
        onInit: function () {
          var oChartContainer = this.getView().byId("sda");
          this.chartContainerContents = [];
          var oSelect = new sap.m.Select();
          var oSelect = this.getView().byId("dimensionSelector");
          performance.overview(formatter).forEach(function (config) {
            var oVizFrame = this._createVizFrameContent(config.vizFrame);
            var oTable = this._createTableContent(config.table);
            this.chartContainerContents[config.id] = [oVizFrame, oTable];
            oSelect.addItem(
              new Item({
                key: config.id,
                text: config.vizFrame.title,
              })
            );
          }, this);
          this._updateChartContainerContent("overview");
        },
        onRefresh : function (oEvent) {
          var oChartContainer = this.getView().byId("sda");
          var data = oChartContainer.getContent()[0].getContent().getDataset().mBindingInfos.data;
          oChartContainer.getContent()[0].getContent().getDataset().bindData(data);
          oChartContainer.getContent()[1].getContent().getBinding("items").refresh();
        },
        handleSelectionChange: function (oEvent) {
          var oItem = oEvent.getParameter("selectedItem");
          this._updateChartContainerContent(oItem.getKey());
        },
        _updateChartContainerContent: function (contentKey) {
          var chartContainerContent = this.chartContainerContents[contentKey];
          var oChartContainer = this.getView().byId("sda");
          oChartContainer.removeAllContent();
          oChartContainer.addContent(chartContainerContent[0]);
          oChartContainer.addContent(chartContainerContent[1]);
          oChartContainer.updateChartContainer();
        },
        _createVizFrameContent: function (vizFrameConfig) {
          var oVizFrame = new VizFrame(vizFrameConfig.config);
          oVizFrame.setVizProperties({
            title: {
              text: vizFrameConfig.title,
            },
            plotArea: {
              gridline: {
                visible: false
              },
              dataLabel: {
                visible: true,
                style: {
                  fontWeight: 'bold'
                },
                hideWhenOverlap: true
              }
            }
          });
          var oDataSet = new FlattenedDataset(vizFrameConfig.dataset);
          oVizFrame.setDataset(oDataSet);
          oVizFrame.setModel(this.getModel());
          for (var i = 0; i < vizFrameConfig.feedItems.length; i++) {
            oVizFrame.addFeed(new FeedItem(vizFrameConfig.feedItems[i]));
          }
          oVizFrame.setVizType(vizFrameConfig.vizType);
          var oContent = new ChartContainerContent({
            icon: vizFrameConfig.icon,
            title: vizFrameConfig.title,
          });
          oContent.setContent(oVizFrame);

          return oContent;
        },
        _createTableContent: function (tableConfig) {
          var aLabels = this._createControls(
            Label,
            "text",
            tableConfig.columnLabelTexts
          );
          var oTable = new Table({

            columns: this._createControls(Column, "header", aLabels),
          });
          var oTableTemplate = new ColumnListItem({
            type: MobileLibrary.ListType.Active,
            cells: this._createControls(
              Label,
              "text",
              tableConfig.templateCellLabelTexts
            ),
          });
          var oSorter = new sap.ui.model.Sorter("avgTimeTakenSeconds");
          oTable.bindItems({
            path: tableConfig.itemBindingPath,
            template: oTableTemplate,
            sorter: this._createControls(
              sap.ui.model.Sorter,
              "path",
              tableConfig.sorter
            ),
            parameters: tableConfig.parameters,
          });
          oTable.setModel(this.getModel());
          var oContent = new ChartContainerContent({
            icon: tableConfig.icon,
            title: tableConfig.title,
          });
          oContent.setContent(oTable);
          return oContent;
        },

        _createControls: function (Control, prop, propValues) {
          var aControls = [];
          var oProps = {};
          for (var i = 0; i < propValues.length; i++) {
            oProps[prop] = propValues[i];
            aControls.push(new Control(oProps));
          }
          return aControls;
        },
      }
    );
  }
);

sap.ui.define([], function () {
  "use strict";
  return {
    sda: function (formatter) {
      return [
        {
          id: "create",
          vizFrame: {
            icon: "sap-icon://bubble-chart",
            title: "Create Subscription",
            vizType: "column",
            config: {
              height: "700px",
              width: "100%",
              uiConfig: {
                applicationSet: "fiori",
              },
            },
            dataset: {
              dimensions: [
                {
                  name: "Job Description",
                  value: "{JOB_EXECUTION_DESC}",
                },
                {
                  name: "Region",
                  value: "{PERFORMANCE_TRACE_REGION}",
                },
              ],
              measures: [
                {
                  name: "Avg Time Taken (Minutes)",
                  value: {
                    path: "avgTimeTakenSeconds",
                    formatter: formatter.secondsToMinute,
                  },
                },
              ],
              data: "{ path:'/HanaCreatePerformance', parameters: {select: 'JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds'}, sorter: [{path: 'PERFORMANCE_TRACE_REGION'},{path: 'avgTimeTakenSeconds'}]}",
            },
            feedItems: [
              {
                uid: "primaryValues",
                type: "Measure",
                values: ["Avg Time Taken (Minutes)"],
              },
              {
                uid: "axisLabels",
                type: "Dimension",
                values: ["Region", "Job Description"],
              },
            ],
          },
          table: {
            icon: "sap-icon://table-view",
            title: "Create Subscription",
            itemBindingPath: "/HanaCreatePerformance",
            sorter: ["PERFORMANCE_TRACE_REGION", "avgTimeTakenSeconds"],
            parameters: {
              select:
                "JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds",
            },
            columnLabelTexts: [
              "Region",
              "Job Description",
              "Avg Time Taken (Minutes)",
            ],
            templateCellLabelTexts: [
              "{PERFORMANCE_TRACE_REGION}",
              "{JOB_EXECUTION_DESC}",
              {
                path: "avgTimeTakenSeconds",
                formatter: formatter.secondsToMinute,
              },
            ],
          },
        },
        {
          id: "delete",
          vizFrame: {
            icon: "sap-icon://bubble-chart",
            title: "Delete Subscription",
            vizType: "column",
            config: {
              height: "700px",
              width: "100%",
              uiConfig: {
                applicationSet: "fiori",
              },
            },
            dataset: {
              dimensions: [
                {
                  name: "Job Description",
                  value: "{JOB_EXECUTION_DESC}",
                },
                {
                  name: "Region",
                  value: "{PERFORMANCE_TRACE_REGION}",
                },
              ],
              measures: [
                {
                  name: "Avg Time Taken (Minutes)",
                  value: {
                    path: "avgTimeTakenSeconds",
                    formatter: formatter.secondsToMinute,
                  },
                },
              ],
              data: "{ path:'/HanaDeletePerformance', parameters: {select: 'JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds'}, sorter: [{path: 'PERFORMANCE_TRACE_REGION'},{path: 'avgTimeTakenSeconds'}]}",
            },
            feedItems: [
              {
                uid: "primaryValues",
                type: "Measure",
                values: ["Avg Time Taken (Minutes)"],
              },
              {
                uid: "axisLabels",
                type: "Dimension",
                values: ["Region", "Job Description"],
              },
            ],
          },
          table: {
            icon: "sap-icon://table-view",
            title: "Delete Subscription",
            itemBindingPath: "/HanaDeletePerformance",
            sorter: ["PERFORMANCE_TRACE_REGION", "avgTimeTakenSeconds"],
            parameters: {
              select:
                "JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds",
            },
            columnLabelTexts: [
              "Region",
              "Job Description",
              "Avg Time Taken (Minutes)",
            ],
            templateCellLabelTexts: [
              "{PERFORMANCE_TRACE_REGION}",
              "{JOB_EXECUTION_DESC}",
              {
                path: "avgTimeTakenSeconds",
                formatter: formatter.secondsToMinute,
              },
            ],
          },
        },
      ];
    },
    overview: function (formatter) {
      return [
        {
          id: "overview",
          vizFrame: {
            icon: "sap-icon://bubble-chart",
            title: "Overview",
            vizType: "column",
            config: {
              height: "700px",
              width: "100%",
              uiConfig: {
                applicationSet: "fiori",
              },
            },
            dataset: {
              dimensions: [
                {
                  name: "Step",
                  value: "{STEP_NAME_EX}",
                },
                {
                  name: "Job Description",
                  value: "{JOB_EXECUTION_DESC}",
                },
                {
                  name: "Region",
                  value: "{PERFORMANCE_TRACE_REGION}"
                },
              ],
              measures: [
                {
                  name: "Avg Time Taken (Minutes)",
                  value: {
                    path: "avgTimeTakenSeconds",
                    formatter: formatter.secondsToMinute,
                  },
                },
              ],
              data: "{ path:'/OverallPerformance', parameters: {select: 'STEP_NAME_EX,JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds'}, sorter: [{path: 'PERFORMANCE_TRACE_REGION'},{path: 'avgTimeTakenSeconds'}]}",
            },
            feedItems: [
              {
                uid: "primaryValues",
                type: "Measure",
                values: ["Avg Time Taken (Minutes)"],
              },
              {
                uid: "axisLabels",
                type: "Dimension",
                values: ["Step","Job Description"],
              },
              {
                uid: "color",
                type: "Dimension",
                values: ["Region"],
              },
            ],
          },
          table: {
            icon: "sap-icon://table-view",
            title: "Overview",
            itemBindingPath: "/OverallPerformance",
            sorter: ["PERFORMANCE_TRACE_REGION", "STEP_NAME_EX","avgTimeTakenSeconds"],
            parameters: {
              select:
                "STEP_NAME_EX,JOB_EXECUTION_DESC,PERFORMANCE_TRACE_REGION,avgTimeTakenSeconds",
            },
            columnLabelTexts: [
              "Region",
              "Job Description",
              "Step",
              "Avg Time Taken (Minutes)",
            ],
            templateCellLabelTexts: [
              "{PERFORMANCE_TRACE_REGION}",
              "{JOB_EXECUTION_DESC}",
              "{STEP_NAME_EX}",
              {
                path: "avgTimeTakenSeconds",
                formatter: formatter.secondsToMinute,
              },
            ],
          },
        },
      ];
    },
  };
});

var menuData = [
    {
        name: "All Apps",
        notebooks: ['2BFX9NR3Y', '2BFF7MCKJ', '2BGYFQD1D'] //first one is for logs second one is for api analytics and so on
    },
    {
        name: "AMPS",
        notebooks: ['2BK1ARANT', '2BHKK7N9P', '2BGYFQD1D']
        //notebooks:['2BK1ARANT', '2BHKK7N9P', '2BHJSJP9W'] //first one is for logs second one is for api analytics and so on
    },
    {
        name: "tapps",
        notebooks: ['2BHBBZ9AX', '2BGAZ47EB', '2BGYFQD1D']
        // notebooks:['2BHBBZ9AX', '2BGAZ47EB', '2BGGWJ9PT'] //first one is for logs second one is for api analytics and so on
    }
]
var colSettings = {
    "2BFX9NR3Y": [{ field: "summary", width: '*', wordWrap: true, cellTooltip: true, enableHiding: false, cellTemplate : '<div ng-bind-html="row.entity[col.field]"></div>' }],
    "2BK1ARANT": [{ field: "summary", width: '*', wordWrap: true, cellTooltip: true, enableHiding: false, cellTemplate : '<div ng-bind-html="row.entity[col.field]"></div>' }],
    "2BHBBZ9AX": [{ field: "summary", width: '*', wordWrap: true, cellTooltip: true, enableHiding: false, cellTemplate : '<div ng-bind-html="row.entity[col.field]"></div>' }],
     "3BFX9NR3Y": [
        { name: "Date", field: "Date", displayName: "Date", width: "13%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ReqId", field: "ReqId", displayName: "Request Id", width: "18%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "HttpMethod", field: "HttpMethod", displayName: "Method", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Request", field: "Request", displayName: "Request", width: "15%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ResponseTime", field: "ResponseTime", displayName: "Time (ms)", width: "5%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "StatusCode", field: "StatusCode", displayName: "Res Code", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Response", field: "Response", displayName: "Response", width: "35%", wordWrap: true, cellTooltip: true, enableHiding: false }
    ],
    "3BK1ARANT": [
        { name: "Date", field: "Date", displayName: "Date", width: "13%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ReqId", field: "ReqId", displayName: "Request Id", width: "18%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "HttpMethod", field: "HttpMethod", displayName: "Method", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Request", field: "Request", displayName: "Request", width: "15%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ResponseTime", field: "ResponseTime", displayName: "Time (ms)", width: "5%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "StatusCode", field: "StatusCode", displayName: "Res Code", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Response", field: "Response", displayName: "Response", width: "35%", wordWrap: true, cellTooltip: true, enableHiding: false }
    ],
    "3BHBBZ9AX": [
        { name: "Date", field: "Date", displayName: "Date", width: "13%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ReqId", field: "ReqId", displayName: "Request Id", width: "18%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "HttpMethod", field: "HttpMethod", displayName: "Method", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Request", field: "Request", displayName: "Request", width: "15%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "ResponseTime", field: "ResponseTime", displayName: "Time (ms)", width: "5%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "StatusCode", field: "StatusCode", displayName: "Res Code", width: "7%", wordWrap: true, cellTooltip: true, enableHiding: false },
        { name: "Response", field: "Response", displayName: "Response", width: "35%", wordWrap: true, cellTooltip: true, enableHiding: false }
    ]
};
var refreshInterval = 10000;
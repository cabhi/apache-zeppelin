angular.module("zeppelinWebApp").controller("sidebarCtrl", ["$scope", "$rootScope", "$routeParams", "$location", "notebookListDataFactory", "websocketMsgSrv", "arrayOrderingSrv", function (a, b, c, d, e, f, g) {
    var j = this;
    j.switchMenuType = fn;
    function fn(menuType) {
        b.$broadcast("menuChanged", menuType);
    }
}]);
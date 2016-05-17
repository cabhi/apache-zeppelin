'use strict';

angular.module('zeppelinWebApp')
    .directive('filterStrip', function () {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            scope: { filters: '=', operators: '=', columns: '=' },
            templateUrl: 'components/filter-strip/filter-strip.html',
            controller: "filterStripCtrl"
        };
    });
angular.module('zeppelinWebApp').controller('filterStripCtrl', function ($scope, $route, $routeParams, $location, $rootScope) {
    $scope.saveFilter = saveFilter;
    $scope.removeFilter = removeFilter;

    function saveFilter(column, operator, operand) {
        if (!$scope.filters) {
            $scope.filters = [];
        }
        $scope.filters.push({ column: column, operator: operator, operand: operand });
    };

    function removeFilter(filter) {
        $scope.filters = _.remove($scope.filters, function (n) {
            return !(filter.$$hashKey === n.$$hashKey);
        });
    };
});
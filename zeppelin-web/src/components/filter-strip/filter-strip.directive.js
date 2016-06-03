'use strict';

angular.module('zeppelinWebApp')
  .directive('filterStrip', function () {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        filters: '=',
        operators: '=',
        columns: '=',
        filterChanged: '&onFilterChange'
      },
      templateUrl: 'components/filter-strip/filter-strip.html',
      controller: "filterStripCtrl"
    };
  });
angular.module('zeppelinWebApp').controller('filterStripCtrl', function ($scope, $route, $routeParams, $location, $rootScope) {
  $scope.filter = {};
  $scope.saveFilter = saveFilter;
  $scope.removeFilter = removeFilter;
  $scope.toggleForm = toggleForm;
  $scope.acceptInput = false;

  var isEmpty = _.flow(_.trim, _.isEmpty);

  $scope.isEmptyFilter = function () {
    var filter = $scope.filter;
    return isEmpty(filter.column) || isEmpty(filter.operator);
  };

  function saveFilter() {
    if (!$scope.filters) {
      $scope.filters = [];
    }
    var newFilter = $scope.filter,
      oldFilter = _.find($scope.filters, function (filter) {
        return filter.column === newFilter.column;
      });
    if (oldFilter) {
      oldFilter.operator = newFilter.operator;
      oldFilter.operand = newFilter.operand;
    } else {
      $scope.filters.push(newFilter);
    }
    $scope.filterChanged();
    $scope.toggleForm();
  }

  function removeFilter(filter, $event) {
    $event.stopPropagation();
    $scope.filters = _.remove($scope.filters, function (n) {
      return !(filter.$$hashKey === n.$$hashKey);
    });
    $scope.filterChanged();
  }

  function toggleForm() {
    $scope.filter = {};
    $scope.acceptInput = !$scope.acceptInput;
  }
});

'use strict';

angular.module('zeppelinWebApp')
  .directive('selectAndSort', function () {
    return {
      restrict: 'E',
      templateUrl: 'components/select-and-sort/select-and-sort.html',
      scope: {
        originalItems: '=items',
        update: '&onUpdate'
      },
      link: function ($scope, $element, $attrs) {
        $scope.items = angular.copy($scope.originalItems);
        $scope.$watch('originalItems', function (newItems) {
          $scope.items = angular.copy(newItems);
        });
        $scope.save = function () {
          $scope.update({items: $scope.items});
        };
      }
    };
  });

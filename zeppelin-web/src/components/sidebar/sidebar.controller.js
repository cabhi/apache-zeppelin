'use strict';

angular.module("zeppelinWebApp")
  .controller("sidebarCtrl", function ($scope, $rootScope, $stateParams, $location, notebookListDataFactory) {
    $scope.nodes = notebookListDataFactory.categories;

    $scope.isRouteActive = function (node) {
      var notebookId = $stateParams.noteId;
      if (node.isRoot) {
        return !!_.find(node.children, function (leaf) {
          return leaf.id === notebookId;
        });
      }
      return node.id === notebookId;
    };

    $scope.$watchCollection(function () {
      return notebookListDataFactory.root.children;
    }, function (notes) {
      if (notes && notes.length > 0) {
        extractAndConvert(notes);
        var firstNote = $scope.nodes[0] && $scope.nodes[0].children && $scope.nodes[0].children[0];
        if (firstNote) {
          $rootScope.firstNoteId = firstNote.id;
          $rootScope.$broadcast('firstNoteId', firstNote.id);
        }
      }
    });

    $scope.setFirstLeafActive = function (node) {
      var firstLeafNode = node.children && node.children[0];
      if (firstLeafNode) {
        $location.path('/notebook/' + firstLeafNode.id);
      }
    };

    $rootScope.$on('$routeChangeStart', function(event, next, current) {
      angular.element('.nav-node').removeClass('active');
    });

    function extractAndConvert(nodes) {
      _.each(nodes, function (node) {
        if (node.children && node.children.length > 0) {
          addInCategory(node.children, node.name);
        }
      });
    }

    function addInCategory(nodes, category) {
      var categoryNode = _.find($scope.nodes, {code: category}) || {};
      categoryNode.isRoot = true;
      categoryNode.children = removeDuplicates((categoryNode.children || []).concat(nodes));
    }

    function removeDuplicates(nodes) {
      var _unique = {};
      return _.filter(nodes, function (node) {
        _unique[node.id] = !_unique[node.id];
        return _unique[node.id];
      });
    }
  });

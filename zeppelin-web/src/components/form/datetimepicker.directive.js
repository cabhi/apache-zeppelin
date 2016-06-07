'use strict';

angular.module('zeppelinWebApp')
  .provider('datetimepicker', function () {
    var default_options = {
      useCurrent: false,
      format: 'MM/DD/YYYY hh:mm:ss A'
    };

    this.setOptions = function (options) {
      default_options = options;
    };

    this.$get = function () {
      return {
        getOptions: function () {
          return default_options;
        }
      };
    };
  })
  .directive('datetimepicker', ['$timeout', 'datetimepicker', function ($timeout, datetimepicker) {
    var default_options = datetimepicker.getOptions();

    var parsePatterns = function (value) {
      var tokens = /(now)(?:((?:-|\+)[0-9]+)([dhms]))*/.exec(value) || [],
        m;
      if (tokens[1] === 'now') {
        m = moment();
      }
      if (tokens[2] && tokens[3]) {
        m.add(+tokens[2], tokens[3]);
      }
      return (m && m.toDate()) || value;
    };

    return {
      require: '?ngModel',
      restrict: 'A',
      scope: {
        options: '@'
      },
      link: function ($scope, $element, $attrs, ngModelCtrl) {
        var passed_in_options = $scope.$eval($attrs.options);
        var options = jQuery.extend({}, default_options, passed_in_options);

        $element.on('dp.hide', function (e) {
          if (ngModelCtrl && (+new Date(e.target.value) !== +new Date(ngModelCtrl.$viewValue))) {
            $timeout(function () {
              ngModelCtrl.$setViewValue(new Date(e.target.value));
            });
          }
        });

        var unregister = $scope.$watch(function () {
          return ngModelCtrl.$modelValue;
        }, initialize);

        function initialize(value) {
          ngModelCtrl.$setViewValue(parsePatterns(value));
          $element.datetimepicker(options);

          if (ngModelCtrl) {
            ngModelCtrl.$render = function () {
              setPickerValue();
            };
          }
          setPickerValue();
          unregister();
        }

        function setPickerValue() {
          var date = options.defaultDate || null;
          if (ngModelCtrl && ngModelCtrl.$viewValue) {
            date = ngModelCtrl.$viewValue;
          }
          $element.data('DateTimePicker').date(date && new Date(date));
        }
      }
    };
  }]);

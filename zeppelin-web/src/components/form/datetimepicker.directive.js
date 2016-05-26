'use strict';

angular.module('zeppelinWebApp')
    .provider('datetimepicker', function () {
        var default_options = {};

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
        return {
            require: '?ngModel',
            restrict: 'A',
            scope: {
                options: '@'
            },
            link: function ($scope, $element, $attrs, ngModelCtrl) {
                var passed_in_options = $scope.$eval($attrs.options);
                var options = jQuery.extend({}, default_options, passed_in_options);
                $element.on('dp.change', function (e) {
                        if (ngModelCtrl) {
                            $timeout(function () {
                                ngModelCtrl.$setViewValue(new Date(e.target.value));
                            });
                        }
                    })
                    .datetimepicker(options);

                function setPickerValue() {
                    var date = options.defaultDate || null;

                    if (ngModelCtrl && ngModelCtrl.$viewValue) {
                        date = ngModelCtrl.$viewValue;
                    }

                    $element.data('DateTimePicker').date(date);
                }

                if (ngModelCtrl) {
                    ngModelCtrl.$render = function () {
                        setPickerValue();
                    };
                }

                setPickerValue();
            }
        };
    }]);

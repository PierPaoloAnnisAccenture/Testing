var exec = require('cordova/exec');

exports.init = function (success, error,sdkKey,sdkSecret,sdkEndpoint,enableNetwork) {
    exec(success, error, 'BlueGPS', 'initializeSDK', [sdkKey,sdkSecret,sdkEndpoint,enableNetwork]);
};


exports.openMapBlock = function (success, error,tagID,style,showMap, heightTopJS, origin, destination) {
    exec(success, error, 'BlueGPS', 'openMapBlock', [tagID,style,showMap, screen.height, heightTopJS, origin, destination]);
};

exports.refreshBlock = function (success, error) {
    exec(success, error, 'BlueGPS', 'refreshBlock');
};

exports.startNavigationBlock = function (success, error, origin, destination) {
    exec(success, error, 'BlueGPS', 'startNavigationBlock', [origin, destination]);
};

exports.refreshHeightBlock = function (success, error, heightJSWithKeyboard, heightJSTop) {
    exec(success, error, 'BlueGPS', 'refreshHeightBlock', [heightJSWithKeyboard, heightJSTop]);
};

exports.currentFloor = function (success, error) {
    exec(success, error, 'BlueGPS', 'currentFloor');
};

exports.gotoFloor = function (success, error, floorId) {
    exec(success, error, 'BlueGPS', 'gotoFloor', [floorId]);
};

exports.closeBlock = function (success, error) {
    exec(success, error, 'BlueGPS', 'closeBlock');
};


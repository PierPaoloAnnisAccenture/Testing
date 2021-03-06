var exec = require('cordova/exec');

exports.initSecret = function (success, error,sdkKey,sdkSecret,sdkEndpoint,enableNetwork) {
    exec(success, error, 'BlueGPS', 'initializeSDK', [sdkKey,sdkSecret,sdkEndpoint,enableNetwork]);
};

exports.initToken = function (success, error,sdkToken,sdkEndpoint) {
    exec(success, error, 'BlueGPS', 'initializeToken', [sdkToken,sdkEndpoint]);
};

exports.openMapBlock = function (success, error,configurationMap, heightJSTop, origin, destination) {
    exec(success, error, 'BlueGPS', 'openMapBlock', [configurationMap,screen.height, heightJSTop, origin, destination]);
};

exports.refreshBlock = function (success, error) {
    exec(success, error, 'BlueGPS', 'refreshBlock');
};

exports.refreshToken = function (success, error,sdkToken) {
    exec(success, error, 'BlueGPS', 'refreshToken', [sdkToken]);
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
//TODO to remove
exports.openMap = function (success, error,tagID,style,showMap) {
    console.log("Open Map" );
    exec(success, error, 'BlueGPS', 'openMap', [tagID,style,showMap]);
};


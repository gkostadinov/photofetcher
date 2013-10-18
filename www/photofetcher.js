var exec = require('cordova/exec');

module.exports = {
    fetch: function(resultCallback) {
        exec(resultCallback, null, "PhotoFetcher", "fetch", []);
    }
};
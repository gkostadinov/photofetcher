var exec = require('cordova/exec');

var splashscreen = {
    fetch: function() {
        exec(null, null, "PhotoFetcher", "execute", []);
    }
};

module.exports = splashscreen;
/**
 * The LMZ widget is able to parse the loaded information and make it available
 * to its callers.
 */
(function($, undefined) {

    $.widget("UOA.lmzWidget", {

        /**
         * Default option values
         */
        options : {

            //
            // default selectors for widget
            //
            sel : {
                base : "> .widget-wrapper[data-base-url]",
                wrapper : "> .widget-wrapper",
                dataElements : "> .widget-wrapper > div > .widget"
            }
        },

        /**
         * Initialize data-members
         *
         * @private
         */
        _create : function() {
            this.element = $(this.element);
            this.dataElements = this._readDataElements();
        },

        /**
         * Initialize the widget by calling the bootstrap callback specified
         * in data-bootstrap.
         */
        initialize : function() {
            var bootName = this.dataElements.bootstrap;
            if (bootName) {
                eval(bootName + "(this.element);");
            }
        },

        /**
         * @return the remote URL
         */
        getRemoteUrl : function() {
            return this.element.find(this.options.sel.wrapper).data("base-url");
        },

        /**
         * @returns {*} a list of CSS files or an empty list when none are found
         */
        getStyleUrls : function() {
            if (this.dataElements['assets-styles']) {
                return JSON.parse(this.dataElements['assets-styles']);
            }
            return [];
        },

        /**
         * Normalize the javascript list by adding the remote url if necessary
         *
         * @param list is the list to normalize
         * @returns {Array} a normalized version
         * @private
         */
        _normalizeJavascriptList: function (list) {

            // save current scope
            var thiz = this;

            // make sure we have proper absolute paths.
            var normalizedList = [];
            $.each(list, function (key, jsLocation) {

                // domainless file? add widget location
                if (jsLocation.indexOf("/") === 0 && jsLocation.indexOf("//") !== 0) {
                    normalizedList.push(thiz.getRemoteUrl() + jsLocation);
                } else {
                    normalizedList.push(jsLocation);
                }

            });

            return normalizedList;
        },

        /**
         * @returns {*} a list of javascript files that need to be loaded.
         */
        getJavascriptUrls : function() {
            // has scripts?
            if (this.dataElements['assets-scripts']) {

                // parse json string
                var list = JSON.parse(this.dataElements['assets-scripts']);
                return this._normalizeJavascriptList(list);
            }
            return [];
        },

        /**
         * Extract data values into a map.
         * @private
         */
        _readDataElements : function() {
            var dataNode = this.element.find(this.options.sel.dataElements);

            var output = {};
            _.each(dataNode[0].attributes, function(attr) {
                if (attr.name.indexOf("data-") === 0) {
                    output[attr.name.substring("data-".length)] = attr.value;
                }
            });

            return output;
        }

    });

})(UOA.jQuery);
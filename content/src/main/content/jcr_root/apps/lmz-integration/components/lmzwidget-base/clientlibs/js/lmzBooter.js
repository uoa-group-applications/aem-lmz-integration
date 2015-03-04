(function($, undefined) {

    $.widget("UOA.lmzBooter", {

        options : {

        },

        /**
         * Number of required CSS files that are to be loaded
         */
        nRequiredFiles : 0,
        nLoadedFiles : 0,

        /**
         * Initialize data-members
         * @private
         */
        _create : function() {

            var
                thiz = this,
                allWidgets = $(".lmzwidget-base > .widget-wrapper[data-base-url!='']"),
                allCss =
                    this.merge(
                        this.concat(allWidgets, function(el) { return el.getStyleUrls(); })
                    ),
                allJs =
                    this.merge(
                        this.concat(allWidgets, function(el) { return el.getJavascriptUrls(); })
                    );

            this.nRequiredFiles = allCss.length + allJs.length;

            // load all stylesheets
            $.each(allCss, function(key, css) {
                thiz.loadStylesheet(css);
            });

            // load all javascripts
            $.each(allJs, function(key, js) {
                thiz.loadJavascript(js);
            });

        },

        /**
         * Callback method that is called when all the files have been completely loaded
         */
        completedFileLoading : function() {

            // show the content
            $(".lmzwidget-base .widget-wrapper > div").attr("data-loading", "false");

            // initialize the widgets
            $(".lmzwidget-base").each(function() {
                if ($(this).data("lmzWidget")) {
                    $(this).data("lmzWidget").initialize();
                }
            });

        },

        /**
         * Called when one of the files emits the onload
         */
        anotherFileDone : function(file) {
            ++this.nLoadedFiles;

            if (this.nLoadedFiles === this.nRequiredFiles) {
                this.completedFileLoading();
            }
        },

        /**
         * This method loads a stylesheet at runtime
         *
         * @param location is the location to load the stylesheet from
         */
        loadStylesheet : function(location) {
            var thiz = this,
                linkElement = $("<link />", {
                    href : location,
                    rel : "stylesheet",
                    type : "text/css"
                });

            linkElement[0].onload = function() {
                thiz.anotherFileDone(location);
            };

            $("head").append(linkElement);
        },

        /**
         * This method loads a javascript at runtime
         *
         * @param location URL from which to load Javascript
         */
        loadJavascript : function(location) {
            var thiz = this;
            try {
                $.getScript(location, function() {
                    thiz.anotherFileDone(location);
                });
            }
            catch (err) {
                console.error("An error occured, still counting is as loaded", err);
                thiz.anotherFileDone("err:" + location);
            }
        },

        /**
         * Concatenate the array results of the elements in `all` returned from
         * the function call with name `functionName`
         *
         * @param all
         * @param functionName
         */
        concat : function(all, callback) {
            var result = [];
            $.each(all, function(key, widgetEl) {
                result = result.concat(
                    callback(
                        $(widgetEl).closest(".lmzwidget-base").data("lmzWidget")
                    )
                );
            });
            return result;
        },

        /**
         * Make `list` has unique elements
         *
         * @param list is the list to undouble
         */
        merge : function(list) {
            var uniqResults = [];

            $.each(list, function(key, element) {
                if (uniqResults.indexOf(element) == -1) {
                    uniqResults.push(element);
                }
            });

            return uniqResults;
        }
    })

})(UOA.jQuery);
(function($) {

    //
    //  Initialize the widgets after the page has finished loading
    //
    $(document).ready(function() {
        $(".lmzwidget-base").each(function() {

            // find wrapper component
            var wrapper = $(this).find("> .widget-wrapper");

            // make sure the base url is set. if so, let's initialize it!
            if (wrapper.attr("data-base-url")) {
                $(this).lmzWidget();
            }
        });

        // boot all the widgets
        $("body").lmzBooter();
    });


})(UOA.jQuery);
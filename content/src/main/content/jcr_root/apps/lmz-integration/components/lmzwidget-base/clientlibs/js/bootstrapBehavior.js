(function($) {

    //
    //  Initialize the widgets after the page has finished loading
    //
    $(document).ready(function() {
        $(".lmzwidget-base").each(function() {
            $(this).lmzWidget();
        });

        $("body").lmzBooter();
    });


})(UOA.jQuery);
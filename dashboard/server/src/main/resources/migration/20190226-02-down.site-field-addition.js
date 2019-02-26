db.site.update(
    {},
    {
        $unset: {
            menu_layout: "",
            content_width: "",
            fixed_sidebar: false,
            fixed_header: false,
            auto_hide_header: false
        }
    },
    {multi: true}
);

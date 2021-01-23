db.site.update(
    {menu_layout: {$exists: false}},
    {
        $set: {
            menu_layout: "",
            content_width: "",
            fixed_sidebar: false,
            fixed_header: false,
            auto_hide_header: false
        }
    },
    {multi: true}
);

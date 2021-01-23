db.site.update(
    {},
    {
        $unset: {
            icon_on_collapsed: false
        }
    },
    {multi: true}
);

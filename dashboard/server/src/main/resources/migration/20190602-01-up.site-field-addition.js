db.site.update(
    {icon_on_collapsed: {$exists: false}},
    {
        $set: {
            icon_on_collapsed: true
        }
    },
    {multi: true}
);

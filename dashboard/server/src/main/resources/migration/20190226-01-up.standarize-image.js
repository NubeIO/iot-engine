function store_image(image) {
    var obj = ObjectId();
    image = image.replace("/media/", "");
    db.media_files.insert({_id: obj.str, name: image, title: ""});
    return obj.str;
}

db.site.find().forEach(function (line) {
    if (line.logo_sm.contains('media')) {
        var image_object_id = store_image(line.logo_sm);
        db.site.update({_id: line._id}, {$set: {logo_sm: image_object_id}});
    }

    if (line.logo_md.contains('media')) {
        image_object_id = store_image(line.logo_md);
        db.site.update({_id: line._id}, {$set: {logo_md: image_object_id}});
    }
});

db.layout_grid.find().forEach(function (line) {
    var widgets = line.widgets;
    if (widgets) {
        for (var i = 0; i < widgets.length; i++) {
            if (widgets[i].backgroundImage && widgets[i].backgroundImage.contains('media')) {
                widgets[i].backgroundImage = store_image(widgets[i].backgroundImage);
            }
        }
        db.layout_grid.update({_id: line._id}, {$set: {widgets: widgets}});
    }
});

function actual_image_extraction(image_object_id) {
    var image = db.media_files.findOne({_id: image_object_id});
    db.media_files.remove({_id: image_object_id});
    return '/media/' + image.name;
}

db.site.find().forEach(function (line) {
    if (line.logo_sm) {
        var image_path = actual_image_extraction(line.logo_sm);
        db.site.update({_id: line._id}, {$set: {logo_sm: image_path}});
    }

    if (line.logo_md) {
        image_path = actual_image_extraction(line.logo_md);
        db.site.update({_id: line._id}, {$set: {logo_md: image_path}});
    }
});

db.layout_grid.find().forEach(function (line) {
    var widgets = line.widgets;

    if (widgets) {
        for (var i = 0; i < widgets.length; i++) {
            if (widgets[i].backgroundImage) {
                return actual_image_extraction(widgets[i].backgroundImage);
            }
        }
        db.layout_grid.update({_id: line._id}, {$set: {widgets: widgets}});
    }
});

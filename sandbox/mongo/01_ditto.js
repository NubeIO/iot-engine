db = db.getSiblingDB("ditto");

db.createUser({
    user: "ditto",
    pwd: "ditto",
    roles:[
        {
            role: "readWrite",
            db:   "ditto"
        }
    ]
});

db.test.save({
	"_id": "ditto",
});

db = db.getSiblingDB("sandbox");

db.createUser({
    user: "sandbox",
    pwd: "111",
    roles:[
        {
            role: "readWrite",
            db:   "sandbox"
        }
    ]
});

db.company.save({
	"_id": "EnviroServices",
	"name": "EnviroServices",
	"role": "SUPER_ADMIN",
	"associated_company_id": ""
});

db.user.save({
	"_id": "7c52c05a-aafe-4913-a5ab-998c9bfffa3a",
	"username": "sandbox",
	"user_id": "7c52c05a-aafe-4913-a5ab-998c9bfffa3a",
	"role": "SUPER_ADMIN",
	"email": "sandbox@gmail.com",
	"address": "Sydney",
	"phone_no": "123456789",
	"company_id": "EnviroServices",
	"site_id": "",
	"associated_company_id": ""
});

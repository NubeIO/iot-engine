package io.nubespark;

import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.servicediscovery.types.MessageSource;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by topsykretts on 4/26/18.
 */
public class JdbcVerticle extends MicroServiceVerticle {

    AsyncSQLClient jdbc;
    public static String ADDRESS = "io.nubespark.jdbc.engine";

    @Override
    public void start() {
        super.start();
        System.out.println("Config on JDBC Engine app");
        System.out.println(Json.encodePrettily(config()));

        System.out.println("Classpath of JDBC Engine app = "+ System.getProperty("java.class.path"));
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        for(URL url: urls){
            System.out.println(url.getFile());
        }
        System.out.println("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        System.out.println(JdbcVerticle.class.getClassLoader());

        jdbc = MySQLClient.createNonShared(vertx, config());
        initializeDatabase();
        publishMessageSource("jdbc-engine", ADDRESS, handler -> {
            if(handler.failed()) {
                handler.cause().printStackTrace();
            } else {
                System.out.println("JDBC Engine Address published: " + handler.succeeded());
            }
        });

        vertx.eventBus().consumer(ADDRESS, message -> {
            String query = message.body().toString();
            JsonObject queryObj = new JsonObject(query);
            String sqlQuery = queryObj.getString("query");
            JsonArray params = queryObj.getJsonArray("params");
            System.out.println("Received query : " + query);
            jdbc.getConnection(jdbcHandler -> {
                SQLConnection connection = jdbcHandler.result();
                connection.queryWithParams(sqlQuery, params, resultHandler -> {
                    if(resultHandler.failed()) {
                        resultHandler.cause().printStackTrace();
                        Future.failedFuture("Failed to execute given query");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("status", "FAILED");
                        jsonObject.put("message", resultHandler.cause().getMessage());
                        message.reply(jsonObject);
                    } else {
                        JsonArray array = new JsonArray(resultHandler.result().getRows());
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put("status", "OK");
                        jsonObject.put("message", array);
                        message.reply(jsonObject);
                    }
                });
            });
//            message.reply("Your query:\n " + query + "\n has been received.\nThis feature is under implementation.. Please have patience.");
        });
    }

    private void initializeDatabase() {
        System.out.println("Initializing database....");
        jdbc.getConnection(ar -> {
            if (ar.failed()) {
                System.out.println("Cannot get connection object");
                ar.cause().printStackTrace();
                Future.failedFuture(ar.cause());
                return;
            }

            Map<String, String> tagsMap = new HashMap<>();
            tagsMap.put("Boiler 1", "boiler");
            tagsMap.put("Boiler 2", "boiler");
            tagsMap.put("Chiller 1", "chiller");
            tagsMap.put("Chiller 2", "chiller");
            tagsMap.put("AHU 1", "ahu");
            tagsMap.put("AHU 2", "ahu");
            tagsMap.put("Set Point1", "sp");

            SQLConnection connection = ar.result();
            String createDemoTable = "CREATE TABLE IF NOT EXISTS metadata (id int AUTO_INCREMENT, name varchar(100), tag varchar" +
                    "(100), PRIMARY KEY (id))";
            connection.execute(createDemoTable, create -> {
                System.out.println("Creating table if not exists....");
                if(create.failed()) {
                    Future.failedFuture(create.cause());
                    connection.close();
                } else {
                    connection.query("SELECT * FROM metadata LIMIT 1", (select)-> {
                        System.out.println("Checking if already initialized...");
                        if(!select.failed()) {
                            if(select.result().getNumRows() == 0) {
                                System.out.println("Initializing Database...");
                                String insertDemo = "INSERT into metadata (name, tag) VALUES ";
                                JsonArray params = new JsonArray();
                                StringJoiner queries =new StringJoiner(" , ");
                                for(Map.Entry<String, String> entry: tagsMap.entrySet()) {
                                    params.add(entry.getKey());
                                    params.add(entry.getValue());
                                    queries.add("(?,?)");
                                }
                                insertDemo = insertDemo + queries.toString();
                                System.out.println(insertDemo);
                                connection.updateWithParams(insertDemo, params, insertHandler -> {
                                    if (insertHandler.failed()) {
                                        System.out.println("Failed to insert records");
                                        Future.failedFuture(insertHandler.cause());
                                        connection.close();
                                    }
                                    System.out.println("Database initialized..");
                                    connection.close();
                                });
                            } else {
                                connection.close();
                                System.out.println("Database already initialized..");
                            }
                        } else {
                            System.out.println("Failed to check if database has records");
                            connection.close();
                            Future.failedFuture(select.cause());
                        }
                    });
                }
            });
        });
    }
}

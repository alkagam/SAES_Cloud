package com.saes.api.producer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MongoProducer {

    private static final String CONNECTION_STRING = "mongodb://saes_admin:passwordSegura123@localhost:27017/saes_cloud_db?authSource=admin";
    private static final String DATABASE_NAME = "saes_cloud_db";

    @Produces
    @ApplicationScoped
    public MongoClient createMongoClient() {
        return MongoClients.create(CONNECTION_STRING);
    }

    public void closeMongoClient(@Disposes MongoClient mongoClient) {
        mongoClient.close();
    }

    @Produces
    public MongoDatabase createDatabase(MongoClient client) {
        return client.getDatabase(DATABASE_NAME);
    }
}
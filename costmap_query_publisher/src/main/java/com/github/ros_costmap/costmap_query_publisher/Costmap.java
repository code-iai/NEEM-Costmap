package com.github.ros_costmap.costmap_query_publisher;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;


import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gte;

public class Costmap {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private String collectionName;
    private MongoCollection<Document> collection;

    public Costmap(String host, int port, String databaseName, String collectionName){
        this.mongoClient = new MongoClient(host, port);
        this.db = mongoClient.getDatabase(databaseName);
        this.collectionName = collectionName;
        this.collection = this.db.getCollection(this.collectionName);
    }

    public Document getCostmapMarkers(double timepoint){
//        Bson query = and(gte("transforms.header.stamp",start),
//                lt("transforms.header.stamp",end));

        Bson query = gte("__recorded",timepoint);
        //Bson query = all("__topic","/cram_location_costmap");

        FindIterable<Document> cursor = collection.find(query);
        return cursor.first();

//        cursor.forEach((Consumer<? super Document>) document -> {
//            System.out.println(document.get("__recorded"));
//            if((Double)document.get("__recorded") == 1526459353.39127){
//                System.out.println("HERE");
//            }
//        });
    }
}

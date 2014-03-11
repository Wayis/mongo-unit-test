package fr.wayis.framework.test.runner.manager;

import com.mongodb.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Class to manage the Mongo DB.<br/>
 * Use {@link MongoManager#runMongoDB()} to start mongod process.<br/>
 * Use {@link MongoManager#shutdownMongoDB()} to stop it.<br/>
 * The manager needs a mongodb.properties file to get these properties:<br/>
 * <ul>
 * <li>mongodb.port</li>
 * <li>mongodb.host</li>
 * <li>mongodb.dbname</li>
 * </ul>
 * This file must be on classpath root.
 */
public final class MongoManager {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mongodb");

    private static final String MONGODB_PORT_PROPERTY = "mongodb.port";
    private static final String MONGODB_HOST_PROPERTY = "mongodb.host";
    private static final String MONGODB_DBNAME_PROPERTY = "mongodb.dbname";

    private static MongoManager instance = null;

    private MongodExecutable mongodExe;
    private MongodProcess mongod;
    private DB db;
    private MongoClient mongoClient;

    private MongoManager() {
    }

    /**
     * Gets the singleton instance of MongoManager.
     *
     * @return The unique instance of MongoManager.
     */
    public static MongoManager getInstance() {
        if (instance == null) {
            instance = new MongoManager();
        }
        return instance;
    }

    /**
     * Runs the mongod process from properties of the mongodb.properties.
     *
     * @throws IOException If an error occurred during the initialization of all mongo processes.
     * @see com.mongodb.MongoClient
     * @see de.flapdoodle.embed.mongo.runtime.Mongod
     * @see de.flapdoodle.embed.mongo.MongodExecutable
     * @see com.mongodb.DBCollection
     */
    public void runMongoDB() throws IOException {
        final int port = Integer.parseInt(getValue(MONGODB_PORT_PROPERTY));
        final String host = getValue(MONGODB_HOST_PROPERTY);
        final String dbName = getValue(MONGODB_DBNAME_PROPERTY);

        MongodStarter starter = MongodStarter.getDefaultInstance();
        this.mongodExe = starter.prepare(new MongodConfig(Version.Main.DEVELOPMENT, port, Network
                .localhostIsIPv6()));
        this.mongod = this.mongodExe.start();
        this.mongoClient = new MongoClient(host, port);
        this.db = this.mongoClient.getDB(dbName);
    }

    /**
     * Shutdowns all mongo processes<br/>
     *
     * @see com.mongodb.MongoClient
     * @see de.flapdoodle.embed.mongo.runtime.Mongod
     * @see de.flapdoodle.embed.mongo.MongodExecutable
     */
    public void shutdownMongoDB() {
        this.mongoClient.close();
        this.mongod.stop();
        this.mongodExe.stop();
    }

    /**
     * Clears the given collection.
     *
     * @param collectionName The name of the collection to clear.
     */
    public void clearCollection(final String collectionName) {
        final DBCollection collection = getCollection(collectionName);
        collection.drop();
    }

    /**
     * Initializes the given collection with an JSON array.
     *
     * @param collectionName The name of the collection to initialize.
     * @param data           Data to insert.
     */
    public void initCollection(final String collectionName, final BasicDBList data) {
        final DBCollection collection = getCollection(collectionName);
        for (int i = 0; i < data.size(); i++) {
            collection.insert((DBObject) data.get(i));
        }
    }

    /**
     * Gets the collection from its name.
     *
     * @param collectionName The name of the collection to get.
     * @return An instance of DBCollection corresponding to the collectionName. If the collection does not exist, it is created.
     * @see com.mongodb.DBCollection
     */
    public DBCollection getCollection(final String collectionName) {
        return db.getCollection(collectionName);
    }

    /**
     * Util method to get a value from the mongodb.properties file.
     *
     * @param key The property key to get the value.
     * @return The value corresponding to the key. If the key does not exist, an empty String is returned.
     */
    private String getValue(final String key) {
        if (BUNDLE.containsKey(key)) {
            return BUNDLE.getString(key);
        }
        return "";
    }
}

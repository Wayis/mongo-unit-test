package fr.wayis.framework.test.mongodb;

import com.mongodb.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
     * Checks an expected collection with an existed mongodb collection from its name.<br/>
     * <p/>
     * Assertion errors are executed when:
     * <ul>
     * <li>The two collections have not the same size.</li>
     * <li>An expected document is not found in the mongodb collection.</li>
     * </ul>
     *
     * @param expectedCollection The expected collection of documents in BasicDBList format.
     * @param collectionName     The name of the mongodb collection to check.
     * @param ignoredProperties  The properties to ignore during the check.
     */
    public void checkCollection(final BasicDBList expectedCollection, final String collectionName, final String[] ignoredProperties) {
        final DBCollection actualCollection = getCollection(collectionName);
        assertEquals(expectedCollection, actualCollection, ignoredProperties);
    }

    /**
     * Asserts the equality of an expected collection of data and the mongodb collection.<br/>
     * An assertion error is thrown when the two collections have not the same size.
     * The expected and the mongodb collections are filtered with ignore properties array.
     *
     * @param expectedCollection
     * @param actualCollection
     * @param ignoredProperties
     * @see java.lang.AssertionError
     */
    private void assertEquals(final BasicDBList expectedCollection, final DBCollection actualCollection, final String[] ignoredProperties) {
        Assert.assertEquals("The expected collection does not have the same number of documents as mongodb collection.", expectedCollection.size(), actualCollection.count());
        final List<DBObject> expectedValues = buildDBObjectList(expectedCollection, ignoredProperties);
        final List<DBObject> actualValues = buildDBObjectList(actualCollection, ignoredProperties);
        assertEquals(expectedValues, actualValues);
    }

    /**
     * Asserts the equality between two expected list of DBObject representing the expected collection documents and the mongodb ones.<br/>
     * An assertion error is thrown if an expected document is not found in the mongodb collection.
     *
     * @param expected The expected collection documents as a list of DBObject.
     * @param actual   The mongodb collection documents as a list of DBObject.
     */
    private void assertEquals(final List<DBObject> expected, final List<DBObject> actual) {
        final Iterator<DBObject> iterator = expected.iterator();
        while (iterator.hasNext()) {
            final DBObject expectedDocument = iterator.next();
            Assert.assertTrue("The expected document <" + expectedDocument + "> was not found in the mongodb collection.", actual.contains(expectedDocument));
        }
    }

    /**
     * Builds a list of DBObject from a mongodb collection.<br/>
     * A mongo keys filter is created from the ignored properties array.<br/>
     * Finds all documents from the mongodb collection with the built keys filter.
     *
     * @param collection       The mongodb collection.
     * @param ignoredProperies The array of ignored properties to use to build the mongodb find keys filter.
     * @return A filtered list of DBObject.
     */
    private List<DBObject> buildDBObjectList(final DBCollection collection, final String[] ignoredProperies) {
        final DBObject ignoredPropertiesFilter = buildIgnoredPropertiesFilter(ignoredProperies);
        return collection.find(null, ignoredPropertiesFilter).toArray();
    }

    /**
     * Builds a mongo keys filter for the find method on a DBCollection from the array of ignored properties.
     *
     * @param ignoredProperties Ignored properties to use.
     * @return A DBObject representing the keys filter.
     * @see com.mongodb.DBCollection#find(com.mongodb.DBObject, com.mongodb.DBObject)
     */
    private DBObject buildIgnoredPropertiesFilter(final String[] ignoredProperties) {
        final DBObject filter = new BasicDBObject();
        if (ignoredProperties != null) {
            for (String ignoredProperty : ignoredProperties) {
                filter.put(ignoredProperty, 0);
            }
        }
        return filter;
    }

    /**
     * Builds a list of DBObject from a simple BSON documents list.<br/>
     * During the build of the list, documents are filtered with the ignored properties.
     *
     * @param basicDBList       The simple BSON documents list.
     * @param ignoredProperties The array of ignored properties to use to filter the original list.
     * @return A filtered list of DBObject.
     */
    private List<DBObject> buildDBObjectList(final BasicDBList basicDBList, final String[] ignoredProperties) {
        final List<DBObject> newList = new ArrayList<>();
        for (int i = 0; i < basicDBList.size(); i++) {
            newList.add(filterDBObject((DBObject) basicDBList.get(i), ignoredProperties));
        }
        return newList;
    }

    /**
     * Filters a DBObject with the ignored properties array.<br/>
     * If the document contains a property from the ignored properties array, the property will be removed from the original document.
     *
     * @param document          The document to filter.
     * @param ignoredProperties Ignored properties to use for the filtering.
     * @return A filtered DBObject.
     */
    private DBObject filterDBObject(final DBObject document, final String[] ignoredProperties) {
        int index = 0;
        while (index < ignoredProperties.length) {
            document.removeField(ignoredProperties[index]);
            index++;
        }
        return document;
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

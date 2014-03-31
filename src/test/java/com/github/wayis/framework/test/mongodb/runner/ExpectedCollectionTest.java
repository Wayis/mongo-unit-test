package com.github.wayis.framework.test.mongodb.runner;

import com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import fr.wayis.framework.javaee.extensions.impl.config.ConfigPropertyProducer;
import fr.wayis.framework.javaee.extensions.mongodb.DBConnection;
import com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection;
import com.github.wayis.framework.test.mongodb.runner.application.ApplicationConfig;
import com.github.wayis.framework.test.mongodb.MongoManager;
import com.github.wayis.framework.test.mongodb.runner.resource.UserResource;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Application;

/**
 * Test of the {@link ExpectedCollection} annotation to use in a test method with the {@link com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer} runner.<br/>
 * @see com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection
 * @see com.github.wayis.framework.test.mongodb.rule.CheckCollectionRule
 * @see com.github.wayis.framework.test.mongodb.MongoManager
 * @see com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer
 */
@EnableServices("jaxrs")
@RunWith(MongoApplicationComposer.class)
public class ExpectedCollectionTest {
    /**
     * Collection name to use for all tests.
     */
    private static final String COLLECTION_NAME = "users";

    /**
     * Test rule to handle assertion error and test if the assertion error is correctly thrown and if the error message is correct.
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none().handleAssertionErrors();

    /**
     * Ensures the user collection is empty.
     */
    private void clearUserCollection() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        users.drop();
        Assert.assertEquals("The original users collection is not correctly cleared", 0, users.count());
    }

    /**
     * Defines a Web ARchive (war) for deployment. It includes classes that the
     * test will invoke.
     */
    @Module
    @Classes(cdi = true, value = {UserResource.class, DBConnection.class, ConfigPropertyProducer.class})
    public WebApp app() {
        return new WebApp().contextRoot("test").addServlet("REST Application", Application.class.getName())
                .addInitParam("REST Application", "javax.ws.rs.Application", ApplicationConfig.class.getName());
    }

    /**
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br/>
     * This method tests if the annotation is called after the test and if the collection corresponds to the file.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check.json")
    public void testExpectedCollectionAnnotation() {
        clearUserCollection();
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler"));
        addUser(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
    }

    /**
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br/>
     * This method tests if the fail message is correct if the size of the expected collection is not the size as the mongodb collection.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check.json")
    public void testExpectedCollectionAnnotationFailedBadSize() {
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("The expected collection does not have the same number of documents as mongodb collection. expected:<3> but was:<4>");

        clearUserCollection();
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler"));
        addUser(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
        addUser(new BasicDBObject("lastname", "SCHRADER").append("firstname", "Hank"));
    }

    /**
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br/>
     * This method tests if the fail message is correct if there is a bad document.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check.json")
    public void testExpectedCollectionAnnotationFailedBadDocument() {
        expectedException.expect(AssertionError.class);
        final DBObject expectedBadDocument = new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler");
        expectedException.expectMessage("The expected document <" + expectedBadDocument + "> was not found in the mongodb collection.");

        clearUserCollection();
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
        addUser(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
        addUser(new BasicDBObject("lastname", "SCHRADER").append("firstname", "Hank"));
    }

    /**
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br/>
     * This method tests if the annotation is called after the test and if ignored columns are taken into account.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check_with_ignored_properties.json", ignoredProperties = {"_id", "lastname"})
    public void testIgnoredColumns() {
        clearUserCollection();
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler"));
        addUser(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
    }

    /**
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br/>
     * This method tests if the order of document keys is not a constraint.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check_different_order.json")
    public void testExpectedCollectionAnnotationDifferentOrder() {
        clearUserCollection();
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
        addUser(new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler"));
        addUser(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
    }

    /**
     * Adds an user into the user collection.
     *
     * @param user The user to add.
     */
    private void addUser(DBObject user) {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        users.insert(user);
    }
}

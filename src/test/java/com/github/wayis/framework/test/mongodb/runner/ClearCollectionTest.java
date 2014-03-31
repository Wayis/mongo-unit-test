package com.github.wayis.framework.test.mongodb.runner;

import com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import fr.wayis.framework.javaee.extensions.impl.config.ConfigPropertyProducer;
import fr.wayis.framework.javaee.extensions.mongodb.DBConnection;
import com.github.wayis.framework.test.mongodb.MongoManager;
import com.github.wayis.framework.test.mongodb.annotation.ClearCollection;
import com.github.wayis.framework.test.mongodb.runner.application.ApplicationConfig;
import com.github.wayis.framework.test.mongodb.runner.resource.UserResource;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Application;

/**
 * Test of the {@link com.github.wayis.framework.test.mongodb.annotation.ClearCollection} annotation to use in a test method with the {@link com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer} runner.<br/>
 *
 * @see com.github.wayis.framework.test.mongodb.annotation.ClearCollection
 * @see com.github.wayis.framework.test.mongodb.rule.ClearCollectionRule
 * @see com.github.wayis.framework.test.mongodb.MongoManager
 * @see com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer
 */
@EnableServices("jaxrs")
@RunWith(MongoApplicationComposer.class)
public class ClearCollectionTest {
    /**
     * Collection name to use for all tests.
     */
    private static final String COLLECTION_NAME = "users";

    static {
        initializeUserCollection();
    }

    /**
     * Initializes the user collection.
     */
    private static void initializeUserCollection() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        users.insert(new BasicDBObject("lastname", "DOE").append("firstname", "John"));
        users.insert(new BasicDBObject("lastname", "DAVIES").append("firstname", "Scott"));
        users.insert(new BasicDBObject("lastname", "NORRIS").append("firstname", "Chuck"));
        users.insert(new BasicDBObject("lastname", "GATES").append("firstname", "Bill"));
        Assert.assertEquals("The original users collection is not correctly initialized", 4, users.count());
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
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.ClearCollection} annotation.<br/>
     * This method tests if the annotation is called before the test and if the collection is correctly dropped.
     */
    @Test
    @ClearCollection(name = COLLECTION_NAME)
    public void testClearCollectionAnnotation() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        Assert.assertEquals("The @ClearCollection does not clear the collection.", 0, users.count());
    }

}

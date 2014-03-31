package com.github.wayis.framework.test.mongodb.runner;

import com.github.wayis.framework.javaee.extensions.impl.config.ConfigPropertyProducer;
import com.github.wayis.framework.javaee.extensions.mongodb.DBConnection;
import com.mongodb.DBCollection;
import com.github.wayis.framework.test.mongodb.annotation.InitCollection;
import com.github.wayis.framework.test.mongodb.runner.application.ApplicationConfig;
import com.github.wayis.framework.test.mongodb.MongoManager;
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
 * Test of the {@link com.github.wayis.framework.test.mongodb.annotation.InitCollection} annotation to use in a test method with the {@link com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer} runner.<br/>
 *
 * @see com.github.wayis.framework.test.mongodb.annotation.InitCollection
 * @see com.github.wayis.framework.test.mongodb.rule.InitCollectionRule
 * @see com.github.wayis.framework.test.mongodb.MongoManager
 * @see com.github.wayis.framework.test.mongodb.runner.MongoApplicationComposer
 */
@EnableServices("jaxrs")
@RunWith(MongoApplicationComposer.class)
public class InitCollectionTest {
    /**
     * Collection name to use for all tests.
     */
    private static final String COLLECTION_NAME = "users";

    static {
        clearUserCollection();
    }

    /**
     * Ensures the user collection is empty.
     */
    private static void clearUserCollection() {
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
     * Tests the {@link com.github.wayis.framework.test.mongodb.annotation.InitCollection} annotation.<br/>
     * This method tests if the annotation is called before the test and if the collection is correctly initialized.
     */
    @Test
    @InitCollection(name = COLLECTION_NAME, file = "/data/users_init.json")
    public void testInitCollectionAnnotation() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        Assert.assertEquals("The @InitCollection does not initialize the collection.", 5, users.count());
    }
}

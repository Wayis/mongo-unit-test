package fr.wayis.framework.test.runner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import fr.wayis.framework.jee6.property.ConfigPropertyProducer;
import fr.wayis.framework.mongo.DBConnection;
import fr.wayis.framework.test.runner.annotation.InitCollection;
import fr.wayis.framework.test.runner.application.ApplicationConfig;
import fr.wayis.framework.test.runner.manager.MongoManager;
import fr.wayis.framework.test.runner.resource.UserResource;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Application;

/**
 * Test of the {@link MongoApplicationComposer} JUnit Runner.<br/>
 * This test class tests:
 * <ul>
 * <li>{@link fr.wayis.framework.test.runner.annotation.ClearCollection}: to test {@link fr.wayis.framework.test.runner.rule.ClearCollectionRule}</li>
 * </ul>
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


    @Test
    @InitCollection(name = COLLECTION_NAME, file = "/users_init.json")
    public void testInitCollectionAnnotation() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        Assert.assertEquals("The @InitCollection does not initialize the collection.", 5, users.count());
    }
}
package fr.wayis.framework.test.runner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import fr.wayis.framework.jee6.property.ConfigPropertyProducer;
import fr.wayis.framework.mongo.DBConnection;
import fr.wayis.framework.test.runner.annotation.ClearCollection;
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

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.List;

/**
 * Test of the {@link fr.wayis.framework.test.runner.MongoApplicationComposer} JUnit Runner.<br/>
 * This test class tests:
 * <ul>
 * <li>{@link fr.wayis.framework.test.runner.annotation.ClearCollection}: to test {@link fr.wayis.framework.test.runner.rule.ClearCollectionRule}</li>
 * </ul>
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
     * Tests the {@link fr.wayis.framework.test.runner.annotation.ClearCollection} annotation.<br/>
     * This method tests if the annotation is called before the test and if the collection is correctly dropped.
     */
    @Test
    @ClearCollection(name = COLLECTION_NAME)
    public void testClearCollectionAnnotation() {
        final DBCollection users = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        Assert.assertEquals("The @ClearCollection does not clear the collection.", 0, users.count());
    }

}

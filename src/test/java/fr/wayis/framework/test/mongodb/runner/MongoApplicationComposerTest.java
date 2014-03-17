package fr.wayis.framework.test.mongodb.runner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import fr.wayis.framework.javaee.extensions.impl.config.ConfigPropertyProducer;
import fr.wayis.framework.javaee.extensions.mongodb.DBConnection;
import fr.wayis.framework.test.mongodb.annotation.ClearCollection;
import fr.wayis.framework.test.mongodb.annotation.ExpectedCollection;
import fr.wayis.framework.test.mongodb.annotation.InitCollection;
import fr.wayis.framework.test.mongodb.runner.application.ApplicationConfig;
import fr.wayis.framework.test.mongodb.MongoManager;
import fr.wayis.framework.test.mongodb.runner.resource.UserResource;
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
 * Test of the {@link fr.wayis.framework.test.mongodb.runner.MongoApplicationComposer} JUnit Runner.<br/>
 * This test class tests:
 * <ul>
 * <li>{@link fr.wayis.framework.test.mongodb.annotation.ClearCollection}: to test {@link fr.wayis.framework.test.mongodb.rule.ClearCollectionRule}</li>
 * <li>{@link fr.wayis.framework.test.mongodb.annotation.InitCollection}: to test {@link fr.wayis.framework.test.mongodb.rule.InitCollectionRule}</li>
 * <li>{@link fr.wayis.framework.test.mongodb.annotation.ExpectedCollection}: to test {@link fr.wayis.framework.test.mongodb.rule.CheckCollectionRule}</li>
 * </ul>
 * The goal of this test is to combine all annotations together to check the correct order of their executions:
 * <ol>
 * <li>@ClearCollection</li>
 * <li>@InitCollection</li>
 * <li>@ExpectedCollection</li>
 * </ol>
 * This test also verifies the combination of the CDI provided by the OpenEJB ApplicationComposer runner and custom annotations.
 *
 * @see ClearCollectionTest
 * @see InitCollectionTest
 * @see ExpectedCollectionTest
 * @see org.apache.openejb.junit.ApplicationComposer
 */
@EnableServices("jaxrs")
@RunWith(MongoApplicationComposer.class)
public class MongoApplicationComposerTest {
    /**
     * Collection name to use for all tests.
     */
    private static final String COLLECTION_NAME = "users";

    /**
     * API Resource injected by CDI to manage users.
     */
    @Inject
    private UserResource userResource;


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
     * Tests the {@link fr.wayis.framework.test.mongodb.annotation.ClearCollection} annotation combined with the {@link fr.wayis.framework.test.mongodb.annotation.InitCollection} one.<br/>
     * This method tests if ClearCollection annotation is called before the InitCollection one.
     */
    @Test
    @ClearCollection(name = COLLECTION_NAME)
    @InitCollection(name = COLLECTION_NAME, file = "/data/users_init.json")
    public void testClearInitCollectionAnnotation() {
        List<DBObject> users = userResource.findAll();
        Assert.assertEquals("The collection was not correctly initialized.", 5, users.size());
    }

    /**
     * Tests the {@link fr.wayis.framework.test.mongodb.annotation.ClearCollection} annotation combined with the {@link fr.wayis.framework.test.mongodb.annotation.InitCollection} one.<br/>
     * This method tests if ClearCollection annotation is called before the InitCollection one even if the annotation order is changed on the test method.
     */
    @Test
    @InitCollection(name = COLLECTION_NAME, file = "/data/users_init.json")
    @ClearCollection(name = COLLECTION_NAME)
    public void testClearInitCollectionAnnotationDifferentOrder() {
        List<DBObject> users = userResource.findAll();
        Assert.assertEquals("The collection was not correctly initialized.", 5, users.size());
    }

    /**
     * Tests the combination of all annotations.<br/>
     * This method tests if ClearCollection annotation is called before the InitCollection one and if the ExpectedCollection is called after all.
     */
    @Test
    @ClearCollection(name = COLLECTION_NAME)
    @InitCollection(name = COLLECTION_NAME, file = "/data/users_init.json")
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check_insert.json")
    public void testCombinedAllAnnotations() {
        List<DBObject> users = userResource.findAll();
        Assert.assertEquals("The collection was not correctly initialized.", 5, users.size());

        final DBCollection usersCollection = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        usersCollection.insert(new BasicDBObject("lastname", "FRING").append("firstname", "Gus"));
        usersCollection.insert(new BasicDBObject("lastname", "EHRMANTRAUT").append("firstname", "Mike"));
    }

    /**
     * Tests the combination of all annotations.<br/>
     * This method tests if ClearCollection annotation is called before the InitCollection one and if the ExpectedCollection is called after all even if the order is changed.
     */
    @Test
    @ExpectedCollection(name = COLLECTION_NAME, file = "/data/users_check_insert.json")
    @InitCollection(name = COLLECTION_NAME, file = "/data/users_init.json")
    @ClearCollection(name = COLLECTION_NAME)
    public void testCombinedAllAnnotationsDifferentOrder() {
        List<DBObject> users = userResource.findAll();
        Assert.assertEquals("The collection was not correctly initialized.", 5, users.size());

        final DBCollection usersCollection = MongoManager.getInstance().getCollection(COLLECTION_NAME);
        usersCollection.insert(new BasicDBObject("lastname", "FRING").append("firstname", "Gus"));
        usersCollection.insert(new BasicDBObject("lastname", "EHRMANTRAUT").append("firstname", "Mike"));
    }
}

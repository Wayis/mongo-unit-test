#MongoDB Java EE 6 unit test utils
It provides a runner extending the OpenEJB ApplicationComposer allowing the use of custom annotations for test methods.
**_Maven Dependencies_**<br/>
```
<dependency>
    <groupId>fr.wayis.framework</groupId>
    <artifactId>mongo-unit-test</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```
## Version 1.0.0
**_MongoApplicationComposer_**<br/>
JUnit Runner to manage custom rules:
* ClearCollectionRule: to clear a given collection. Used with the @ClearCollection annotation.
* InitCollectionRule: to initialize a given collection with a JSON file. Used with the @InitCollection annotation.
* CheckCollectionRule: to check a JSON file with the given collection. Used with @ExpectedCollection annotation.

These rules will be executed before all others test rules declared by @Rule annotation.<br/>
This runner extends the OpenEJB ApplicationComposer runner.<br/>
To use it:
```java
@RunWith(MongoApplicationComposer.class)
```

**_@ClearCollection(String name)_**<br/>
Annotation used by the runner to clear a specific collection.<br/>
Example:
```java
@Test
@ClearCollection(name = "collection")
public void testClearCollectionAnnotation() {
    final DBCollection users = MongoManager.getInstance().getCollection("collection");
    Assert.assertEquals("The @ClearCollection does not clear the collection.", 0, users.count());
}
```

**_@InitCollection(String name, String file)_**<br/>
Annotation used by the runner to initialize a specific collection with a JSON file.<br/>
The file must be on JSON format.<br/>
Example:
```java
@Test
@InitCollection(name = "collection", file = "/data/users_init.json")
public void testInitCollectionAnnotation() {
    final DBCollection users = MongoManager.getInstance().getCollection("collection");
    Assert.assertEquals("The @InitCollection does not initialize the collection.", 5, users.count());
}
```

**_@ExpectedCollection()_**<br/>
Annotation used by the runner to verify the given collection.<br/>
The file must be on JSON format.<br/>
Documents order in the JSON array is not a constraint.<br/>
Example:
```java
@Test
@ExpectedCollection(name = "collection", file = "/data/users_check.json")
public void testExpectedCollectionAnnotation() {
    final DBCollection usersCollection = MongoManager.getInstance().getCollection("collection");
    usersCollection.insert(new BasicDBObject("lastname", "WHITE").append("firstname", "Walt"));
    usersCollection.insert(new BasicDBObject("lastname", "WHITE").append("firstname", "Skyler"));
    usersCollection.insert(new BasicDBObject("lastname", "PINKMAN").append("firstname", "Jesse"));
}
```

Full example with combination of all annotations:
```java
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
}
```

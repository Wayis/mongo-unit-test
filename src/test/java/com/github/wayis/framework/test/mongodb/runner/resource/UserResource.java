package com.github.wayis.framework.test.mongodb.runner.resource;

import com.github.wayis.framework.javaee.extensions.mongodb.DBConnection;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST API to manage users.<br/>
 * <ul>
 * <li><i>/users : GET</i> => Gets all users in JSON format.</li>
 * </ul>
 * Example of users in JSON format :<br/>
 * <pre>
 * {
 *      "lastname": "DOE",
 *      "firstname": "John"
 * }
 * </pre>
 */
@Stateless
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    /**
     * MongoDB connection provider.
     */
    @Inject
    private DBConnection dbConnection;
    /**
     * Users collection.
     */
    private DBCollection users;

    @PostConstruct
    public void init() {
        users = dbConnection.getCollection("users");
    }

    /**
     * Finds all users.
     *
     * @return List of DBObject represents users.
     */
    @GET
    public List<DBObject> findAll() {
        return users.find().toArray();
    }
}

package com.github.wayis.framework.test.mongodb.runner.application;

import com.mongodb.jee.jaxrs.JaxrsMongoApplication;
import com.github.wayis.framework.test.mongodb.runner.resource.UserResource;

import javax.ws.rs.ApplicationPath;
import java.util.Set;

/**
 * This class replaces servlet definition into the web.xml.<br/>
 *
 * @ApplicationPath allows to create servlet definition.
 */
@ApplicationPath("/api/v1")
public class ApplicationConfig extends JaxrsMongoApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.add(UserResource.class);
        return classes;
    }
}

package fr.wayis.framework.test.mongodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by the runner to initialize a specific collection with a JSON file.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitCollection {

    /**
     * The name of the collection to initialize.
     */
    String name();

    /**
     * The file in JSON format.
     */
    String file();
}

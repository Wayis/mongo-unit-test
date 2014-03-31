package com.github.wayis.framework.test.mongodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by the runner to verify the given collection.<br/>
 * The file must be on JSON format.<br/>
 * Documents order in the JSON array is not a constraint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectedCollection {

    /**
     * The name of the collection to initialize.
     */
    String name();

    /**
     * The file in JSON format.
     */
    String file();

    /**
     * Properties to ignore during documents comparison.
     */
    String[] ignoredProperties() default "_id";
}

package com.github.wayis.framework.test.mongodb.rule;

import com.github.wayis.framework.test.mongodb.annotation.ClearCollection;
import com.github.wayis.framework.test.mongodb.MongoManager;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit test rule to manage the {@link com.github.wayis.framework.test.mongodb.annotation.ClearCollection} annotation.<br>
 * The unit test is evaluated after this test rule.<br>
 * This rule checks the ClearCollection annotation and calls the MongoManager to clear the given collection.
 *
 * @see com.github.wayis.framework.test.mongodb.annotation.ClearCollection
 * @see com.github.wayis.framework.test.mongodb.MongoManager
 * @see org.junit.rules.TestRule
 */
public final class ClearCollectionRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearCollectionRule.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ClearCollection annotation = description.getAnnotation(ClearCollection.class);
                if (annotation != null) {
                    final String collectionName = annotation.name();
                    LOGGER.info("@ClearCollection found -> collection '" + collectionName + "' will be cleared");
                    MongoManager.getInstance().clearCollection(collectionName);
                }
                base.evaluate();
            }
        };
    }

}

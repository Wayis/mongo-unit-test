package fr.wayis.framework.test.runner.rule;

import fr.wayis.framework.test.runner.annotation.ClearCollection;
import fr.wayis.framework.test.runner.manager.MongoManager;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit test rule to manage the {@link fr.wayis.framework.test.runner.annotation.ClearCollection} annotation.<br>
 * The unit test is evaluated after this test rule.<br>
 * This rule checks the ClearCollection annotation and calls the CollectionManager to clear the given collection.
 *
 * @see fr.wayis.framework.test.runner.annotation.ClearCollection
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
                    LOGGER.info("@ClearCollection found -> collection '" + annotation.value() + "' will be cleared");
                    MongoManager.getInstance().clearCollection(annotation.value());
                }
                base.evaluate();
            }
        };
    }

}

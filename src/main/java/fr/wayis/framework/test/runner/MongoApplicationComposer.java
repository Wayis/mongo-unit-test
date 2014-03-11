package fr.wayis.framework.test.runner;

import fr.wayis.framework.test.runner.manager.MongoManager;
import fr.wayis.framework.test.runner.rule.ClearCollectionRule;
import fr.wayis.framework.test.runner.rule.InitCollectionRule;
import org.apache.openejb.junit.ApplicationComposer;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.rules.TestRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.List;

/**
 * JUnit Runner to manage custom rules :
 * <ul>
 * <li>ClearCollectionRule: to clear a given collection. Used with the {@link fr.wayis.framework.test.runner.annotation.ClearCollection} annotation.</li>
 * <li>InitCollectionRule: to initialize a given collection with a JSON file. Used with the {@link fr.wayis.framework.test.runner.annotation.InitCollection} annotation.</li>
 * </ul>
 * <p/>
 * This Runner extends the openejb {@link org.apache.openejb.junit.ApplicationComposer} Runner.
 *
 * @see fr.wayis.framework.test.runner.rule.ClearCollectionRule
 * @see fr.wayis.framework.test.runner.annotation.ClearCollection
 * @see fr.wayis.framework.test.runner.rule.InitCollectionRule
 * @see fr.wayis.framework.test.runner.annotation.InitCollection
 * @see org.apache.openejb.junit.ApplicationComposer
 */
public class MongoApplicationComposer extends ApplicationComposer {

    private TestRule clearCollectionRule;
    private TestRule initCollectionRule;

    /**
     * Constructs the Runner and initializes all rules.<br/>
     * The ApplicationComposer constructor is called first.
     *
     * @param klass The test class
     * @throws InitializationError Thrown during the initialization of the Runner
     */
    public MongoApplicationComposer(Class<?> klass) throws InitializationError {
        super(klass);
        this.clearCollectionRule = new ClearCollectionRule();
        this.initCollectionRule = new InitCollectionRule();
    }

    /**
     * {@inheritDoc}<br/>
     * A MongoDB is instanciated once before any of the test methods in the class.<br/>
     * The MongoDB is shutdowned after all the tests in the class have run.
     */
    @Override
    public void run(RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier,
                getDescription());
        try {
            MongoManager.getInstance().runMongoDB();
        } catch (IOException e) {
            testNotifier.addFailure(e);
        }
        super.run(notifier);
        MongoManager.getInstance().shutdownMongoDB();
    }

    /**
     * Adds custom rules to the ApplicationComposer rules.
     *
     * @param target the test case instance
     * @return a list of TestRules that should be applied when executing this
     * test
     *
     * @see fr.wayis.framework.test.runner.rule.ClearCollectionRule
     * @see fr.wayis.framework.test.runner.rule.InitCollectionRule
     */
    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> rules = super.getTestRules(target);
        rules.add(clearCollectionRule);
        rules.add(initCollectionRule);
        return rules;
    }
}

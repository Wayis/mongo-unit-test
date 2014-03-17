package fr.wayis.framework.test.mongodb.rule;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import fr.wayis.framework.test.mongodb.annotation.InitCollection;
import fr.wayis.framework.test.mongodb.MongoManager;
import fr.wayis.framework.test.util.StreamUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * JUnit test rule to manage the {@link fr.wayis.framework.test.mongodb.annotation.InitCollection} annotation.<br>
 * The unit test is evaluated after this test rule.<br>
 * This rule checks the InitCollection annotation and calls the MongoManager to initialize the given collection with a JSON file.
 *
 * @see fr.wayis.framework.test.mongodb.annotation.InitCollection
 * @see fr.wayis.framework.test.mongodb.MongoManager
 * @see org.junit.rules.TestRule
 */
public final class InitCollectionRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitCollectionRule.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                InitCollection annotation = description.getAnnotation(InitCollection.class);
                if (annotation != null) {
                    final String collectionName = annotation.name();
                    final String fileName = annotation.file();
                    LOGGER.info("@InitCollection found -> collection '" + collectionName + "' will be initialized with the file '" + fileName + "'");
                    InputStream file = null;
                    try {
                        file = description.getTestClass().getResourceAsStream(fileName);
                        if (file == null) {
                            throw new FileNotFoundException("Unable to load file '" + fileName + "' from the classpath");
                        }

                        String jsonFile = IOUtils.toString(file, "UTF-8");
                        BasicDBList data = (BasicDBList) JSON.parse(jsonFile);
                        MongoManager.getInstance().initCollection(collectionName, data);
                    } finally {
                        StreamUtils.closeQuietly(file);
                    }
                }
                base.evaluate();
            }
        };
    }

}

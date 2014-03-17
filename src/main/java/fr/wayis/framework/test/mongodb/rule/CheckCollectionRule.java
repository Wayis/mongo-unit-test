package fr.wayis.framework.test.mongodb.rule;

import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import fr.wayis.framework.test.mongodb.annotation.ExpectedCollection;
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
import java.util.Arrays;

/**
 * JUnit test rule to manage the {@link fr.wayis.framework.test.mongodb.annotation.ExpectedCollection} annotation.<br>
 * The unit test is evaluated before this test rule.<br>
 * This rule checks the ExpectedCollection annotation and calls the MongoManager to check if the given collection corresponds to the JSON file.
 *
 * @see fr.wayis.framework.test.mongodb.annotation.ExpectedCollection
 * @see fr.wayis.framework.test.mongodb.MongoManager
 * @see org.junit.rules.TestRule
 */
public final class CheckCollectionRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckCollectionRule.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();

                ExpectedCollection annotation = description.getAnnotation(ExpectedCollection.class);
                if (annotation != null) {
                    final String collectionName = annotation.name();
                    final String fileName = annotation.file();
                    final String[] ignoredProperties = annotation.ignoredProperties();
                    LOGGER.info("@ExpectedCollection found -> collection '" + collectionName + "' will be checked with the file '" + fileName + "' with ignored properties: " + Arrays.toString(ignoredProperties));
                    InputStream file = null;
                    try {
                        file = description.getTestClass().getResourceAsStream(fileName);
                        if (file == null) {
                            throw new FileNotFoundException("Unable to load file '" + fileName + "' from the classpath");
                        }
                        String jsonFile = IOUtils.toString(file, "UTF-8");
                        BasicDBList data = (BasicDBList) JSON.parse(jsonFile);
                        MongoManager.getInstance().checkCollection(data, collectionName, ignoredProperties);
                    } finally {
                        StreamUtils.closeQuietly(file);
                    }
                }
            }
        };
    }

}

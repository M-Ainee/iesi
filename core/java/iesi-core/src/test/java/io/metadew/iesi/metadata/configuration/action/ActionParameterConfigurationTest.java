package io.metadew.iesi.metadata.configuration.action;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.definition.action.ActionParameter;
import io.metadew.iesi.metadata.definition.action.key.ActionParameterKey;
import io.metadew.iesi.metadata.repository.DesignMetadataRepository;
import io.metadew.iesi.metadata.repository.RepositoryTestSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionParameterConfigurationTest {

    private ActionParameter actionParameter1;
    private DesignMetadataRepository designMetadataRepository;
    private ActionParameter actionParameter2;

    @BeforeEach
    void setup() {
        designMetadataRepository = RepositoryTestSetup.getDesignMetadataRepository();
        actionParameter1 = new ActionParameterBuilder("1", 1, "1", "firstParameter")
                .value("parameter value")
                .build();
        actionParameter2 = new ActionParameterBuilder("1", 1, "1", "secondParameter")
                .value("parameter value")
                .build();
    }

    @AfterEach
    void clearDatabase() {
        // drop because the designMetadataRepository already is initialized so you can't recreate those tables
        // in the initializer unless you delete the tables after each test
        designMetadataRepository.dropAllTables();
    }

    @Test
    void actionParameterNotExistsOnlyTest() {
        assertFalse(ActionParameterConfiguration.getInstance().exists(actionParameter1));
    }

    @Test
    void actionParameterNotExistsTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter2);
        assertFalse(ActionParameterConfiguration.getInstance().exists(actionParameter1));
    }

    @Test
    void actionParameterExistsOnlyTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        assertTrue(ActionParameterConfiguration.getInstance().exists(actionParameter1.getMetadataKey()));
    }

    @Test
    void actionParameterExistsTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        ActionParameterConfiguration.getInstance().insert(actionParameter2);
        assertTrue(ActionParameterConfiguration.getInstance().exists(actionParameter1.getMetadataKey()));
        assertTrue(ActionParameterConfiguration.getInstance().exists(actionParameter2.getMetadataKey()));
    }

    @Test
    void actionParameterInsertOnlyTest() throws MetadataAlreadyExistsException {
        assertEquals(0, ActionParameterConfiguration.getInstance().getAll().size());

        ActionParameterConfiguration.getInstance().insert(actionParameter1);

        assertEquals(1, ActionParameterConfiguration.getInstance().getAll().size());
        assertTrue(ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey()).isPresent());
        assertEquals(actionParameter1, ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey()).get());
    }

    @Test
    void actionParameterInsertTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter2);
        assertEquals(1, ActionParameterConfiguration.getInstance().getAll().size());
        ActionParameterConfiguration.getInstance().insert(actionParameter1);

        assertTrue(ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey()).isPresent());
        assertEquals(actionParameter1, ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey()).get());
        assertEquals(2, ActionParameterConfiguration.getInstance().getAll().size());
    }

    @Test
    void actionParameterInsertAlreadyExistsTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        assertThrows(MetadataAlreadyExistsException.class,() -> ActionParameterConfiguration.getInstance().insert(actionParameter1));
    }

    @Test
    void actionDeleteTest() throws MetadataDoesNotExistException, MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        assertEquals(1, ActionParameterConfiguration.getInstance().getAll().size());
        ActionParameterConfiguration.getInstance().delete(actionParameter1.getMetadataKey());
        assertEquals(0, ActionParameterConfiguration.getInstance().getAll().size());
    }

    @Test
    void actionDeleteDoesNotExistOnlyTest() {
        assertThrows(MetadataDoesNotExistException.class,() -> ActionParameterConfiguration.getInstance().delete(actionParameter1.getMetadataKey()));
    }

    @Test
    void setActionParameterGetOnlyTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);

        Optional<ActionParameter> actionParameter1Fetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameter1Fetched.isPresent());
        assertEquals(actionParameter1, actionParameter1Fetched.get());
    }

    @Test
    void actionDeleteDoesNotExistTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter2);
        assertThrows(MetadataDoesNotExistException.class,() -> ActionParameterConfiguration.getInstance().delete(actionParameter1.getMetadataKey()));
    }

    @Test
    void setActionParameterGetTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        ActionParameterConfiguration.getInstance().insert(actionParameter2);

        Optional<ActionParameter> actionParameter1Fetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameter1Fetched.isPresent());
        assertEquals(actionParameter1, actionParameter1Fetched.get());
    }

    @Test
    void actionParameterGetNotExistsTest() throws MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        ActionParameterConfiguration.getInstance().insert(actionParameter2);

        ActionParameterKey newActionParameterKey = new ActionParameterKey("3", 4, "not exist", "test parameter");
        assertFalse(ActionParameterConfiguration.getInstance().get(newActionParameterKey).isPresent());
    }

    @Test
    void actionParameterUpdateOnlyTest() throws MetadataDoesNotExistException, MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        Optional<ActionParameter> actionParameterFetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameterFetched.isPresent());
        assertEquals("parameter value", actionParameter1.getValue());

        actionParameter1.setValue("new value");
        ActionParameterConfiguration.getInstance().update(actionParameter1);

        actionParameterFetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameterFetched.isPresent());
        assertEquals("new value", actionParameter1.getValue());
    }

    @Test
    void actionParameterUpdateTest() throws MetadataDoesNotExistException, MetadataAlreadyExistsException {
        ActionParameterConfiguration.getInstance().insert(actionParameter1);
        ActionParameterConfiguration.getInstance().insert(actionParameter2);
        Optional<ActionParameter> actionParameterFetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameterFetched.isPresent());
        assertEquals("parameter value", actionParameter1.getValue());

        actionParameter1.setValue("new value");
        ActionParameterConfiguration.getInstance().update(actionParameter1);

        actionParameterFetched = ActionParameterConfiguration.getInstance().get(actionParameter1.getMetadataKey());
        assertTrue(actionParameterFetched.isPresent());
        assertEquals("new value", actionParameter1.getValue());
    }
}

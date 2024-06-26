package edu.illinois.library.cantaloupe.resource.api;

import edu.illinois.library.cantaloupe.Application;
import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.http.Headers;
import edu.illinois.library.cantaloupe.http.Method;
import edu.illinois.library.cantaloupe.http.ResourceException;
import edu.illinois.library.cantaloupe.http.Response;
import edu.illinois.library.cantaloupe.image.MediaType;
import edu.illinois.library.cantaloupe.resource.Route;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional test of TasksResource.
 */
public class TasksResourceTest extends AbstractAPIResourceTest {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        client.setMethod(Method.POST);
    }

    @Override
    protected String getEndpointPath() {
        return Route.TASKS_PATH;
    }

    @Override
    @Test
    public void testOPTIONSWhenEnabled() throws Exception {
        Configuration config = Configuration.getInstance();
        config.setProperty(Key.API_ENABLED, true);

        client.setMethod(Method.OPTIONS);
        Response response = client.send();
        assertEquals(204, response.getStatus());

        Headers headers = response.getHeaders();
        List<String> methods =
                List.of(StringUtils.split(headers.getFirstValue("Allow"), ", "));
        assertEquals(2, methods.size());
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("OPTIONS"));
    }

    @Test
    void testPOSTWithIncorrectContentType() throws Exception {
        try {
            client.setEntity("{ \"verb\": \"PurgeCache\" }");
            client.setContentType(MediaType.TEXT_PLAIN);
            client.send();
        } catch (ResourceException e) {
            assertEquals(415, e.getStatusCode());
        }
    }

    @Test
    void testPOSTWithEmptyRequestBody() throws Exception {
        try {
            client.setContentType(MediaType.APPLICATION_JSON);
            client.send();
        } catch (ResourceException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    void testPOSTWithMalformedRequestBody() throws Exception {
        try {
            client.setEntity("{ this is: invalid\" }");
            client.setContentType(MediaType.APPLICATION_JSON);
            client.send();
        } catch (ResourceException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    void testPOSTWithMissingVerb() throws Exception {
        try {
            client.setEntity("{ \"cats\": \"yes\" }");
            client.setContentType(MediaType.APPLICATION_JSON);
            client.send();
        } catch (ResourceException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    void testPOSTWithUnsupportedVerb() throws Exception {
        try {
            client.setEntity("{ \"verb\": \"dogs\" }");
            client.setContentType(MediaType.APPLICATION_JSON);
            client.send();
        } catch (ResourceException e) {
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    void testPOSTWithPurgeInfoCacheVerb() throws Exception {
        client.setEntity("{ \"verb\": \"PurgeInfoCache\" }");
        client.setContentType(MediaType.APPLICATION_JSON);
        Response response = client.send();

        assertEquals(202, response.getStatus());
        assertNotNull(response.getHeaders().getFirstValue("Location"));
    }

    @Test
    void testPOSTWithPurgeInvalidFromCacheVerb() throws Exception {
        client.setEntity("{ \"verb\": \"PurgeInvalidFromCache\" }");
        client.setContentType(MediaType.APPLICATION_JSON);
        Response response = client.send();

        assertEquals(202, response.getStatus());
        assertNotNull(response.getHeaders().getFirstValue("Location"));
    }

    @Test
    void testPOSTWithPurgeItemFromCacheVerb() throws Exception {
        client.setEntity("{ \"verb\": \"PurgeItemFromCache\", \"identifier\": \"cats\" }");
        client.setContentType(MediaType.APPLICATION_JSON);
        Response response = client.send();

        assertEquals(202, response.getStatus());
        assertNotNull(response.getHeaders().getFirstValue("Location"));
    }

    @Test
    void testPOSTResponseHeaders() throws Exception {
        client.setEntity("{ \"verb\": \"PurgeInfoCache\" }");
        client.setContentType(MediaType.APPLICATION_JSON);
        Response response = client.send();
        Headers headers = response.getHeaders();
        assertEquals(6, headers.size());

        // Cache-Control
        assertEquals("no-cache", headers.getFirstValue("Cache-Control"));
        // Content-Length
        assertNotNull(headers.getFirstValue("Content-Length"));
        // Date
        assertNotNull(headers.getFirstValue("Date"));
        // Location
        assertNotNull(headers.getFirstValue("Location"));
        // Server
        assertNotNull(headers.getFirstValue("Server"));
        // X-Powered-By
        assertEquals(Application.getName() + "/" + Application.getVersion(),
                headers.getFirstValue("X-Powered-By"));
    }

}

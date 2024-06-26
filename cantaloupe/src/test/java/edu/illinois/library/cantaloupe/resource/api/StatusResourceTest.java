package edu.illinois.library.cantaloupe.resource.api;

import edu.illinois.library.cantaloupe.Application;
import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.http.Headers;
import edu.illinois.library.cantaloupe.http.ResourceException;
import edu.illinois.library.cantaloupe.http.Response;
import edu.illinois.library.cantaloupe.resource.Route;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatusResourceTest extends AbstractAPIResourceTest {

    @Override
    protected String getEndpointPath() {
        return Route.STATUS_PATH;
    }

    @Test
    void testGETWithEndpointEnabled() throws Exception {
        Configuration config = Configuration.getInstance();
        config.setProperty(Key.API_ENABLED, true);

        Response response = client.send();
        assertEquals(200, response.getStatus());
    }

    @Test
    void testGETWithEndpointDisabled() throws Exception {
        Configuration config = Configuration.getInstance();
        config.setProperty(Key.API_ENABLED, false);
        try {
            client.send();
            fail("Expected exception");
        } catch (ResourceException e) {
            assertEquals(403, e.getStatusCode());
        }
    }

    @Test
    void testGETResponseBody() throws Exception {
        Response response = client.send();
        assertTrue(response.getBodyAsString().contains("\"infoCache\":"));
    }

    @Test
    void testGETResponseHeaders() throws Exception {
        Response response = client.send();
        Headers headers = response.getHeaders();
        assertEquals(6, headers.size());

        // Cache-Control
        assertEquals("no-cache", headers.getFirstValue("Cache-Control"));
        // Content-Length
        assertNotNull(headers.getFirstValue("Content-Length"));
        // Content-Type
        assertTrue("application/json;charset=UTF-8".equalsIgnoreCase(
                headers.getFirstValue("Content-Type")));
        // Date
        assertNotNull(headers.getFirstValue("Date"));
        // Server
        assertNotNull(headers.getFirstValue("Server"));
        // X-Powered-By
        assertEquals(Application.getName() + "/" + Application.getVersion(),
                headers.getFirstValue("X-Powered-By"));
    }

}

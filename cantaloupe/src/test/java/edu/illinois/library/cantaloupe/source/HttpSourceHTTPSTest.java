package edu.illinois.library.cantaloupe.source;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;

abstract class HttpSourceHTTPSTest extends HttpSourceTest {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Configuration config = Configuration.getInstance();
        config.setProperty(Key.HTTPSOURCE_URL_PREFIX, server.getHTTPSURI() + "/");
        config.setProperty(Key.HTTPSOURCE_ALLOW_INSECURE, true);
    }

    @Override
    String getScheme() {
        return "https";
    }

    @Override
    URI getServerURI() {
        return server.getHTTPSURI();
    }

}

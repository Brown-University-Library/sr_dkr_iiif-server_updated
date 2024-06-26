package edu.illinois.library.cantaloupe.resource;

import edu.illinois.library.cantaloupe.http.Method;
import edu.illinois.library.cantaloupe.http.Reference;
import edu.illinois.library.cantaloupe.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permanently redirects (via HTTP 301) {@literal /some/path/} to {@literal
 * /some/path}, respecting the Servlet context root, {@literal
 * X-Forwarded-Path} header, and other factors.
 */
public class TrailingSlashRemovingResource extends AbstractResource {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TrailingSlashRemovingResource.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Method[] getSupportedMethods() {
        return Method.values();
    }

    @Override
    public void doGET() {
        final Reference newRef = new Reference(getPublicReference());
        final String path = newRef.getPath();
        newRef.setPath(path.substring(0, path.length() - 1));

        getResponse().setStatus(Status.MOVED_PERMANENTLY.getCode());
        getResponse().setHeader("Location", newRef.toString());
    }

}

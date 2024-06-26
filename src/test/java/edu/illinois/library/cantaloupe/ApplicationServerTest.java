package edu.illinois.library.cantaloupe;

import edu.illinois.library.cantaloupe.test.BaseTest;
import edu.illinois.library.cantaloupe.test.TestUtil;
import edu.illinois.library.cantaloupe.util.SocketUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static edu.illinois.library.cantaloupe.test.Assert.HTTPAssert.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationServerTest extends BaseTest {

    private static int HTTP_PORT, HTTPS_PORT;

    private ApplicationServer instance;

    @BeforeAll
    public static void beforeClass() throws Exception {
        BaseTest.beforeClass();
        int[] ports = SocketUtils.getOpenPorts(2);
        HTTP_PORT   = ports[0];
        HTTPS_PORT  = ports[1];
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        instance = new ApplicationServer();
    }

    @AfterEach
    public void tearDown() throws Exception {
        instance.stop();
    }

    @Test
    void getAcceptQueueLimit() {
        // default
        assertEquals(0, instance.getAcceptQueueLimit());
        // explicitly set
        instance.setAcceptQueueLimit(0);
        assertEquals(0, instance.getAcceptQueueLimit());
    }

    @Test
    void getHTTPHost() {
        // default
        assertEquals(ApplicationServer.DEFAULT_HTTP_HOST,
                instance.getHTTPHost());
        // explicitly set
        instance.setHTTPHost("127.0.0.1");
        assertEquals("127.0.0.1", instance.getHTTPHost());
    }

    @Test
    void getHTTPPort() {
        // default
        assertEquals(ApplicationServer.DEFAULT_HTTP_PORT,
                instance.getHTTPPort());
        // explicitly set
        instance.setHTTPPort(5000);
        assertEquals(5000, instance.getHTTPPort());
    }

    @Test
    void getHTTPSHost() {
        // default
        assertEquals(ApplicationServer.DEFAULT_HTTPS_HOST,
                instance.getHTTPSHost());
        // explicitly set
        instance.setHTTPSHost("127.0.0.1");
        assertEquals("127.0.0.1", instance.getHTTPSHost());
    }

    @Test
    void getHTTPSKeyPassword() {
        // default
        assertNull(instance.getHTTPSKeyPassword());
        // explicitly set
        instance.setHTTPSKeyPassword("cats");
        assertEquals("cats", instance.getHTTPSKeyPassword());
    }

    @Test
    void getHTTPSKeyStorePassword() {
        // default
        assertNull(instance.getHTTPSKeyStorePassword());
        // explicitly set
        instance.setHTTPSKeyStorePassword("cats");
        assertEquals("cats", instance.getHTTPSKeyStorePassword());
    }

    @Test
    void getHTTPSKeyStorePath() {
        // default
        assertNull(instance.getHTTPSKeyStorePath());
        // explicitly set
        instance.setHTTPSKeyStorePath("/cats");
        assertEquals("/cats", instance.getHTTPSKeyStorePath());
    }

    @Test
    void getHTTPSKeyStoreType() {
        // default
        assertNull(instance.getHTTPSKeyStoreType());
        // explicitly set
        instance.setHTTPSKeyStoreType("cats");
        assertEquals("cats", instance.getHTTPSKeyStoreType());
    }

    @Test
    void getHTTPSPort() {
        // default
        assertEquals(ApplicationServer.DEFAULT_HTTPS_PORT,
                instance.getHTTPSPort());
        // explicitly set
        instance.setHTTPSPort(5000);
        assertEquals(5000, instance.getHTTPSPort());
    }

    @Test
    void getMaxThreads() {
        // default
        assertEquals(ApplicationServer.DEFAULT_MAX_THREADS,
                instance.getMaxThreads());
        // explicitly set
        instance.setMaxThreads(45);
        assertEquals(45, instance.getMaxThreads());
    }

    @Test
    void getMinThreads() {
        // default
        assertEquals(ApplicationServer.DEFAULT_MIN_THREADS,
                instance.getMinThreads());
        // explicitly set
        instance.setMinThreads(45);
        assertEquals(45, instance.getMinThreads());
    }

    @Test
    void isHTTPEnabled() {
        // default
        assertFalse(instance.isHTTPEnabled());
        // explicitly set
        instance.setHTTPEnabled(true);
        assertTrue(instance.isHTTPEnabled());
    }

    @Test
    void isHTTPSEnabled() {
        // default
        assertFalse(instance.isHTTPSEnabled());
        // explicitly set
        instance.setHTTPSEnabled(true);
        assertTrue(instance.isHTTPSEnabled());
    }

    @Test
    void isStarted() throws Exception {
        assertFalse(instance.isStarted());
        assertTrue(instance.isStopped());
        instance.start();
        assertTrue(instance.isStarted());
        assertFalse(instance.isStopped());
    }

    @Test
    void isStopped() throws Exception {
        isStarted();
    }

    @Test
    void startStartsHTTPServer() throws Exception {
        initializeHTTP();
        instance.start();
        assertStatus(200, "http://localhost:" + HTTP_PORT);
    }

    @Test
    void startStartsHTTPSServerWithJKSKeyStoreWithPassword() throws Exception {
        initializeHTTPSWithJKSKeyStoreWithPassword();
        instance.setHTTPEnabled(false);
        instance.start();
        assertStatus(200, "https://localhost:" + HTTPS_PORT);
    }

    @Test
    void startStartsHTTPSServerWithPKCS12KeyStoreWithPassword()
            throws Exception {
        initializeHTTPSWithPKCS12KeyStoreWithPassword();
        instance.setHTTPEnabled(false);
        instance.start();
        assertStatus(200, "https://localhost:" + HTTPS_PORT);
    }


    @Disabled // TODO: this fails in Jetty 9.4.24
    @Test
    void startStartsHTTPSServerWithPKCS12KeyStoreWithoutPassword()
            throws Exception {
        initializeHTTPSWithPKCS12KeyStoreWithoutPassword();
        instance.setHTTPEnabled(false);
        instance.start();
        assertStatus(200, "https://localhost:" + HTTPS_PORT);
    }

    @Test
    void startStartsInsecureHTTP2Server() {
        // TODO: write this
    }

    @Test
    void startStartsSecureHTTP2Server() {
        // TODO: write this
    }

    @Test
    void stopStopsHTTPServer() throws Exception {
        initializeHTTP();
        try {
            instance.start();
        } finally {
            instance.stop();
        }
        assertConnectionRefused("http://localhost:" + HTTP_PORT);
    }

    @Test
    void stopStopsHTTPSServerWithJKSKeyStoreWithPassword() throws Exception {
        initializeHTTPSWithJKSKeyStoreWithPassword();
        try {
            instance.start();
        } finally {
            instance.stop();
        }
        assertConnectionRefused("https://localhost:" + HTTPS_PORT);
    }

    @Test
    void stopStopsHTTPSServerWithPKCS12KeyStoreWithPassword()
            throws Exception {
        initializeHTTPSWithPKCS12KeyStoreWithPassword();
        try {
            instance.start();
        } finally {
            instance.stop();
        }
        assertConnectionRefused("https://localhost:" + HTTPS_PORT);
    }

    @Test
    void stopStopsHTTPSServerWithPKCS12KeyStoreWithoutPassword()
            throws Exception {
        initializeHTTPSWithPKCS12KeyStoreWithoutPassword();
        try {
            instance.start();
        } finally {
            instance.stop();
        }
        assertConnectionRefused("https://localhost:" + HTTPS_PORT);
    }

    private void initializeHTTP() {
        instance.setHTTPEnabled(true);
        instance.setHTTPPort(HTTP_PORT);
    }

    private void initializeHTTPSWithJKSKeyStoreWithPassword() {
        instance.setHTTPSEnabled(true);
        instance.setHTTPSPort(HTTPS_PORT);
        instance.setHTTPSKeyStorePath(TestUtil.getFixture("keystore-password.jks").toString());
        instance.setHTTPSKeyStorePassword("password");
        instance.setHTTPSKeyStoreType("JKS");
        instance.setHTTPSKeyPassword("password");
    }

    private void initializeHTTPSWithPKCS12KeyStoreWithPassword() {
        instance.setHTTPSEnabled(true);
        instance.setHTTPSPort(HTTPS_PORT);
        instance.setHTTPSKeyStorePath(TestUtil.getFixture("keystore-password.p12").toString());
        instance.setHTTPSKeyStorePassword("password");
        instance.setHTTPSKeyStoreType("PKCS12");
        instance.setHTTPSKeyPassword("password");
    }

    private void initializeHTTPSWithPKCS12KeyStoreWithoutPassword() {
        instance.setHTTPSEnabled(true);
        instance.setHTTPSPort(HTTPS_PORT);
        instance.setHTTPSKeyStorePath(TestUtil.getFixture("keystore-nopass.p12").toString());
        instance.setHTTPSKeyStoreType("PKCS12");
    }

}

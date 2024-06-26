package edu.illinois.library.cantaloupe.resource;

import edu.illinois.library.cantaloupe.delegate.JavaRequestContext;
import edu.illinois.library.cantaloupe.http.Reference;
import edu.illinois.library.cantaloupe.image.Dimension;
import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.image.Metadata;
import edu.illinois.library.cantaloupe.image.ScaleConstraint;
import edu.illinois.library.cantaloupe.operation.OperationList;
import edu.illinois.library.cantaloupe.delegate.JavaContext;

import java.net.URI;
import java.util.Map;

/**
 * <p>Contains information about a client request.</p>
 *
 * <p>Developer note: adding, removing, or changing any of the properties also
 * requires updating {@link RequestContextMap}.</p>
 *
 * <p>Developer note: this class' properties need to be kept in sync with
 * {@link JavaContext}.</p>
 *
 * @see RequestContextMap
 */
public final class RequestContext {

    private String clientIPAddress;
    private Map<String,String> cookies;
    private Dimension fullSize;
    private Identifier identifier;
    private Reference localURI;
    private Metadata metadata;
    private OperationList operations;
    private Format outputFormat;
    private Integer pageCount;
    private Integer pageNumber;
    private Map<String,String> requestHeaders;
    private Reference requestURI;
    private Dimension resultingSize;
    private ScaleConstraint scaleConstraint;

    public String getClientIP() {
        return clientIPAddress;
    }

    public Map<String,String> getCookies() {
        return cookies;
    }

    public Dimension getFullSize() {
        return fullSize;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Reference getLocalURI() {
        return localURI;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public OperationList getOperationList() {
        return operations;
    }

    public Format getOutputFormat() {
        return outputFormat;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Map<String,String> getRequestHeaders() {
        return requestHeaders;
    }

    public Reference getRequestURI() {
        return requestURI;
    }

    public Dimension getResultingSize() {
        return resultingSize;
    }

    public ScaleConstraint getScaleConstraint() {
        return scaleConstraint;
    }

    /**
     * @param clientIP May be {@code null}.
     */
    public void setClientIP(String clientIP) {
        this.clientIPAddress = clientIP;
    }

    /**
     * @param cookies May be {@code null}.
     */
    public void setCookies(Map<String,String> cookies) {
        this.cookies = cookies;
    }

    /**
     * N.B.: {@link #setOperationList(OperationList, Dimension)} also sets
     * this.
     *
     * @param fullSize May be {@code null}.
     */
    public void setFullSize(Dimension fullSize) {
        this.fullSize = fullSize;
    }

    /**
     * @param identifier May be {@code null}.
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @param uri URI seen by the application. May be {@code null}.
     * @see #setRequestURI(Reference)
     */
    public void setLocalURI(Reference uri) {
        this.localURI = uri;
    }

    /**
     * @param metadata May be {@code null}.
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * @param opList   May be {@code null}.
     * @param fullSize May be {@code null}.
     */
    public void setOperationList(OperationList opList, Dimension fullSize) {
        if (opList != null && fullSize != null) {
            this.fullSize      = fullSize;
            this.identifier    = opList.getIdentifier();
            this.operations    = opList;
            this.outputFormat  = opList.getOutputFormat();
            this.resultingSize = opList.getResultingSize(fullSize);
            if (opList.getMetaIdentifier() != null) {
                this.pageNumber      = opList.getMetaIdentifier().getPageNumber();
                this.scaleConstraint = opList.getMetaIdentifier().getScaleConstraint();
            }
        } else {
            this.fullSize        = null;
            this.identifier      = null;
            this.operations      = null;
            this.outputFormat    = null;
            this.pageNumber      = null;
            this.resultingSize   = null;
            this.scaleConstraint = null;
        }
    }

    /**
     * @param pageCount May be {@code null}.
     */
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * @param pageNumber May be {@code null}.
     */
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * @param requestHeaders May be {@code null}.
     */
    public void setRequestHeaders(Map<String,String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * @param uri URI requested by the client .May be {@code null}.
     * @see #setLocalURI(Reference)
     */
    public void setRequestURI(Reference uri) {
        this.requestURI = uri;
    }

    /**
     * @param scaleConstraint May be {@code null}.
     */
    public void setScaleConstraint(ScaleConstraint scaleConstraint) {
        this.scaleConstraint = scaleConstraint;
    }

    /**
     * @return New instance backed by this instance.
     */
    public JavaContext toJavaContext() {
        return new JavaRequestContext(this);
    }

    /**
     * @return &quot;Live view&quot; map representation of the instance.
     * @see RequestContextMap for available keys.
     */
    public Map<String,Object> toMap() {
        return new RequestContextMap<>(this);
    }

}

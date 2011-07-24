package com.bertvanbrakel.android.rest;

/**
 * Thrown to indicate there was an error while invoking the RestClient
 */
public class RestClientException extends Exception {

    private static final long serialVersionUID = 8379272548450351948L;

    public RestClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RestClientException(final String message) {
        super(message);
    }

}

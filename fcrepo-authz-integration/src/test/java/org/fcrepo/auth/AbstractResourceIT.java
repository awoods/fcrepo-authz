
package org.fcrepo.auth;

/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResourceIT {

    protected Logger logger;

    protected String OBJECT_PATH = "objects";

    @Before
    public void setLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected static final int SERVER_PORT = Integer.parseInt(System
            .getProperty("test.port", "8080"));

    /**
     * The context path of the application (including the leading "/"), set as
     * system property by maven-failsafe-plugin.
     */
    private static final String CONTEXT_PATH = System
            .getProperty("test.context.path");

    protected static final String HOSTNAME = "localhost";

    protected static final String serverAddress = "http://" + HOSTNAME +
            ":" + SERVER_PORT + CONTEXT_PATH;

    protected final PoolingClientConnectionManager connectionManager =
            new PoolingClientConnectionManager();

    protected static HttpClient client;

    public AbstractResourceIT() {
        connectionManager.setMaxTotal(Integer.MAX_VALUE);
        connectionManager.setDefaultMaxPerRoute(5);
        connectionManager.closeIdleConnections(3, TimeUnit.SECONDS);
        client = new DefaultHttpClient(connectionManager);
    }

    protected static HttpPost postObjMethod(final String pid) {
        return new HttpPost(serverAddress + "rest/" + pid);
    }

    protected static HttpPost postObjMethod(final String pid,
            final String query) {
        if (query.equals("")) {
            return new HttpPost(serverAddress + "rest/" + pid);
        } else {
            return new HttpPost(serverAddress + "rest/" + pid + "?" +
                    query);
        }
    }

    protected static HttpPost postDSMethod(final String pid,
            final String ds, final String content)
            throws UnsupportedEncodingException {
        final HttpPost post =
                new HttpPost(serverAddress + "rest/" + pid + "/" + ds +
                        "/fcr:content");
        post.setEntity(new StringEntity(content));
        return post;
    }

    protected static HttpPut putDSMethod(final String pid,
            final String ds, final String content)
            throws UnsupportedEncodingException {
        final HttpPut put =
                new HttpPut(serverAddress + "rest/" + pid + "/" + ds +
                        "/fcr:content");

        put.setEntity(new StringEntity(content));
        return put;
    }

    protected HttpResponse execute(final HttpUriRequest method)
            throws ClientProtocolException, IOException {
        logger.debug("Executing: " + method.getMethod() + " to " +
                method.getURI());
        return client.execute(method);
    }

    protected int getStatus(final HttpUriRequest method)
            throws ClientProtocolException, IOException {
        final HttpResponse response = execute(method);
        final int result = response.getStatusLine().getStatusCode();
        if (!(result > 199) || !(result < 400)) {
            logger.warn(EntityUtils.toString(response.getEntity()));
        }
        return result;
    }

}

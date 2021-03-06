/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.filters.custom.helloworldjava;

import org.openrepose.filters.custom.helloworldjava.config.HelloWorldJavaConfig;
import org.openrepose.filters.custom.helloworldjava.config.Message;
import org.openrepose.filters.custom.helloworldjava.config.MessageList;
import org.openrepose.commons.config.manager.UpdateListener;
import org.openrepose.commons.utils.servlet.http.MutableHttpServletRequest;
import org.openrepose.commons.utils.servlet.http.MutableHttpServletResponse;
import org.openrepose.core.filter.FilterConfigHelper;
import org.openrepose.core.services.config.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;


@Named
public class HelloWorldJavaFilter implements Filter, UpdateListener<HelloWorldJavaConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldJavaFilter.class);
    private static final String DEFAULT_CONFIG = "hello-world-java.cfg.xml";
    private final ConfigurationService configurationService;
    private String configurationFile = DEFAULT_CONFIG;
    private HelloWorldJavaConfig configuration = null;
    private boolean initialized = false;

    @Inject
    public HelloWorldJavaFilter(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        configurationFile = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG);
        LOG.info("Initializing filter using config " + configurationFile);
        // Must match the .xsd file created in step 18.
        URL xsdURL = getClass().getResource("/META-INF/schema/config/hello-world-java.xsd");
        configurationService.subscribeTo(
                filterConfig.getFilterName(),
                configurationFile,
                xsdURL,
                this,
                HelloWorldJavaConfig.class
        );
    }

    @Override
    public void destroy() {
        configurationService.unsubscribeFrom(configurationFile, this);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!initialized) {
            LOG.error("Hello World Java filter has not yet initialized...");
            ((HttpServletResponse) servletResponse).sendError(500);
        } else {
            MutableHttpServletRequest mutableHttpRequest = MutableHttpServletRequest.wrap((HttpServletRequest) servletRequest);
            MutableHttpServletResponse mutableHttpResponse = MutableHttpServletResponse.wrap(mutableHttpRequest, (HttpServletResponse) servletResponse);

            // This is where this filter's custom logic is invoked.
            // For the purposes of this example, the configured messages are logged
            // before and after the Filter Chain is processed.
            LOG.trace("Hello World Java filter processing request...");
            MessageList messageList = configuration.getMessages();
            for (Message message : messageList.getMessage()) {
                LOG.info("Request  message: " + message.getValue());
            }

            LOG.trace("Hello World Java filter passing on down the Filter Chain...");
            filterChain.doFilter(mutableHttpRequest, mutableHttpResponse);

            LOG.trace("Hello World Java filter processing response...");
            for (Message message : messageList.getMessage()) {
                LOG.info("Response message: " + message.getValue());
            }
        }
        LOG.trace("Hello World filter returning response...");
    }

    // This class is generated from.xsd file.
    @Override
    public void configurationUpdated(HelloWorldJavaConfig configurationObject) {
        configuration = configurationObject;
        MessageList messageList = configuration.getMessages();
        for (Message message : messageList.getMessage()) {
            LOG.info("Update   message: " + message.getValue());
        }
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}

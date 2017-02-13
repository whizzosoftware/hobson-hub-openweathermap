/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.device.HobsonDeviceDescriptor;
import com.whizzosoftware.hobson.api.device.proxy.HobsonDeviceProxy;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.openweathermap.action.AddDeviceActionProvider;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A plugin that retrieves weather information from OpenWeatherMap.org.
 *
 * @author Dan Noguerol
 */
public class OpenWeatherMapPlugin extends AbstractHttpClientPlugin {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapPlugin.class);

    static final String PROP_API_KEY = "apiKey";

    private String apiKey;
    private boolean startupCompleted;

    public OpenWeatherMapPlugin(String pluginId, String version, String description) {
        super(pluginId, version, description);
    }

    @Override
    public String getName() {
        return "OpenWeatherMap";
    }

    @Override
    public void onStartup(PropertyContainer config) {
        // process config
        try {
            configureApiKey(config);
        } catch (Exception e) {
            logger.error("Error starting OpenWeatherMap plugin", e);
            setStatus(PluginStatus.failed("Error starting OpenWeatherMap plugin"));
        }
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
        try {
            logger.debug("Configuration has changed");
            configureApiKey(config);
        } catch (Exception e) {
            logger.error("Error updating configuration", e);
        }
    }

    @Override
    public long getRefreshInterval() {
        return 300;
    }

    @Override
    public void onRefresh() {
        if (apiKey != null) {
            for (HobsonDeviceProxy d : getDeviceProxies()) {
                try {
                    URI uri = createUri(d.getContext().getDeviceId());
                    logger.debug("Requesting OpenWeatherMap data from {}", uri);
                    String id = d.getContext().getDeviceId();
                    sendHttpRequest(uri, HttpRequest.Method.GET, id);
                } catch (Exception e) {
                    logger.error("Error retrieving data from OpenWeatherMap", e);
                }
            }
        }
    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return new TypedProperty[] {
            new TypedProperty.Builder(PROP_API_KEY, "API Key", "The OpenWeatherMap API key to use for requests", TypedProperty.Type.STRING).build(),
        };
    }

    @Override
    public void onHttpResponse(HttpResponse response, Object context) {
        logger.trace("Received HTTP response for {}", context);
        try {
            if (response.getStatusCode() == 200) {
                onHttpResponse(new JSONObject(new JSONTokener(response.getBody())), (String)context);
            } else {
                logger.error("Error retrieving data from OpenWeatherMap (" + response.getStatusCode() + ")");
            }
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
        }
    }

    private void onHttpResponse(JSONObject json, String deviceId) {
        logger.trace("Received JSON response for device {}: {}", deviceId, json);
        OpenWeatherMapDevice device;
        try {
            device = (OpenWeatherMapDevice)getDeviceProxy(deviceId);
            device.onUpdate(json);
        } catch (HobsonNotFoundException e) {
            logger.trace("Publishing new device: {}", deviceId);
            String name = "OpenWeatherMap Station";
            if (json.has("name")) {
                name = json.getString("name");
            }
            publishDeviceProxy(new OpenWeatherMapDevice(this, deviceId, name, json));
        }
    }

    @Override
    public void onHttpRequestFailure(Throwable cause, Object context) {
        logger.error("Error retrieving data from OpenWeatherMap", cause);
    }

    public void addCityId(String cityId) {
        try {
            URI uri = createUri(cityId);
            logger.debug("Requesting OpenWeatherMap data from {}", uri);
            sendHttpRequest(uri, HttpRequest.Method.GET, null, cityId);
        } catch (URISyntaxException e) {
            logger.error("Error adding city ID: " + cityId, e);
        }
    }

    private void configureApiKey(PropertyContainer config) throws Exception {
        if (config != null) {
            apiKey = (String)config.getPropertyValue(PROP_API_KEY);
            if (apiKey != null) {
                performStartup();
            }
        }

        if (apiKey == null) {
            logger.debug("No plugin configuration; unable to query OpenWeatherMap server");
            setStatus(PluginStatus.notConfigured("API key is not set in plugin configuration"));
        }
    }

    private void performStartup() {
        if (!startupCompleted) {
            publishActionProvider(new AddDeviceActionProvider(this));

            for (HobsonDeviceDescriptor d : getPublishedDeviceDescriptions()) {
                publishDeviceProxy(new OpenWeatherMapDevice(this, d.getContext().getDeviceId(), d.getName(),null));
            }

            startupCompleted = true;
            setStatus(PluginStatus.running());

            onRefresh();
        }
    }

    private URI createUri(String cityId) throws URISyntaxException {
        return new URI("http", "api.openweathermap.org", "/data/2.5/weather", "id=" + cityId + "&APPID=" + apiKey, null);
    }
}

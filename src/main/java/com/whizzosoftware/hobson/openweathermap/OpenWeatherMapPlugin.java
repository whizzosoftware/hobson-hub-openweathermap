/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * A plugin that retrieves the current external temperature from OpenWeatherMap.org.
 *
 * @author Dan Noguerol
 */
public class OpenWeatherMapPlugin extends AbstractHttpClientPlugin {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapPlugin.class);

    protected static final String PROP_API_KEY = "api.key";
    protected static final String PROP_CITY_STATE = "city.state";

    private String cityState;
    private URI uri;
    private OpenWeatherMapDevice device;

    public OpenWeatherMapPlugin(String pluginId) {
        super(pluginId);
    }

    @Override
    public String getName() {
        return "OpenWeatherMap";
    }

    @Override
    public void onStartup(PropertyContainer config) {
        try {
            createUri(config);
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
            createUri(config);
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
        if (uri != null) {
            try {
                logger.debug("Requesting OpenWeatherMap data from {}", uri);
                sendHttpRequest(uri, HttpRequest.Method.GET, null);
            } catch (Exception e) {
                logger.error("Error retrieving data from OpenWeatherMap", e);
            }
        } else {
            logger.debug("OpenWeatherMap plugin is not configured properly. Please configure correctly and restart.");
        }
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder(PROP_API_KEY, "API Key", "The OpenWeatherMap API key to use for requests", TypedProperty.Type.STRING).build(),
            new TypedProperty.Builder(PROP_CITY_STATE, "City, State", "The city and state from which you want the current conditions reported (format: City, State)", TypedProperty.Type.STRING).build()
        };
    }

    @Override
    public void onHttpResponse(HttpResponse response, Object context) {
        try {
            if (response.getStatusCode() == 200) {
                JSONObject json = new JSONObject(new JSONTokener(response.getBody()));
                try {
                    device.onUpdate(json);
                } catch (JSONException e) {
                    logger.error("Unknown OpenWeatherMap JSON response: {}", json.toString());
                }
            } else {
                logger.error("Error retrieving data from OpenWeatherMap (" + response.getStatusCode() + ")");
            }
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
        }
    }

    @Override
    public void onHttpRequestFailure(Throwable cause, Object context) {
        logger.error("Error retrieving data from OpenWeatherMap", cause);
    }

    private void createUri(PropertyContainer config) throws Exception {
        if (config != null) {
            String s = (String)config.getPropertyValue(PROP_CITY_STATE);
            if (s != null && (cityState == null || !cityState.equals(s))) {
                this.cityState = s;
                String queryString = "q=" + cityState;
                String apiKey = (String)config.getPropertyValue(PROP_API_KEY);
                if (apiKey != null) {
                    queryString += "&APPID=" + apiKey;
                }
                uri = new URI("http", "api.openweathermap.org", "/data/2.5/weather", queryString, null);
            }
        }

        if (uri != null) {
            logger.debug("Using URI: {}", uri.toASCIIString());
            setStatus(PluginStatus.running());
            if (device == null) {
                device = new OpenWeatherMapDevice(this, "station");
                publishDevice(device);
            }
            onRefresh();
        } else {
            logger.debug("No plugin configuration; unable to query OpenWeatherMap server");
            setStatus(PluginStatus.notConfigured("City/state is not set in plugin configuration"));
        }
    }
}

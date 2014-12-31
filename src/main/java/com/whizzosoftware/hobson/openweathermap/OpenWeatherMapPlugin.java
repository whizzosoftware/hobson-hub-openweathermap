/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.config.ConfigurationPropertyMetaData;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * A plugin that retrieves the current external temperature from OpenWeatherMap.org.
 *
 * @author Dan Noguerol
 */
public class OpenWeatherMapPlugin extends AbstractHttpClientPlugin {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String PROP_CITY_STATE = "city.state";

    private String cityState;
    private URI uri;
    private boolean varsPublished = false;

    public OpenWeatherMapPlugin(String pluginId) {
        super(pluginId);
    }

    @Override
    public String getName() {
        return "OpenWeatherMap";
    }

    @Override
    public void onStartup(Dictionary config) {
        addConfigurationPropertyMetaData(new ConfigurationPropertyMetaData(PROP_CITY_STATE, "City, State", "The city and state from which you want the current conditions reported (format: City, State)", ConfigurationPropertyMetaData.Type.STRING));
        try {
            createUri(config);
        } catch (Exception e) {
            logger.error("Error starting OpenWeatherMap plugin", e);
            setStatus(new PluginStatus(PluginStatus.Status.FAILED, "Error starting OpenWeatherMap plugin"));
        }
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onPluginConfigurationUpdate(Dictionary config) {
        try {
            logger.debug("Configuration has changed");
            createUri(config);
        } catch (Exception e) {
            logger.error("Error updating configuration", e);
        }
    }

    @Override
    public void onSetDeviceVariable(String deviceId, String variableName, Object value) {
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
                sendHttpGetRequest(uri, null, null);
            } catch (Exception e) {
                logger.error("Error retrieving data from OpenWeatherMap", e);
            }
        } else {
            logger.debug("OpenWeatherMap plugin is not configured properly. Please configure correctly and restart.");
        }
    }

    @Override
    protected void onHttpResponse(int statusCode, List<Map.Entry<String, String>> headers, String response, Object context) {
        if (statusCode == 200) {
            JSONObject json = new JSONObject(new JSONTokener(response));
            try {
                List<VariableUpdate> updates = parseServerResponse(json);
                // set the variables that have changed
                if (!updates.isEmpty()) {
                    fireVariableUpdateNotifications(updates);
                }
            } catch (JSONException e) {
                logger.error("Unknown OpenWeatherMap JSON response: {}", json.toString());
            }
        } else {
            logger.error("Error retrieving data from OpenWeatherMap (" + statusCode + ")");
        }
    }

    @Override
    protected void onHttpRequestFailure(Throwable cause, Object context) {
        logger.error("Error retrieving data from OpenWeatherMap", cause);
    }

    protected List<VariableUpdate> parseServerResponse(JSONObject response) throws JSONException {
        List<VariableUpdate> updates = new ArrayList<>();

        JSONObject obsObj = response.getJSONObject("main");

        // determine which variables have changed and their values
        Double d = obsObj.getDouble("temp");
        logger.debug("Temperature in Kelvin is {}", d);
        Double tempC = d - 273.15;
        Double tempF = tempC * 1.8 + 32;
        updates.add(new VariableUpdate(getId(), VariableConstants.TEMP_C, tempC));
        updates.add(new VariableUpdate(getId(), VariableConstants.TEMP_F, tempF));

        logger.debug("Successfully retrieved OpenWeatherMap data");

        return updates;
    }

    private void createUri(Dictionary config) throws Exception {
        if (config != null) {
            String s = (String) config.get("city.state");
            if (s != null && (cityState == null || !cityState.equals(s))) {
                this.cityState = s;
                uri = new URI("http", "api.openweathermap.org", "/data/2.5/weather", "q=" + cityState, null);
            }
        }

        if (uri != null) {
            logger.debug("Using URI: {}", uri.toASCIIString());
            setStatus(new PluginStatus(PluginStatus.Status.RUNNING));
            if (!varsPublished) {
                publishGlobalVariable(VariableConstants.TEMP_C, null, HobsonVariable.Mask.READ_ONLY);
                publishGlobalVariable(VariableConstants.TEMP_F, null, HobsonVariable.Mask.READ_ONLY);
                varsPublished = true;
            }
            onRefresh();
        } else {
            logger.debug("No plugin configuration; unable to query OpenWeatherMap server");
            setStatus(new PluginStatus(PluginStatus.Status.NOT_CONFIGURED, "City/state is not set in plugin configuration"));
        }
    }
}

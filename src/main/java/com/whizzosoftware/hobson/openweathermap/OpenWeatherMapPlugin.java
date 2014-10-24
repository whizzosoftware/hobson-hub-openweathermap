/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableImpl;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.bootstrap.api.config.ConfigurationMetaData;
import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * A plugin that retrieves the current external temperature from OpenWeatherMap.org.
 *
 * @author Dan Noguerol
 */
public class OpenWeatherMapPlugin extends AbstractHobsonPlugin {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String PROP_CITY_STATE = "city.state";

    private HttpClient client = new HttpClient(new SimpleHttpConnectionManager(true));
    private String cityState;
    private String uri;

    public OpenWeatherMapPlugin(String pluginId) {
        super(pluginId);
    }

    @Override
    public String getName() {
        return "OpenWeatherMap";
    }

    @Override
    public void onStartup(Dictionary config) {
        addConfigurationMetaData(new ConfigurationMetaData(PROP_CITY_STATE, "City, State", "The city and state from which you want the current conditions reported (format: City, State)", ConfigurationMetaData.Type.STRING));

        try {
            createUri(config);
        } catch (IOException e) {
            setStatus(new PluginStatus(PluginStatus.Status.FAILED, "Error starting OpenWeatherMap plugin"));
        }
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onPluginConfigurationUpdate(Dictionary config) {
        try {
            createUri(config);
        } catch (IOException e) {
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
                logger.debug("Connecting to {}", uri);
                GetMethod get = new GetMethod(uri);
                int statusCode = client.executeMethod(get);
                if (statusCode == 200) {
                    JSONObject json = new JSONObject(new JSONTokener(get.getResponseBodyAsStream()));
                    List<VariableUpdate> updates = parseServerResponse(json);
                    // set the variables that have changed
                    if (!updates.isEmpty()) {
                        fireVariableUpdateNotifications(updates);
                    }
                }
            } catch (Exception e) {
                logger.error("Error retrieving data from OpenWeatherMap", e);
            }
        } else {
            logger.debug("OpenWeatherMap plugin is not configured properly. Please configure correctly and restart.");
        }
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

    private void createUri(Dictionary config) throws IOException {
        if (config != null) {
            String s = (String) config.get("city.state");
            if (s != null && (cityState == null || !cityState.equals(s))) {
                this.cityState = s;
                uri = "http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(cityState, "UTF8");
            }
        }

        if (uri != null) {
            setStatus(new PluginStatus(PluginStatus.Status.RUNNING));
            publishGlobalVariable(new HobsonVariableImpl(VariableConstants.TEMP_C, null, HobsonVariable.Mask.READ_ONLY));
            publishGlobalVariable(new HobsonVariableImpl(VariableConstants.TEMP_F, null, HobsonVariable.Mask.READ_ONLY));
        } else {
            setStatus(new PluginStatus(PluginStatus.Status.NOT_CONFIGURED, "City/state is not set in plugin configuration"));
        }
    }
}

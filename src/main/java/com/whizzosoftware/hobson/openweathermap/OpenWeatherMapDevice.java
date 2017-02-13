/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.device.proxy.AbstractHobsonDeviceProxy;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableMask;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A device representing an OpenWeatherMap weather station.
 *
 * @author Dan Noguerol
 */
public class OpenWeatherMapDevice extends AbstractHobsonDeviceProxy {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapDevice.class);

    private JSONObject initialJson;

    OpenWeatherMapDevice(HobsonPlugin plugin, String cityId, String name, JSONObject json) {
        super(plugin, cityId, name, DeviceType.WEATHER_STATION);
        this.initialJson = json;
    }

    @Override
    public void onStartup(String name, Map<String,Object> config) {
        // publish the variables
        publishVariables(
            createDeviceVariable(VariableConstants.OUTDOOR_TEMP_C, VariableMask.READ_ONLY),
            createDeviceVariable(VariableConstants.OUTDOOR_TEMP_F, VariableMask.READ_ONLY),
            createDeviceVariable(VariableConstants.OUTDOOR_RELATIVE_HUMIDITY, VariableMask.READ_ONLY),
            createDeviceVariable(VariableConstants.WIND_DIRECTION_DEGREES, VariableMask.READ_ONLY),
            createDeviceVariable(VariableConstants.WIND_SPEED_MPH, VariableMask.READ_ONLY)
        );

        // process any initial JSON we received
        if (initialJson != null) {
            onUpdate(initialJson);
            initialJson = null;
        }
    }

    @Override
    public void onShutdown() {
    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return null;
    }

    @Override
    public String getManufacturerName() {
        return "OpenWeatherMap";
    }

    @Override
    public String getManufacturerVersion() {
        return null;
    }

    @Override
    public String getModelName() {
        return null;
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.OUTDOOR_TEMP_F;
    }

    @Override
    public void onDeviceConfigurationUpdate(Map<String, Object> config) {

    }

    @Override
    public void onSetVariables(Map<String, Object> values) {

    }

    void onUpdate(JSONObject response) {
        Map<String,Object> updates = new HashMap<>();

        if (response.has("main")) {
            JSONObject obsObj = response.getJSONObject("main");

            // set temperature
            if (obsObj.has("temp")) {
                Double d = obsObj.getDouble("temp");
                logger.trace("Temperature in Kelvin is {}", d);
                Double tempC = d - 273.15;
                Double tempF = tempC * 1.8 + 32;
                updates.put(VariableConstants.OUTDOOR_TEMP_C, tempC);
                updates.put(VariableConstants.OUTDOOR_TEMP_F, tempF);
            }

            // set humidity
            if (obsObj.has("humidity")) {
                Double d = obsObj.getDouble("humidity");
                logger.trace("Humidity is {}", d);
                updates.put(VariableConstants.OUTDOOR_RELATIVE_HUMIDITY, d);
            }
        } else {
            logger.error("Received malformed JSON (missing main) from OpenWeatherMap");
        }

        // send wind info
        if (response.has("wind")) {
            JSONObject obsObj = response.getJSONObject("wind");
            if (obsObj.has("speed")) {
                updates.put(VariableConstants.WIND_SPEED_MPH, obsObj.getDouble("speed"));
            }
            if (obsObj.has("deg")) {
                updates.put(VariableConstants.WIND_DIRECTION_DEGREES, obsObj.getDouble("deg"));
            }
        }

        logger.debug("Successfully retrieved OpenWeatherMap data");
        setLastCheckin(System.currentTimeMillis());

        // set new variables
        if (updates.size() > 0) {
            setVariableValues(updates);
        }
    }
}

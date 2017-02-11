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

public class OpenWeatherMapDevice extends AbstractHobsonDeviceProxy {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapDevice.class);

    public OpenWeatherMapDevice(HobsonPlugin plugin, String id) {
        super(plugin, id, "OpenWeatherMap Station", DeviceType.WEATHER_STATION);
    }

    @Override
    public void onStartup(String name, Map<String,Object> config) {
        publishVariables(
            createDeviceVariable(VariableConstants.OUTDOOR_TEMP_C, VariableMask.READ_ONLY),
            createDeviceVariable(VariableConstants.OUTDOOR_TEMP_F, VariableMask.READ_ONLY)
        );
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

    public void onUpdate(JSONObject response) {
        Map<String,Object> updates = new HashMap<>();

        if (response.has("main")) {
            JSONObject obsObj = response.getJSONObject("main");
            if (obsObj.has("temp")) {
                // determine which variables have changed and their values
                Double d = obsObj.getDouble("temp");

                logger.debug("Temperature in Kelvin is {}", d);

                Double tempC = d - 273.15;
                Double tempF = tempC * 1.8 + 32;
                updates.put(VariableConstants.OUTDOOR_TEMP_C, tempC);
                updates.put(VariableConstants.OUTDOOR_TEMP_F, tempF);

                logger.debug("Successfully retrieved OpenWeatherMap data");

                setVariableValues(updates);

                setLastCheckin(System.currentTimeMillis());
            } else {
                logger.error("Received malformed JSON (missing temp) from OpenWeatherMap");
            }
        } else {
            logger.error("Received malformed JSON (missing main) from OpenWeatherMap");
        }
    }
}

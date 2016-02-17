/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OpenWeatherMapDevice extends AbstractHobsonDevice {
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapDevice.class);

    public OpenWeatherMapDevice(HobsonPlugin plugin, String id) {
        super(plugin, id);

        setDefaultName("OpenWeatherMap Station");
    }

    @Override
    public void onStartup(PropertyContainer config) {
        super.onStartup(config);

        publishVariable(VariableConstants.OUTDOOR_TEMP_C, null, HobsonVariable.Mask.READ_ONLY, null);
        publishVariable(VariableConstants.OUTDOOR_TEMP_F, null, HobsonVariable.Mask.READ_ONLY, null);
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.WEATHER_STATION;
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onSetVariable(String s, Object o) {
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.OUTDOOR_TEMP_F;
    }

    public void onUpdate(JSONObject response) {
        List<VariableUpdate> updates = new ArrayList<>();

        if (response.has("main")) {
            JSONObject obsObj = response.getJSONObject("main");
            if (obsObj.has("temp")) {
                // determine which variables have changed and their values
                Double d = obsObj.getDouble("temp");

                logger.debug("Temperature in Kelvin is {}", d);

                Double tempC = d - 273.15;
                Double tempF = tempC * 1.8 + 32;
                updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.OUTDOOR_TEMP_C), tempC));
                updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.OUTDOOR_TEMP_F), tempF));

                logger.debug("Successfully retrieved OpenWeatherMap data");

                fireVariableUpdateNotifications(updates);
            } else {
                logger.error("Received malformed JSON (missing temp) from OpenWeatherMap");
            }
        } else {
            logger.error("Received malformed JSON (missing main) from OpenWeatherMap");
        }
    }
}

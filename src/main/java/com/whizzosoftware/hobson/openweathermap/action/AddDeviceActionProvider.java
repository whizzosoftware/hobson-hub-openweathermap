/*
 *******************************************************************************
 * Copyright (c) 2017 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.openweathermap.action;

import com.whizzosoftware.hobson.api.action.Action;
import com.whizzosoftware.hobson.api.action.ActionProvider;
import com.whizzosoftware.hobson.api.property.PropertyConstraintType;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.openweathermap.OpenWeatherMapPlugin;

import java.util.Map;

/**
 * Action provider implementation that allows adding a new OpenWeatherMap device.
 *
 * @author Dan Noguerol
 */
public class AddDeviceActionProvider extends ActionProvider {
    static final String PROP_CITY_ID = "cityId";

    private final OpenWeatherMapPlugin plugin;

    public AddDeviceActionProvider(OpenWeatherMapPlugin plugin) {
        super(PropertyContainerClassContext.create(plugin.getContext(), "addDevice"), "Add OpenWeatherMap station", "Adds a new OpenWeatherMap weather station", false, 1000);

        this.plugin = plugin;

        addSupportedProperty(new TypedProperty.Builder(PROP_CITY_ID, "City ID", "The city ID assigned by OpenWeatherMap", TypedProperty.Type.STRING).
            constraint(PropertyConstraintType.required, true).
            build()
        );
    }

    @Override
    public Action createAction(final Map<String, Object> properties) {
        return new AddDeviceAction(plugin.getContext(), new DeviceActionExecutionContext() {
            @Override
            public void addCityId(String cityId) {
                plugin.addCityId(cityId);
            }

            @Override
            public Map<String, Object> getProperties() {
                return properties;
            }
        }, plugin.getEventLoopExecutor());
    }
}

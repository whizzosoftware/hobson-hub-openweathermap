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

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class OpenWeatherMapPluginTest {
    @Test
    public void testInitWithNoConfiguration() {
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id", "version", "description");
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        plugin.onStartup(null);
        assertEquals(PluginStatus.Code.NOT_CONFIGURED, plugin.getStatus().getCode());
    }

    @Test
    public void testInitWithConfiguration() {
        MockDeviceManager dm = new MockDeviceManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        assertEquals(0, dm.getPublishedDeviceCount(plugin.getContext()));

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onStartup(config);
        assertEquals(PluginStatus.Code.RUNNING, plugin.getStatus().getCode());

        assertEquals(1, dm.getPublishedDeviceCount(plugin.getContext()));

        DeviceVariableState v = dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_F));
        assertNotNull(v);
        assertEquals(null, v.getValue());
        v = dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_C));
        assertNotNull(v);
        assertEquals(null, v.getValue());
    }

    @Test
    public void testOnPluginConfigurationUpdate() {
        MockDeviceManager dm = new MockDeviceManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        plugin.onStartup(null);
        assertEquals(PluginStatus.Code.NOT_CONFIGURED, plugin.getStatus().getCode());

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onPluginConfigurationUpdate(config);
        assertEquals(PluginStatus.Code.RUNNING, plugin.getStatus().getCode());
        assertNotNull(dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_F)));
        assertNotNull(dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_C)));
    }
}

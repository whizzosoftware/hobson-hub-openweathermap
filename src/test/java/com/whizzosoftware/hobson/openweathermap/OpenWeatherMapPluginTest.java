/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import java.util.Collection;
import java.util.Iterator;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class OpenWeatherMapPluginTest {
    @Test
    public void testInitWithNoConfiguration() {
        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        plugin.onStartup(null);
        assertEquals(PluginStatus.Code.NOT_CONFIGURED, plugin.getStatus().getCode());
        assertEquals(0, vm.getAllVariables(HubContext.createLocal()).size());
    }

    @Test
    public void testInitWithConfiguration() {
        MockVariableManager vm = new MockVariableManager();
        MockDeviceManager dm = new MockDeviceManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        plugin.setDeviceManager(dm);
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        assertEquals(0, dm.getAllDevices(HubContext.createLocal()).size());

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onStartup(config);
        assertEquals(PluginStatus.Code.RUNNING, plugin.getStatus().getCode());

        Collection<HobsonDevice> devices = dm.getAllDevices(HubContext.createLocal());
        assertEquals(1, devices.size());
        Collection<HobsonVariable> vars = vm.getAllVariables(HubContext.createLocal());
        assertEquals(2, vars.size());
        Iterator<HobsonVariable> it = vars.iterator();
        HobsonVariable v0 = it.next();
        HobsonVariable v1 = it.next();
        assertEquals(null, v0.getValue());
        assertTrue(v0.getName().equals(VariableConstants.OUTDOOR_TEMP_F) || v0.getName().equals(VariableConstants.OUTDOOR_TEMP_C));
        assertEquals(null, v1.getValue());
        assertTrue(v1.getName().equals(VariableConstants.OUTDOOR_TEMP_F) || v1.getName().equals(VariableConstants.OUTDOOR_TEMP_C));
    }

    @Test
    public void testOnPluginConfigurationUpdate() {
        MockVariableManager vm = new MockVariableManager();
        MockDeviceManager dm = new MockDeviceManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        plugin.setDeviceManager(dm);
        assertEquals(PluginStatus.Code.INITIALIZING, plugin.getStatus().getCode());
        plugin.onStartup(null);
        assertEquals(PluginStatus.Code.NOT_CONFIGURED, plugin.getStatus().getCode());

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onPluginConfigurationUpdate(config);
        assertEquals(PluginStatus.Code.RUNNING, plugin.getStatus().getCode());
        Collection<HobsonVariable> vars = vm.getAllVariables(HubContext.createLocal());
        assertEquals(2, vars.size());
    }
}

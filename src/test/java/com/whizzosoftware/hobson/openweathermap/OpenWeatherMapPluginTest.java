/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import java.util.Hashtable;
import java.util.List;

import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class OpenWeatherMapPluginTest {
    @Test
    public void testInitWithNoConfiguration() {
        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        assertEquals(PluginStatus.Status.INITIALIZING, plugin.getStatus().getStatus());
        plugin.onStartup(new Hashtable());
        assertEquals(PluginStatus.Status.NOT_CONFIGURED, plugin.getStatus().getStatus());
        assertEquals(0, vm.globalVariables.size());
    }

    @Test
    public void testInitWithConfiguration() {
        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        assertEquals(PluginStatus.Status.INITIALIZING, plugin.getStatus().getStatus());
        Hashtable config = new Hashtable();
        config.put(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onStartup(config);
        assertEquals(PluginStatus.Status.RUNNING, plugin.getStatus().getStatus());
        assertEquals(1, vm.globalVariables.size());
        List<HobsonVariable> vars = vm.globalVariables.get(plugin.getId());
        assertEquals(2, vars.size());
        assertEquals(null, vars.get(0).getValue());
        assertTrue(vars.get(0).getName().equals(VariableConstants.TEMP_F) || vars.get(0).getName().equals(VariableConstants.TEMP_C));
        assertEquals(null, vars.get(1).getValue());
        assertTrue(vars.get(1).getName().equals(VariableConstants.TEMP_F) || vars.get(1).getName().equals(VariableConstants.TEMP_C));
    }

    @Test
    public void testOnPluginConfigurationUpdate() {
        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        assertEquals(PluginStatus.Status.INITIALIZING, plugin.getStatus().getStatus());
        plugin.onStartup(new Hashtable());
        assertEquals(PluginStatus.Status.NOT_CONFIGURED, plugin.getStatus().getStatus());

        Hashtable config = new Hashtable();
        config.put(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onPluginConfigurationUpdate(config);
        assertEquals(PluginStatus.Status.RUNNING, plugin.getStatus().getStatus());
        assertEquals(1, vm.globalVariables.size());
        List<HobsonVariable> vars = vm.globalVariables.get(plugin.getId());
        assertEquals(2, vars.size());
    }

    @Test
    public void testOnRefresh() {
        Hashtable config = new Hashtable();
        config.put(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");

        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setVariableManager(vm);
        assertEquals(PluginStatus.Status.INITIALIZING, plugin.getStatus().getStatus());
        plugin.onStartup(config);
        assertEquals(PluginStatus.Status.RUNNING, plugin.getStatus().getStatus());

        List<HobsonVariable> vars = vm.globalVariables.get(plugin.getId());
        assertEquals(2, vars.size());
        assertEquals(null, vars.get(0).getValue());
        assertTrue(vars.get(0).getName().equals(VariableConstants.TEMP_F) || vars.get(0).getName().equals(VariableConstants.TEMP_C));
        assertEquals(null, vars.get(1).getValue());
        assertTrue(vars.get(1).getName().equals(VariableConstants.TEMP_F) || vars.get(1).getName().equals(VariableConstants.TEMP_C));

        plugin.onRefresh();

        assertEquals(2, vars.size());
        assertNotNull(vars.get(0).getValue());
        assertTrue(vars.get(0).getName().equals(VariableConstants.TEMP_F) || vars.get(0).getName().equals(VariableConstants.TEMP_C));
        assertNotNull(vars.get(1).getValue());
        assertTrue(vars.get(1).getName().equals(VariableConstants.TEMP_F) || vars.get(1).getName().equals(VariableConstants.TEMP_C));
    }

    @Test
    public void testServerResponseWithSuccess() {
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");

        JSONObject json = new JSONObject(new JSONTokener("{\"main\":{\"temp\":306.15}}"));

        List<VariableUpdate> updates = plugin.parseServerResponse(json);
        assertEquals(2, updates.size());
        assertEquals(updates.get(0).getName(), VariableConstants.TEMP_C);
        assertEquals(33.0, updates.get(0).getValue());
        assertEquals(updates.get(1).getName(), VariableConstants.TEMP_F);
        assertEquals(91.4, updates.get(1).getValue());
    }

    @Test
    public void testServerResponseWithFailure() {
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");

        JSONObject json = new JSONObject(new JSONTokener("{\"error\":\"Something or other\"}"));

        try {
            plugin.parseServerResponse(json);
            fail("Should have thrown an exception");
        } catch (JSONException ignored) {}
    }
}

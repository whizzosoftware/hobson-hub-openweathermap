/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.util.UserUtil;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
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
        assertEquals(0, vm.getGlobalVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB).size());
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
        Collection<HobsonVariable> vars = vm.getGlobalVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB);
        assertEquals(2, vars.size());
        Iterator<HobsonVariable> it = vars.iterator();
        HobsonVariable v0 = it.next();
        HobsonVariable v1 = it.next();
        assertEquals(null, v0.getValue());
        assertTrue(v0.getName().equals(VariableConstants.TEMP_F) || v0.getName().equals(VariableConstants.TEMP_C));
        assertEquals(null, v1.getValue());
        assertTrue(v1.getName().equals(VariableConstants.TEMP_F) || v1.getName().equals(VariableConstants.TEMP_C));
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
        Collection<HobsonVariable> vars = vm.getGlobalVariables(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB);
        assertEquals(2, vars.size());
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

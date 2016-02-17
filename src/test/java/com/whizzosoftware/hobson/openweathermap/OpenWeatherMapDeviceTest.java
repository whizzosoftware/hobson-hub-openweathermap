/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.event.MockEventManager;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpenWeatherMapDeviceTest {
    @Test
    public void testOnUpdateWithGoodJSON() {
        MockDeviceManager dm = new MockDeviceManager();
        MockVariableManager vm = new MockVariableManager();
        MockEventManager em = new MockEventManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setDeviceManager(dm);
        plugin.setVariableManager(vm);
        plugin.setEventManager(em);
        OpenWeatherMapDevice device = new OpenWeatherMapDevice(plugin, "station");

        JSONObject json = new JSONObject(new JSONTokener("{\"main\":{\"temp\":306.15}}"));

        assertEquals(0, vm.getVariableUpdates().size());
        device.onUpdate(json);
        assertEquals(2, vm.getVariableUpdates().size());
        assertEquals(vm.getVariableUpdates().get(0).getName(), VariableConstants.OUTDOOR_TEMP_C);
        assertEquals(33.0, vm.getVariableUpdates().get(0).getValue());
        assertEquals(vm.getVariableUpdates().get(1).getName(), VariableConstants.OUTDOOR_TEMP_F);
        assertEquals(91.4, vm.getVariableUpdates().get(1).getValue());
    }

    @Test
    public void testOnUpdateWithBadJSON() {
        MockDeviceManager dm = new MockDeviceManager();
        MockVariableManager vm = new MockVariableManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id");
        plugin.setDeviceManager(dm);
        plugin.setVariableManager(vm);
        OpenWeatherMapDevice device = new OpenWeatherMapDevice(plugin, "station");

        JSONObject json = new JSONObject(new JSONTokener("{\"error\":\"Something or other\"}"));

        assertEquals(0, vm.getVariableUpdates().size());
        device.onUpdate(json);
        assertEquals(0, vm.getVariableUpdates().size());
    }
}

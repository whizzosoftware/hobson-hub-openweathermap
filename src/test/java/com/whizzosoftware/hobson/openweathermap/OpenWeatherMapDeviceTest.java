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

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.event.MockEventManager;
import com.whizzosoftware.hobson.api.event.device.DeviceVariablesUpdateEvent;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OpenWeatherMapDeviceTest {
    @Test
    public void testOnUpdateWithGoodJSON() {
        MockDeviceManager dm = new MockDeviceManager();
        MockEventManager em = new MockEventManager();

        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        plugin.setEventManager(em);

        Map<String,Object> config = new HashMap<>();
        config.put(OpenWeatherMapPlugin.PROP_API_KEY, "abcd");
        config.put(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onStartup(new PropertyContainer(PropertyContainerClassContext.create(plugin.getContext(), "configuration"), config));

        JSONObject json = new JSONObject(new JSONTokener("{\"main\":{\"temp\":306.15}}"));
        em.clearEvents();
        assertEquals(0, em.getEventCount());

        plugin.onHttpResponse(json);

        assertEquals(2, em.getEventCount());
        DeviceVariablesUpdateEvent e = (DeviceVariablesUpdateEvent)em.getEvent(0);
        assertEquals(2, e.getUpdates().size());
        DeviceVariableState v = dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_C));
        assertEquals(33.0, v.getValue());
        v = dm.getDeviceVariable(DeviceVariableContext.create(plugin.getContext(), OpenWeatherMapPlugin.DEVICE_ID, VariableConstants.OUTDOOR_TEMP_F));
        assertEquals(91.4, v.getValue());
    }

    @Test
    public void testOnUpdateWithBadJSON() {
        MockDeviceManager dm = new MockDeviceManager();
        MockEventManager em = new MockEventManager();
        OpenWeatherMapPlugin plugin = new OpenWeatherMapPlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        plugin.setEventManager(em);

        Map<String,Object> config = new HashMap<>();
        config.put(OpenWeatherMapPlugin.PROP_API_KEY, "abcd");
        config.put(OpenWeatherMapPlugin.PROP_CITY_STATE, "Denver, CO");
        plugin.onStartup(new PropertyContainer(PropertyContainerClassContext.create(plugin.getContext(), "configuration"), config));

        JSONObject json = new JSONObject(new JSONTokener("{\"error\":\"Something or other\"}"));

        em.clearEvents();

        assertEquals(0, em.getEventCount());
        plugin.onHttpResponse(json);
        assertEquals(0, em.getEventCount());
    }
}

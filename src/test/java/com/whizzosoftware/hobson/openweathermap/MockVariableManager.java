/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.openweathermap;

import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableImpl;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;

import java.util.*;

public class MockVariableManager implements VariableManager {
    public Map<String,List<HobsonVariable>> globalVariables = new HashMap<>();

    @Override
    public void publishGlobalVariable(String userId, String hubId, String pluginId, HobsonVariable var) {
        List<HobsonVariable> list = getVariableList(pluginId);
        list.add(var);
    }

    @Override
    public Collection<HobsonVariable> getGlobalVariables(String userId, String hubId) {
        return null;
    }

    @Override
    public HobsonVariable getGlobalVariable(String userId, String hubId, String name) {
        return null;
    }

    @Override
    public void unpublishGlobalVariable(String userId, String hubId, String pluginId, String name) {

    }

    @Override
    public void publishDeviceVariable(String userId, String hubId, String pluginId, String deviceId, HobsonVariable var) {

    }

    @Override
    public void unpublishDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name) {

    }

    @Override
    public void unpublishAllDeviceVariables(String userId, String hubId, String pluginId, String deviceId) {

    }

    @Override
    public void unpublishAllPluginVariables(String userId, String hubId, String pluginId) {

    }

    @Override
    public Collection<HobsonVariable> getDeviceVariables(String userId, String hubId, String pluginId, String deviceId) {
        return null;
    }

    @Override
    public HobsonVariable getDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name) {
        return null;
    }

    @Override
    public boolean hasDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name) {
        return false;
    }

    @Override
    public Long setDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name, Object value) {
        return null;
    }

    @Override
    public void fireVariableUpdateNotification(String userId, String hubId, HobsonPlugin plugin, VariableUpdate update) {
        List<HobsonVariable> vars = getVariableList(update.getPluginId());
        HobsonVariable found = null;
        for (HobsonVariable hv : vars) {
            if (hv.getName().equals(update.getName())) {
                found = hv;
                break;
            }
        }

        if (found != null) {
            vars.remove(found);
        }
        vars.add(new HobsonVariableImpl(update.getName(), update.getValue(), HobsonVariable.Mask.READ_ONLY));
    }

    @Override
    public void fireVariableUpdateNotifications(String userId, String hubId, HobsonPlugin plugin, List<VariableUpdate> updates) {
        for (VariableUpdate vu : updates) {
            fireVariableUpdateNotification(userId, hubId, plugin, vu);
        }
    }

    private List<HobsonVariable> getVariableList(String pluginId) {
        List<HobsonVariable> vars = globalVariables.get(pluginId);
        if (vars == null) {
            vars = new ArrayList<>();
            globalVariables.put(pluginId, vars);
        }
        return vars;
    }
}

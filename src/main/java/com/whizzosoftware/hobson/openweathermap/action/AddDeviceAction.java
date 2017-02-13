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

import com.whizzosoftware.hobson.api.action.ActionLifecycleContext;
import com.whizzosoftware.hobson.api.action.SingleAction;
import com.whizzosoftware.hobson.api.plugin.EventLoopExecutor;
import com.whizzosoftware.hobson.api.plugin.PluginContext;

import java.util.Map;

/**
 * Action implementation that performs the addition of a new OpenWeatherMap device.
 *
 * @author Dan Noguerol
 */
public class AddDeviceAction extends SingleAction {

    public AddDeviceAction(PluginContext pctx, DeviceActionExecutionContext ectx, EventLoopExecutor executor) {
        super(pctx, ectx, executor);
    }

    @Override
    public void onStart(ActionLifecycleContext ctx) {
        Map<String,Object> properties = getContext().getProperties();
        String cityId = (String)properties.get(AddDeviceActionProvider.PROP_CITY_ID);
        ((DeviceActionExecutionContext)getContext()).addCityId(cityId);
        ctx.complete();
    }

    @Override
    public void onStop(ActionLifecycleContext ctx) {
    }

    @Override
    public void onMessage(ActionLifecycleContext ctx, String msgName, Object prop) {
    }
}

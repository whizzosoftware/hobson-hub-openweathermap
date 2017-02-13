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

import com.whizzosoftware.hobson.api.action.ActionExecutionContext;

/**
 * ActionExecutionContext implementation that is used by OpenWeatherMap actions.
 *
 * @author Dan Noguerol
 */
public interface DeviceActionExecutionContext extends ActionExecutionContext {
    void addCityId(String cityId);
}

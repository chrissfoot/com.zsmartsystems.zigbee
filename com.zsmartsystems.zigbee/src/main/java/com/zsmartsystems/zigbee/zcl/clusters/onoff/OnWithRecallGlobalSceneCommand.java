/**
 * Copyright (c) 2016-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.zigbee.zcl.clusters.onoff;

import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.protocol.ZclCommandDirection;

/**
 * On With Recall Global Scene Command value object class.
 * <p>
 * The On With Recall Global Scene command allows the recall of the settings when the device was turned off.
 * <p>
 * Cluster: <b>On/Off</b>. Command is sent <b>TO</b> the server.
 * This command is a <b>specific</b> command used for the On/Off cluster.
 * <p>
 * Attributes and commands for switching devices between ‘On’ and ‘Off’ states.
 * <p>
 * Code is auto-generated. Modifications may be overwritten!
 */
public class OnWithRecallGlobalSceneCommand extends ZclCommand {
    /**
     * Default constructor.
     */
    public OnWithRecallGlobalSceneCommand() {
        genericCommand = false;
        clusterId = 6;
        commandId = 65;
        commandDirection = ZclCommandDirection.CLIENT_TO_SERVER;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(33);
        builder.append("OnWithRecallGlobalSceneCommand [");
        builder.append(super.toString());
        builder.append(']');
        return builder.toString();
    }

}

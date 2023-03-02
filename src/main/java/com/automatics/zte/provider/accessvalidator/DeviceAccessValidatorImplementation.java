/**
* If not stated otherwise in this file or this component's Licenses.txt
* file the following copyright and licenses apply:
*
* Copyright 2022 RDK Management
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**/

package com.automatics.zte.provider.accessvalidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automatics.constants.AutomaticsConstants;
import com.automatics.device.Dut;
import com.automatics.providers.connection.SshConnection;
import com.automatics.providers.impl.DeviceAccessValidatorImpl;
import com.automatics.utils.CommonMethods;
import com.connectionproviders.deviceconnectionprovider.DeviceConnectionProviderImpl;

/**
 * The class extends Device access validator implementation class defined at Core . The implementation of method
 * isDeviceAccessible with arguments will check whether device is accessible, and returns true to the caller method if
 * Device is Accessible.
 */

public class DeviceAccessValidatorImplementation extends DeviceAccessValidatorImpl {

private static final Logger LOGGER = LoggerFactory.getLogger(DeviceAccessValidatorImpl.class);

    /**
     * Verify if device is accessible. Return true if device is accessible.
     */
    public boolean isDeviceAccessible(Dut dut) {
	boolean isDeviceAccessible = false;
	SshConnection connection = null;
	try {  
	    String sshAddress = AutomaticsConstants.EMPTY_STRING;
	    String commandResponse = null;
	    LOGGER.info("SSH Host IP : " + dut.getHostIpAddress());
	    sshAddress = dut.getHostIpAddress();
	    if (CommonMethods.isNotNull(sshAddress) && !"UNAVAILABLE".equalsIgnoreCase(sshAddress)) {
		connection = new SshConnection(dut.getHostIpAddress());
		commandResponse = DeviceConnectionProviderImpl.sendReceive(connection, "echo test_connection;", 30000);
		if (CommonMethods.isNull(commandResponse) || commandResponse.indexOf("test_connection") == -1) {
		    LOGGER.error("\n*************************************\n UNABLE TO ACCESS THE DEVICE ("
			    + dut.getHostMacAddress() + ") USING IP/MAC Address " + sshAddress
			    + "\n*************************************");
		}
		else
		    isDeviceAccessible = true;
	    } else {
		LOGGER.error("IP Address is null , skipping connection check");
	    }

	} catch (Exception ex) {
	    LOGGER.error("Exiting occured while running command : {}", ex.getMessage());
	} finally {
	    if (null != connection) {
		LOGGER.info("Closing SSH connection from DeviceIP: {}", dut.getHostIpAddress());
		connection.disconnect();
	    }
	}
	
	LOGGER.info("Exiting method isDeviceAccessible. Status - " + isDeviceAccessible);
	return isDeviceAccessible;

    }

}


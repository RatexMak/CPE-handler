/**
 * Copyright 2022 Vodafone Group plc
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.connectionproviders.deviceconnectionprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import com.automatics.device.Device;
import com.automatics.device.Dut;
import com.automatics.providers.connection.Connection;
import com.automatics.providers.connection.DeviceConnectionProvider;
import com.automatics.providers.connection.DeviceConsoleType;
import com.automatics.providers.connection.ExecuteCommandType;
import com.automatics.providers.connection.SshConnection;
import com.automatics.resource.IServer;
import com.jcraft.jsch.JSchException;

/**
 * The class provides Device connection provider implementation as defined by the interface class
 * DeviceConnectionProvider. The implementation of execute methods with different overloaded arguments will establish an
 * SSH connection to the target HG, launch the commands and returns the response to the caller method. The target HW
 * access info. need to be updated in server-config.xml and automatics-core resources folder
 */

public class DeviceConnectionProviderImpl implements DeviceConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConnectionProviderImpl.class);
    private static final long defaultTimeout = 1000;

    private static String sendReceive(SshConnection conn, String command, long timeOutMilliSecs)
	    throws IOException, InterruptedException, JSchException {
	LOGGER.debug("sendReceive() method invoked");
	String response = "";

	conn.send(command, (int) (timeOutMilliSecs));
	response = conn.getSettopResponse(timeOutMilliSecs);

	return response;
    }

    /**
     * Copy file to device
     * 
     * @param device
     * @param fileToCopy
     * @param remoteLocation
     * @return bool status
     */
    public boolean copyFile(Device device, String fileToCopy, String remoteLocation) {
	LOGGER.error("copyFile method is not implemented");
	return false;
    }

    /**
     * To get connection to device
     * 
     * @param device
     * @return SSH connection
     */
    public Connection getConnection(Device device) {
	LOGGER.debug("getConnection method invoked ");
	Connection conn = new SshConnection(device.getHostIpAddress());
	return conn;
    }

    /**
     * Execute commands in device
     * 
     * @param device
     * @param command
     * @return response string
     */
    public String execute(Device device, String command) {
	String response = "";
	LOGGER.debug("execute method invoked with device,command: " + command);
	SshConnection conn = new SshConnection(device.getHostIpAddress());
	try {
	    response = sendReceive(conn, command, defaultTimeout);
	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands in device
     * 
     * @param device
     * @param commandList
     * @return response string
     */
    public String execute(Device device, List<String> commandList) {
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	for (String idx : commandList) {
	    LOGGER.debug("execute method invoked with device,commandList: " + idx);
	    try {
		response = sendReceive(conn, idx, defaultTimeout);
	    } catch (Exception ex) {
		LOGGER.error("Exception occured while sending request DUT " + ex);
		if (null != conn) {
		    conn.disconnect();
		}

		// got exception for some reason try to reconnect and try other commands
		conn = new SshConnection(device.getHostIpAddress());
	    }
	}
	if (null != conn) {
	    conn.disconnect();
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands in device
     * 
     * @param device
     * @param executeCommandType
     * @param commandList
     * @return response string
     */
    public String execute(Device device, ExecuteCommandType executeCommandType, List<String> commandList) {
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	for (String idx : commandList) {
	    LOGGER.debug("execute method invoked with device,executeCommandType, commandList: " + idx);
	    try {
		switch (executeCommandType) {
		case REV_SSH_DEVICE_VERIFY: {
		    break;
		}
		case TRACE_INIT_COMMAND_GATEWAY: {
		    break;
		}
		case ADDLN_TRACE_INIT_COMMAND_GATEWAY: {
		    break;
		}
		case SNMP_CODE_DOWNLOAD: {
		    break;
		}
		case SNMP_COMMAND: {
		    response = sendReceive(conn, idx, defaultTimeout);
		    break;
		}
		case XCONF_CONFIG_UPDATE: {
		    break;
		}
		default: {
		    response = sendReceive(conn, idx, defaultTimeout);
		}
		}
	    } catch (Exception ex) {
		LOGGER.error("Exception occured while sending request DUT " + ex);
		if (null != conn) {
		    conn.disconnect();
		}
		// got exception for some reason try to reconnect and try other commands
		conn = new SshConnection(device.getHostIpAddress());
	    }
	}
	if (null != conn) {
	    conn.disconnect();
	}

	LOGGER.debug("Received response: " + response);

	return response.toString();
    }

    /**
     * Execute commands in device
     * 
     * @param dut
     * @param command
     * @param expectStr
     * @param options
     * @return response string
     */
    public String execute(Dut dut, String command, String expectStr, String[] options) {
	String response = "";
	LOGGER.debug("execute method invoked with device,command, expectStr and options: " + command);
	SshConnection conn = new SshConnection(dut.getHostIpAddress());

	try {
	    response = conn.send(command, expectStr, options);
	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands in device
     * 
     * @param dut
     * @param atomServerIp
     * @param command
     * @return response string
     */
    public String executeInsideAtomConsoleUsingExpect(Dut dut, String atomServerIp, String command) {
	LOGGER.error("executeInsideAtomConsoleUsingExpect() method not implemented!");
	return null;
    }

    /**
     * Execute commands in given device console
     * 
     * @param device
     * @param command
     * @param consoleType
     * @param timeOutMilliSecs
     * @return response string
     */
    public String execute(Device device, String command, DeviceConsoleType consoleType, long timeOutMilliSecs) {
	String response = "";
	LOGGER.debug("execute method invoked with device,command, consoleType and timeout: " + command);
	SshConnection conn = new SshConnection(device.getHostIpAddress());
	try {
	    switch (consoleType) {
	    case ARM: {
		response = sendReceive(conn, command, timeOutMilliSecs);
		break;
	    }
	    case ATOM: {
		break;
	    }
	    default: {
		response = sendReceive(conn, command, timeOutMilliSecs);
	    }
	    }
	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}
	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands in given device console
     * 
     * @param device
     * @param commandList
     * @param consoleType
     * @return response string
     */
    public String execute(Device device, List<String> commandList, DeviceConsoleType consoleType) {
	LOGGER.debug("execute method invoked with device,commandList, consoleType ");
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	for (String idx : commandList) {
	    try {
		switch (consoleType) {
		case ARM: {
		    response = sendReceive(conn, idx, defaultTimeout);
		    break;
		}
		case ATOM: {
		    break;
		}
		default: {
		    response = sendReceive(conn, idx, defaultTimeout);
		}
		}
	    } catch (Exception ex) {
		LOGGER.error("Exception occured while sending request DUT " + ex);
		if (null != conn) {
		    conn.disconnect();
		}
		// got exception for some reason try to reconnect and try
		conn = new SshConnection(device.getHostIpAddress());
	    }
	}
	if (null != conn) {
	    conn.disconnect();
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands in given device console
     * 
     * @param device
     * @param commandList
     * @param consoleType
     * @param timeOutMilliSecs
     * @return response string
     */
    public String execute(Device device, List<String> commandList, DeviceConsoleType consoleType,
	    long timeOutMilliSecs) {
	LOGGER.debug("execute method invoked with device,commandList, consoleType, timeOut");
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	for (String idx : commandList) {
	    try {
		switch (consoleType) {
		case ARM: {
		    response = sendReceive(conn, idx, timeOutMilliSecs);
		    break;
		}
		case ATOM: {
		    break;
		}
		default: {
		    response = sendReceive(conn, idx, timeOutMilliSecs);
		}
		}
	    } catch (Exception ex) {
		LOGGER.error("Exception occured while sending request DUT " + ex);
		if (null != conn) {
		    conn.disconnect();
		}
		// got exception for some reason try to reconnect and try
		conn = new SshConnection(device.getHostIpAddress());
	    }
	}
	if (null != conn) {
	    conn.disconnect();
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands using given device connection
     * 
     * @param device
     * @param deviceConnnection
     * @param command
     * @return response string
     */
    public String execute(Device device, Connection deviceConnnection, String command) {
	LOGGER.debug("execute method invoked with device, deviceConnnection, command");
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	try {
	    response = sendReceive(conn, command, defaultTimeout);
	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands using given device connection
     * 
     * @param device
     * @param deviceConnnection
     * @param executeCommandType
     * @param command
     * @return response string
     */
    public String execute(Device device, Connection deviceConnnection, ExecuteCommandType executeCommandType,
	    String command) {
	LOGGER.debug("execute method invoked with device, deviceConnnection, CommandType, command");
	String response = "";
	SshConnection conn = new SshConnection(device.getHostIpAddress());

	try {
	    switch (executeCommandType) {
	    case REV_SSH_DEVICE_VERIFY: {
		break;
	    }
	    case TRACE_INIT_COMMAND_GATEWAY: {
		break;
	    }
	    case ADDLN_TRACE_INIT_COMMAND_GATEWAY: {
		break;
	    }
	    case SNMP_CODE_DOWNLOAD: {
		break;
	    }
	    case SNMP_COMMAND: {
		response = sendReceive(conn, command, defaultTimeout);
		break;
	    }
	    case XCONF_CONFIG_UPDATE: {
		break;
	    }
	    default: {
		response = sendReceive(conn, command, defaultTimeout);
	    }
	    }

	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}

	LOGGER.debug("Received response: " + response);

	return response;
    }

    /**
     * Execute commands on given host
     * 
     * @param hostDetails
     * @param commands
     * @param timeOutMilliSecs
     * @return response string
     */
    public String execute(IServer hostDetails, List<String> commands, long timeOutMilliSecs) {
	LOGGER.error("execute method hostDetails, commandslist, timeout is not implemented");
	return null;
    }

    /**
     * Execute commands on given host
     * 
     * @param hostIp
     * @param command
     * @param timeOutMilliSecs
     * @param connectionType
     * @return response string
     */
    public String execute(String hostIp, String command, long timeOutMilliSecs, String connectionType) {
	LOGGER.debug("execute method invoked with hostIp " + hostIp + " " + command + " " + timeOutMilliSecs);
	String response = "";
	SshConnection conn = new SshConnection(hostIp);

	try {
	    response = sendReceive(conn, command, timeOutMilliSecs);
	} catch (Exception ex) {
	    LOGGER.error("Exception occured while sending request DUT " + ex);
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}
	LOGGER.debug("Received response: " + response);

	return response;
    }
}

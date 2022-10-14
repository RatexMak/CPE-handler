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
import com.automatics.error.GeneralError;
import com.automatics.exceptions.FailedTransitionException;
import com.automatics.providers.connection.Connection;
import com.automatics.providers.connection.DeviceConnectionProvider;
import com.automatics.providers.connection.DeviceConsoleType;
import com.automatics.providers.connection.ExecuteCommandType;
import com.automatics.providers.connection.SshConnection;
import com.automatics.resource.IServer;
import com.automatics.rpi.constants.Constants;
import com.automatics.rpi.utils.CommonMethods;
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
    private static final int SSH_CONNECTION_MAX_ATTEMPT = 4;

    private static String sendReceive(SshConnection conn, String command, long timeOutMilliSecs) {
	LOGGER.info("Executing command: " + command);
	String response = "";
	try {
	    conn.send(command, (int) (timeOutMilliSecs));
	    response = conn.getSettopResponse(timeOutMilliSecs);
	    response = CommonMethods.removeSecurityBannerFromResponse(response);
	    LOGGER.info("\n<===========================  RESPONSE =======================> \n" + response
		    + "\n<=============================================================>");
	    return response;
	} catch (Exception ex) {
	    LOGGER.error("Exception occurred while executing the command ", ex);
	    throw new FailedTransitionException(GeneralError.SSH_CONNECTION_FAILURE, ex);
	}

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
	LOGGER.info("getConnection method invoked ");
	Connection conn = null;
	conn = createSshConnection(device.getHostIpAddress());
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
	return executeCommand(device.getHostIpAddress(), command, defaultTimeout);
    }

    /**
     * Execute commands in device
     * 
     * @param device
     * @param commandList
     * @return response string
     */
    public String execute(Device device, List<String> commandList) {
	StringBuilder response = new StringBuilder();
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DeviceIP:" + device.getHostIpAddress());
	try {
	    conn = createSshConnection(device.getHostIpAddress());
	    for (String idx : commandList) {
		response.append(sendReceive(conn, idx, defaultTimeout)).append(Constants.NEW_LINE);
	    }

	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device.getHostIpAddress());
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response.toString());

	return response.toString();
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
	StringBuilder response = new StringBuilder();
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DeviceIP:" + device.getHostIpAddress());
	try {
	    conn = createSshConnection(device.getHostIpAddress());
	    for (String idx : commandList) {

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
		    response.append(sendReceive(conn, idx, defaultTimeout)).append(Constants.NEW_LINE);
		    break;
		}
		case XCONF_CONFIG_UPDATE: {
		    break;
		}
		default: {
		    response.append(sendReceive(conn, idx, defaultTimeout)).append(Constants.NEW_LINE);
		}
		}

	    }

	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device.getHostIpAddress());
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response.toString());

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
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DutIP:" + dut.getHostIpAddress());
	try {
	    conn = createSshConnection(dut.getHostIpAddress());
	    response = conn.send(command, expectStr, options);
	} catch (Exception ex) {
	    LOGGER.info("Exception occurred while executing command " + ex.getMessage(), ex);
	    throw new FailedTransitionException(GeneralError.SSH_CONNECTION_FAILURE, ex);
	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DutIP:" + dut.getHostIpAddress());
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response);

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
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DeviceIP:" + device.getHostIpAddress());
	try {
	    conn = createSshConnection(device.getHostIpAddress());
	    switch (consoleType) {
	    case ARM: {
		response = sendReceive(conn, command, timeOutMilliSecs);
		break;
	    }
	    case ATOM: {
		LOGGER.info(
			"Implementation is not available for DeviceConsoleType: " + DeviceConsoleType.ATOM.toString());
		break;
	    }
	    default: {
		response = sendReceive(conn, command, timeOutMilliSecs);
	    }
	    }
	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device.getHostIpAddress());
		conn.disconnect();
	    }
	}
	LOGGER.info("Received response: " + response);

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

	StringBuilder response = new StringBuilder();
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DutIP:" + device.getHostIpAddress());
	try {
	    conn = createSshConnection(device.getHostIpAddress());
	    for (String idx : commandList) {

		switch (consoleType) {
		case ARM: {
		    response.append(sendReceive(conn, idx, defaultTimeout)).append(Constants.NEW_LINE);
		    break;
		}
		case ATOM: {
		    break;
		}
		default: {
		    response.append(sendReceive(conn, idx, defaultTimeout)).append(Constants.NEW_LINE);
		}
		}

	    }
	} finally {
	    if (null != conn) {
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response.toString());

	return response.toString();
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

	StringBuilder response = new StringBuilder();
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DeviceIP:" + device.getHostIpAddress());
	try {
	    conn = createSshConnection(device.getHostIpAddress());
	    for (String idx : commandList) {

		switch (consoleType) {
		case ARM: {
		    response.append(sendReceive(conn, idx, timeOutMilliSecs)).append(Constants.NEW_LINE);
		    break;
		}
		case ATOM: {
		    break;
		}
		default: {
		    response.append(sendReceive(conn, idx, timeOutMilliSecs)).append(Constants.NEW_LINE);
		}
		}
	    }
	} finally {

	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device.getHostIpAddress());
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response.toString());

	return response.toString();
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
	return executeCommand(device.getHostIpAddress(), command, defaultTimeout);
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
	String response = "";
	SshConnection conn = null;
	LOGGER.info("About to create SSH connection to DeviceIP:" + device.getHostIpAddress());

	try {
	    conn = createSshConnection(device.getHostIpAddress());
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

	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device.getHostIpAddress());
		conn.disconnect();
	    }
	}

	LOGGER.info("Received response: " + response);

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
     * @throws JSchException
     * @throws InterruptedException
     * @throws IOException
     */

    public String execute(String hostIp, String command, long timeOutMilliSecs, String connectionType) {
	return executeCommand(hostIp, command, timeOutMilliSecs);
    }

    private String executeCommand(String device, String command, long timeOutMilliSecs) {
	SshConnection conn = null;
	String response = "";
	LOGGER.info("About to create SSH connection to DeviceIP:" + device);
	try {
	    conn = createSshConnection(device);
	    response = sendReceive(conn, command, timeOutMilliSecs);
	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device);
		conn.disconnect();
	    }
	}
	LOGGER.info("Received response: " + response);
	return response;
    }

    private static SshConnection createSshConnection(String hostIp) {
	SshConnection connection = null;
	String sshFailureMesaage = "";
	String trying = "Trying once more..";
	LOGGER.info("SSH Host IP : " + hostIp);

	for (int retryCount = 1; retryCount <= SSH_CONNECTION_MAX_ATTEMPT; retryCount++) {
	    try {
		LOGGER.info("SSh connection attempet : " + retryCount);
		connection = new SshConnection(hostIp);
	    } catch (Exception e) {

		// Trying once more

		if (retryCount == 4) {
		    trying = "";
		}

		LOGGER.info("SSh connection attempet : " + retryCount + " failed due to " + e.getMessage() + " for "
			+ hostIp + ". " + trying);
		sshFailureMesaage = e.getMessage();
		connection = null;
		if (retryCount != SSH_CONNECTION_MAX_ATTEMPT) {
		    CommonMethods.sleep(Constants.TEN_SECONDS);
		}

	    }

	    if (null != connection) {
		break;
	    }
	}

	if (null == connection) {
	    throw new FailedTransitionException(GeneralError.SSH_CONNECTION_FAILURE, sshFailureMesaage);
	}

	return connection;
    }

}

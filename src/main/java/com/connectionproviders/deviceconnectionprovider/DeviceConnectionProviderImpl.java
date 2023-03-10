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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automatics.constants.AutomaticsConstants;
import com.automatics.core.SupportedModelHandler;
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
import com.automatics.zte.constants.Constants;
import com.automatics.zte.utils.CommonMethods;
import com.automatics.utils.AutomaticsPropertyUtility;
import com.jcraft.jsch.JSchException;

/**
 * The class provides Device connection provider implementation as defined by the interface class
 * DeviceConnectionProvider. The implementation of execute methods with different overloaded arguments will establish an
 * SSH connection to the target HG, launch the commands and returns the response to the caller method. The target HW
 * access info. need to be updated in server-config.xml and automatics-core resources folder
 */

public class DeviceConnectionProviderImpl implements DeviceConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConnectionProviderImpl.class);
    private static long defaultTimeout = 1000;
    private static final int SSH_CONNECTION_MAX_ATTEMPT = 4;

    private int sshConnectMaxAttempt = SSH_CONNECTION_MAX_ATTEMPT;

    public DeviceConnectionProviderImpl() {

	String timeOutInString = AutomaticsPropertyUtility.getProperty(Constants.PROPS_RDK_RESP_WAIT_TIME_MILLISEC);
	if (CommonMethods.isNotNull(timeOutInString)) {
	    try {
		defaultTimeout = Long.parseLong(timeOutInString);

	    } catch (NumberFormatException e) {
		LOGGER.error("Error parsing value for field: {}, {}", Constants.PROPS_RDK_RESP_WAIT_TIME_MILLISEC,
			e.getMessage());
	    }
	}
	String maxAttempt = AutomaticsPropertyUtility.getProperty("SSH_CONNECTION_MAX_ATTEMPT",
		Integer.toString(SSH_CONNECTION_MAX_ATTEMPT));
	try {
	    sshConnectMaxAttempt = Integer.parseInt(maxAttempt);
	} catch (Exception e) {
	    LOGGER.error("Error parsing ssh connection max attempt property: SSH_CONNECTION_MAX_ATTEMPT: {}",
		    e.getMessage());
	}

    }

    public static String sendReceive(SshConnection conn, String command, long timeOutMilliSecs) {
	LOGGER.info("Executing command: " + command);
	String response = AutomaticsConstants.EMPTY_STRING;

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

	String response = AutomaticsConstants.EMPTY_STRING;
	if (SupportedModelHandler.isNonRDKDevice(device)) {
		LOGGER.info("============================ Debug ============================");
		LOGGER.info("device IP Address: "+device.getHostIpAddress());
		LOGGER.info("============================ Debug ============================");
	    response = executeCommandOnNonRdkDevice(device, command, defaultTimeout);
	} else {
	    response = executeCommand(device.getHostIpAddress(), command, defaultTimeout);
	}

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
	StringBuilder response = new StringBuilder();
	SshConnection conn = null;

	if (SupportedModelHandler.isNonRDKDevice(device)) {
	    for (String command : commandList) {
		LOGGER.info("Executing command on non-RDK device: {} {}", device.getHostMacAddress(), command);
		response.append(executeCommandOnNonRdkDevice(device, command, defaultTimeout))
			.append(AutomaticsConstants.NEW_LINE);
	    }

	} else {
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
	List<String> commandList = new ArrayList<String>();
	commandList.add(command);
	return execute(device, commandList, consoleType, timeOutMilliSecs);
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

	if (SupportedModelHandler.isNonRDKDevice(device)) {
	    for (String command : commandList) {
		LOGGER.info("Executing command on non-RDK device: {} {}", device.getHostMacAddress(), command);
		response.append(executeCommandOnNonRdkDevice(device, command, timeOutMilliSecs))
			.append(AutomaticsConstants.NEW_LINE);
	    }

	} else {
	    LOGGER.info("About to create SSH connection to DutIP:" + device.getHostIpAddress());
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
		    conn.disconnect();
		}
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
     * @return response string
     */
    public String execute(Device device, List<String> commandList, DeviceConsoleType consoleType) {
	return execute(device, commandList, consoleType, defaultTimeout);

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
	String response = AutomaticsConstants.EMPTY_STRING;
	SshConnection conn = null;

	try {
	    conn = (SshConnection) conn;

	    response = sendReceive(conn, command, defaultTimeout);
	} finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:" + device);
		conn.disconnect();
	    }
	}
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
	String response = AutomaticsConstants.EMPTY_STRING;
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
     * Method establish a SSH connection to the host using the user name.
     *
     * @param userName
     *            The SSH user name.
     * @param password
     *            Password for establishing the SSH connection.
     * @param hostIp
     *            The host name to which connection to be established.
     *
     * @return The SSH connection.
     */
    private static SshConnection getSshConnection(String userName, String password, String hostIp) {
	SshConnection connection = null;
	String sshFailureMesaage = "";
	String trying = "trying for";
	LOGGER.debug("SSH Host IP : " + hostIp);

	for (int retryCount = 1; retryCount <= SSH_CONNECTION_MAX_ATTEMPT; retryCount++) {
	    try {
		LOGGER.debug("SSh connection attempet : " + retryCount);
		connection = new SshConnection(userName, password, hostIp);
	    } catch (Exception e) {

		// Trying once more

		if (SSH_CONNECTION_MAX_ATTEMPT == retryCount) {
		    trying = "";
		}

		LOGGER.info("SSh connection attempet : " + retryCount + " failed due to " + e.getMessage() + " for "
			+ hostIp + ". " + trying);
		sshFailureMesaage = e.getMessage();
		connection = null;
		if (SSH_CONNECTION_MAX_ATTEMPT != retryCount) {
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

    /**
     * Execute commands on given host
     * 
     * @param hostDetails
     * @param commands
     * @param timeOutMilliSecs
     * @return response string
     */
    public String execute(IServer hostDetails, List<String> commands, long timeOutMilliSecs) {
	StringBuilder response = new StringBuilder();
	SshConnection sshConnection = null;

	try {

	    if ("localhost".equals(hostDetails.getHostIp())) {
		for (String command : commands) {
		    LOGGER.info("About to execute the command : " + command);
		    response.append(execute(command)).append(Constants.NEW_LINE);
		}
	    } else {
		LOGGER.info("Creating ssh connection to server: {}", hostDetails.getHostIp());
		sshConnection = getSshConnection(hostDetails.getUserId(), hostDetails.getPassword(),
			hostDetails.getHostIp());
		LOGGER.info("Success fully established the SSH connection with server.");

		for (String command : commands) {
		    LOGGER.info("About to execute the command : " + command);
		    response.append(sendReceive(sshConnection, command, 50000)).append(Constants.NEW_LINE);
		}
	    }

	} catch (Exception e) {
	    LOGGER.error("Exception occured while executing command: " + hostDetails + " " + e.getMessage());
	} finally {

	    if (null != sshConnection) {
		sshConnection.disconnect();
	    }
	}

	LOGGER.info("Successfully executed commands  = \n " + response.toString());

	return response.toString();

    }

    /**
     * Execute command on localhost
     *
     * @param command
     * @return response string 
     */
    public String execute(String command) {
	String response = null;
	ProcessBuilder processBuilder = new ProcessBuilder();
	processBuilder.command("bash", "-c", command);

	try {
	    LOGGER.info("Command execution Started ....");
	    Process process = processBuilder.start();

	    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	    StringBuffer sb = new StringBuffer();
	    String line;
	    while ((line = reader.readLine()) != null) {
		sb.append(line + "\n");
	    }
	    response = sb.toString();
	    int exitCode = process.waitFor();
	    if (exitCode == 0) {
		LOGGER.info("Command execution Completed");
	    }
	} catch (IOException | InterruptedException e) {
	    LOGGER.error("Exception occured while executing commands on localhost :  " + e.getMessage());
	}
	return response;
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
	String response = AutomaticsConstants.EMPTY_STRING;

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

    /**
     * Executes commands on non-rdk device
     * 
     * @param device
     * @param command
     * @param timeOutMilliSecs
     * @return Command execution response
     */
    private String executeCommandOnNonRdkDevice(Device device, String command, long timeOutMilliSecs) {
	SshConnection conn = null;
	String response = AutomaticsConstants.EMPTY_STRING;

	String hostIpAddress = device.getNatAddress();
	String username = device.getUsername();
	String password = device.getPassword();
	String sshPort = device.getNatPort();
	LOGGER.info("++++++++++++++++++++++++++++++++ DEBUG execute ++++++++++++++++++++++++++++++++");
	LOGGER.info("device.getHostIpAddress: " + device.getHostIpAddress());
	LOGGER.info("device.getNatAddress: " + device.getNatAddress());
	LOGGER.info("++++++++++++++++++++++++++++++++ DEBUG execute ++++++++++++++++++++++++++++++++");
	LOGGER.info("About to create SSH connection to DeviceIP:{}", hostIpAddress);
	try {
	    conn = createSshConnectionWithoutRetry(hostIpAddress, sshPort, username, password);

	    command = replaceAnyPipesInCommand(command);

	    LOGGER.info(
		    "\n(SSH EXECUTION) : Executing command {}  on client : Mac Address [{}] , User Name [{}], IP Address [{}] and Port Number [{}]",
		    command, device.getHostMacAddress(), username, hostIpAddress, sshPort);

	    command += AutomaticsConstants.NEW_LINE;
	    response = sendReceiveOnNonRdk(conn, command, timeOutMilliSecs);
	} catch (Exception e) {
	    LOGGER.error("[SSH FAILED] : " + hostIpAddress + ":" + sshPort + e.getMessage(), e);
	    LOGGER.error("[SSH FAILED] : " + hostIpAddress + ":" + sshPort
		    + " Looks like this device is not properly configured");
	}

	finally {
	    if (null != conn) {
		LOGGER.info("Closing SSH connection from DeviceIP:{}", hostIpAddress);
		conn.disconnect();
	    }
	}
	LOGGER.info("Received response: {}", response);
	return response;
    }

    /**
     * Creates ssh connection without retry
     * 
     * @param hostIp
     * @param sshPort
     * @param username
     * @param password
     * @return SshConnection instance
     */
    private SshConnection createSshConnectionWithoutRetry(String hostIp, String sshPort, String username,
	    String password) {

	return new SshConnection(hostIp, Integer.parseInt(sshPort), username, password, null);

    }

    /**
     * Creates ssh connection. Retry if connection failed to create
     * 
     * @param hostIp
     * @return SshConnection instance
     */
    private SshConnection createSshConnection(String hostIp) {
	SshConnection connection = null;
	connection = createSshConnection(hostIp, sshConnectMaxAttempt);

	return connection;
    }

    /**
     * Creates ssh connection. Retry if connection failed to create
     * 
     * @param hostIp
     * @param retryCount
     * @return SshConnection instance
     */
    private SshConnection createSshConnection(String hostIp, int retryCount) {
	SshConnection connection = null;
	String sshFailureMesaage = "";
	String trying = "Trying once more..";
	LOGGER.info("SSH Host IP : " + hostIp);

	for (int retryIndex = 1; retryIndex <= retryCount; retryIndex++) {
	    try {
		LOGGER.info("SSh connection attempet : " + retryIndex);
		connection = new SshConnection(hostIp);
	    } catch (Exception e) {

		// Trying once more

		if (retryIndex == retryCount) {
		    trying = "";
		}

		LOGGER.info("SSh connection attempet : " + retryIndex + " failed due to " + e.getMessage() + " for "
			+ hostIp + ". " + trying);
		sshFailureMesaage = e.getMessage();
		connection = null;
		if (retryIndex != retryCount) {
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

    /**
     * 
     * WHen pip symbol is present, at times the ssh connection is not able to read the about. To resolve this issue, pip
     * is being replaced with another operator which has same capability.But their internal operation is different that
     * of pip
     * 
     * @param command
     * @return
     */
    private static String replaceAnyPipesInCommand(String command) {
	boolean isAndPresent = false;

	StringBuffer commandToExecute = new StringBuffer();
	if (CommonMethods.isNotNull(command) && command.contains("|")) {
	    if (command.lastIndexOf("&") == command.length() - 1) {
		isAndPresent = true;
		command = command.substring(0, command.length() - 2);
	    }
	    String[] commands = command.split("\\|");
	    int numberOfCommands = 0;
	    for (int i = commands.length - 1; i >= 0; i--) {
		numberOfCommands++;
		commandToExecute.append(commands[i]);
		if (i != commands.length - 1) {
		    commandToExecute.append(")");
		}
		if (i != 0) {
		    commandToExecute.append("< <");
		    commandToExecute.append("(");
		    if (commands.length - numberOfCommands > 1) {
			commandToExecute.append("(");
		    }
		}
	    }
	    if (commands.length - 2 > 0) {
		for (int j = 0; j < commands.length - 2; j++) {
		    commandToExecute.append(")");
		}
	    }
	    if (isAndPresent) {
		commandToExecute.append(" &");
	    }
	} else {
	    commandToExecute.append(command);
	}
	return commandToExecute.toString();
    }

    /**
     * Send command to non-rdk device
     * 
     * @param conn
     * @param command
     * @param timeOutMilliSecs
     * @return
     */
    private String sendReceiveOnNonRdk(SshConnection conn, String command, long timeOutMilliSecs) {
	LOGGER.info("Executing command: {}", command);
	String response = AutomaticsConstants.EMPTY_STRING;
	String timeOutInString = AutomaticsPropertyUtility.getProperty(Constants.PROPS_NON_RDK_RESP_WAIT_TIME_MILLISEC);
	if (CommonMethods.isNotNull(timeOutInString)) {
	    try {
		timeOutMilliSecs = Integer.parseInt(timeOutInString);
		LOGGER.info("Using configured response timeout: {}", timeOutMilliSecs);

	    } catch (NumberFormatException e) {
		LOGGER.error("Error parsing value for field: {}, {}", Constants.PROPS_NON_RDK_RESP_WAIT_TIME_MILLISEC,
			e.getMessage());
	    }
	}
	try {
	    conn.sendCommand(command, (int) (timeOutMilliSecs));
	    response = conn.getSettopResponse(timeOutMilliSecs);

	    LOGGER.info("\n<===========================  RESPONSE =======================> \n" + response
		    + "\n<=============================================================>");
	    return response;
	} catch (Exception ex) {
	    LOGGER.error("Exception occurred while executing the command ", ex);
	    throw new FailedTransitionException(GeneralError.SSH_CONNECTION_FAILURE, ex);
	}

    }

}

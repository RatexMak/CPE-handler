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

package com.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automatics.device.Dut;
import com.automatics.snmp.SnmpParams;
import com.automatics.snmp.SnmpProtocol;
import com.automatics.snmp.SnmpSecurityDetails;
import com.automatics.snmp.Snmpv3SecurityLevel;
import com.automatics.utils.AutomaticsPropertyUtility;
import com.automatics.providers.snmp.SnmpDataProvider;

/*
* The class SnmpDataProviderImpl provides implementation
* as defined by the interface class SnmpDataProvider.
* The implementation updates the SNMP authorization details
* specific to the provider based on the deployment
* which is configured in the Automatics properties
* For e.g. (to run on RPi below values are tried)
* snmp.port=161(161/162)
* snmp.protocol=udp(tcp/udp)
* snmp.community=private
* snmp.securityLevel= authPriv
* snmp.securityName= linuser
* snmp.securityOptions= -a SHA -A linuserpass -x DES -X linprivpass
* The above parameters snmp.port, snmp.protocol, snmp.community, snmp.securityLevel,
* snmp.securityName and snmp.securityOptions can be custom configured by partner in 
* Automatics properties according to dut Configuration. 
* SNMP securityLevel,securityName, securityOptions are parameters used 
* for SNMP V3 and where community String is used for SNMP V2.
* */

public class SnmpDataProviderImpl implements SnmpDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpDataProviderImpl.class);
    private static final String DEFAULT_SNMP_PORT = "161";
    private static final String DEFAULT_SNMP_PROTOCOL = "udp";
    private static final String DEFAULT_SNMP_COMMUNITY = "private";

    /**
     * Get authorization data required for snmp communication.
     * 
     * @param dut
     *            Device on snmp commands are executed
     * @param snmpParams
     *            Snmp params
     * @return Authentication data for snmp communication
     */

    public SnmpSecurityDetails getSnmpAuthorization(Dut dut, SnmpParams snmpParams) {

	/* Creation of SnmpSecurityDetails Object */
	SnmpSecurityDetails autObj = new SnmpSecurityDetails();
	LOGGER.info(" SNMP PROVIDER " + snmpParams.getSnmpVersion());

	/* Setting SNMP Port if default value is null */
	String snmpPort = AutomaticsPropertyUtility.getProperty("snmp.port");
	if (snmpPort == null) {
	    snmpPort = DEFAULT_SNMP_PORT;
	}
	autObj.setSnmpPort(snmpPort);
	LOGGER.info(" SNMP PROVIDER port " + snmpPort);

	/* Setting protocol if default value is null */
	String snmpProtocol = AutomaticsPropertyUtility.getProperty("snmp.protocol");
	if (snmpProtocol == null) {
	    snmpProtocol = DEFAULT_SNMP_PROTOCOL;
	}
	autObj.setSnmpProtocol(snmpProtocol);
	LOGGER.info(" SNMP PROVIDER protocol " + snmpProtocol);

	/*
	 * Extracting SNMP Params i.e, SecurityName and SecurityOptions from AutomaticsPropertyUtility for running SNMP
	 * V3 communication
	 */
	if (snmpParams.getSnmpVersion() == SnmpProtocol.SNMP_V3) {
	    LOGGER.info(" SNMP PROVIDER V3");
	    
	    /* Get securityLevel e.g. snmp.securityLevel= authPriv */
	    String PropSecLvl = AutomaticsPropertyUtility.getProperty("snmp.securityLevel");
	    
	    if (null == PropSecLvl) {
		LOGGER.error("Security level is null");
	    } else {
		Snmpv3SecurityLevel securityLevel = Snmpv3SecurityLevel.valueOf(PropSecLvl);
		autObj.setSecurityLevel(securityLevel);
		LOGGER.info("Security level is set " + securityLevel);
	    }
	    
	    /* Get securityName e.g. snmp.securityName= linuser */
	    String securityName = AutomaticsPropertyUtility.getProperty("snmp.securityName");
	    if (null == securityName) {
		LOGGER.error("Security Name is null");
	    } else {
		autObj.setSecurityName(securityName);
		LOGGER.info("Security level is set " + securityName);
	    }
	    
	    /* Get securityOptions e.g. snmp.securityOptions= -a SHA -A linuserpass -x DES -X linprivpass */
	    String securityOptions = AutomaticsPropertyUtility.getProperty("snmp.securityOptions");
	    if (null == securityOptions) {
		LOGGER.error("Security Options are null");
	    } else {
		autObj.setSecurityOptions(securityOptions);
		LOGGER.info("Security Options are set " + securityOptions);
	    }
	}

	/*
	 * Extracting SNMP Param i.e, Community String from AutomaticsPropertyUtility and setting it's value, if no
	 * value configured default "private" will be configured as through which both get/set can be performed
	 */
	else if (snmpParams.getSnmpVersion() == SnmpProtocol.SNMP_V2) {
	    LOGGER.info(" SNMP PROVIDER V2");
	    String snmpCommunity = AutomaticsPropertyUtility.getProperty("snmp.community");
	    if (snmpCommunity == null) {
		snmpCommunity = DEFAULT_SNMP_COMMUNITY;
	    }
	    autObj.setCommunity(snmpCommunity);
	    LOGGER.info(" SNMP PROVIDER community " + snmpCommunity);
	}

	return autObj;
    }

    /**
     * Update snmp params data required for snmp communication.
     * 
     * @param dut
     *            Device on snmp commands are executed
     * @param snmpParams
     *            Snmp params
     * @return SnmpParams data for snmp communication
     */
    public SnmpParams updateSnmpParams(Dut dut, SnmpParams snmpParams) {
	/* Setting IP Address if default value is null */
	if (snmpParams.getIpAddress() == null) {
	    if (dut.getHostIpAddress() != null) {
		snmpParams.setIpAddress(dut.getHostIpAddress());
		LOGGER.info("Target IP is " + dut.getHostIpAddress());
	    }
	}
	return snmpParams;
    }
}

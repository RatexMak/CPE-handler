/**
 * Copyright 2022 Vodafone Group plc
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * DeviceConnectionProviderImplTest class runs basic level of testing 
 * the DeviceConnectionProviderImpl execute methods
 */
package com.connectionproviders.deviceconnectionprovider;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automatics.device.Device;

/**
 * Unit test for deviceConnectionProviderImpl.
 */
public class DeviceConnectionProviderImplTest 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceConnectionProviderImplTest.class);
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
    	DeviceConnectionProviderImpl ins = new DeviceConnectionProviderImpl();
    	String hostIp = ""; // target hw IP need to be provided
    	String command = "uname -a";
    	String connectionType = "SSH";
    	long timeOutMilliSecs = 9000;
    	
    	String ret = ins.execute(hostIp, command, timeOutMilliSecs, connectionType);
    	LOGGER.info("exe1: The return value is " + ret);
    	
    	Device dev = new Device();
    	dev.setHostIp4Address(hostIp);
    	LOGGER.info("dev HostIpAddress " + dev.getHostIpAddress());

    	ret = ins.execute(dev, "df");
    	
    	List<String> list=new ArrayList<String>();
    	
    	list.add("uname -a");
    	list.add("cat /version.txt");
    	list.add("ls -l");
    	list.add("uptime");
    	
    	ret = ins.execute(dev, list);
    	LOGGER.info("Response with commandlist metnod is:  " + ret);
        assertTrue( true );
    }
}

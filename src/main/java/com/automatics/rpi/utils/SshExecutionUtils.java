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

package com.automatics.rpi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.automatics.utils.CommonMethods;

/**
 * Utils APIs for SSH related execution
 *
 */
public class SshExecutionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshExecutionUtils.class);

    /**
     * Executes regex validation identify supplied command contains 'sed' command
     * 
     * @param command
     * @return boolean based on whether input command has sed or not
     */
    public static boolean isSedCommandPresent(String command) {
	LOGGER.debug("Checking for sed command");
	boolean status = false;
	String regexTypicalSedComand = "^sed\\s+";
	String regexTypicalSedComandwithQuote = "\"sed\\s+";
	String regexSedComandWithPipe = "\\|\\s*sed\\s+";
	// LOGGER.debug("Checking for sed command of type " + regexTypicalSedComand);
	boolean temp = CommonMethods.validateTextUsingRegularExpression(command, regexTypicalSedComand);
	// LOGGER.debug("Checking for sed command of type " + regexSedComandWithPipe);
	status = CommonMethods.validateTextUsingRegularExpression(command, regexSedComandWithPipe);
	status = temp || status
		|| CommonMethods.validateTextUsingRegularExpression(command, regexTypicalSedComandwithQuote);
	return status;
    }

}

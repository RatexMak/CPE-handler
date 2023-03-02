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
package com.automatics.zte.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.automatics.zte.constants.Constants;

public class CommonMethods {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonMethods.class);

    public static void sleep(long milliseconds) {

	try {
	    Thread.sleep(milliseconds);
	} catch (InterruptedException e) {
	    LOGGER.error("Sleep interrupted " + e.getMessage());
	}
    }

    public static String removeSecurityBannerFromResponse(String response) {
	if (isNotNull(response)) {
	    int indexEndOfPrivacyMessage = response.lastIndexOf(Constants.END_OF_SSH_CONNECTION_PRIVACY_MESSAGE);
	    if (indexEndOfPrivacyMessage > 0) {
		response = response
			.substring(indexEndOfPrivacyMessage + Constants.END_OF_SSH_CONNECTION_PRIVACY_MESSAGE.length());
	    }
	}
	return response;
    }

    public static boolean isNotNull(String value) {

	boolean isNotNull = !isNull(value);
	return isNotNull;
    }

    public static boolean isNull(String value) {

	return ((value == null) || (value.trim().length() == 0));
    }

}

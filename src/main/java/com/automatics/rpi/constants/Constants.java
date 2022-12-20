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
package com.automatics.rpi.constants;

public class Constants {
    public static String END_OF_SSH_CONNECTION_PRIVACY_MESSAGE = "law enforcement.";
    public static final long TEN_SECONDS = 10000;
    public static final String NEW_LINE = "\n";

    /**
     * Property to keep command execution response wait time for non-RDK devices during ssh connection
     */
    public static final String PROPS_NON_RDK_RESP_WAIT_TIME_MILLISEC = "nonrdk.resp.wait.time.millisecs";

    /**
     * Property to keep command execution response wait time for RDK devices during ssh connection
     */
    public static final String PROPS_RDK_RESP_WAIT_TIME_MILLISEC = "rdk.resp.wait.time.millisecs";

}

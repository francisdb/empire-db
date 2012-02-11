/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.empire.struts2.websample.web;

import org.apache.empire.xml.XMLConfiguration;

public class SampleConfig extends XMLConfiguration {
	
	private String databaseProvider = "webservice";
	
	private String serviceHost = "http://localhost";

	private String servicePort = "8080";

	private String serviceName = "WSSAMPLE";


	/**
	 * Initialize the configuration
	 */
	public void init(String filename) {
		// Read the properties file
		super.init(filename, false);
		// Done
		readProperties(this, "properties");
		// Reader Provider Properties
		readProperties(this, "properties-" + databaseProvider);
	}

	public String getDatabaseProvider() {
		return databaseProvider;
	}

	public String getServiceHost() {
		return serviceHost;
	}

	public String getServicePort() {
		return servicePort;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceAddress() {
		return serviceHost + ":" + servicePort + "/" + serviceName;
	}

	// ------- Setters -------

	public void setDatabaseProvider(String databaseProvider) {
		this.databaseProvider = databaseProvider;
	}

	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}

	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}

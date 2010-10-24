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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.empire.samples.cxf.wssample.client.EmployeeServiceClient;
import org.apache.empire.struts2.actionsupport.TextProviderActionSupport;
import org.apache.empire.struts2.html.HtmlTagDictionary;
import org.apache.empire.struts2.web.AppContext;
import org.apache.empire.struts2.web.WebApplication;
import org.apache.empire.struts2.websample.ws.SampleBeanDomain;

public class SampleApplication implements WebApplication {
	// Logger
	protected static Log log = LogFactory.getLog(SampleApplication.class);

	// must be singleton
	private static SampleApplication application;

	public static SampleApplication getInstance() {
		return application;
	}

	// Non-Static
	private SampleBeanDomain beanDomain = new SampleBeanDomain();
	private SampleConfig config = new SampleConfig();
	
	private EmployeeServiceClient ws = new EmployeeServiceClient(config
			.getServiceAddress());

	public void init(AppContext appContext) {
		try {

			// Application
			if (application != null) {
				log.fatal("Application has already been initiallized!");
				return;
			}
			application = this;

			// register all controls
			// InputControlManager.registerControl("myType", new
			// MyTypeInputControl());
			config.init(appContext.getRealPath("WEB-INF/config.xml"));

			// Set Html Dictionary
			HtmlTagDictionary.set(new SampleHtmlTagDictionary());

			// Set Database to Servlet Context
			appContext.setAttribute("db", beanDomain);

			// Get a Webservice Connection
			log.info("*** testing Webservice Connection ***");
			ws = initEmployeeServiceClient();

			// Disable Message caching
			TextProviderActionSupport.setCachingEnabled(false);

			// Done
			log.info("Application initialized ");

		} catch (Exception e) {
			// Error
			log.info(e.toString());
			e.printStackTrace();
		}

	}

	public SampleBeanDomain getBeanDomain() {
		return beanDomain;
	}

	public EmployeeServiceClient getEmployeeServiceClient() {
		return ws;
	}

	private EmployeeServiceClient initEmployeeServiceClient() {
		String addr = config.getServiceAddress();
		try {
			EmployeeServiceClient emp = new EmployeeServiceClient(addr);
			return emp;
		} catch (Throwable e) {
			log.error("Failed to connect directly to '" + addr + "'");
			throw new RuntimeException(e);
		}

	}

}

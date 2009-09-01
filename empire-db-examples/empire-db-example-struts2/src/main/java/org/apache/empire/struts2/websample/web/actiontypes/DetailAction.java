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
package org.apache.empire.struts2.websample.web.actiontypes;


public abstract class DetailAction extends Action
{
    /**
     * Action mappings
     */
    public static final String INPUT  = "input";
    public static final String RETURN = "return";

    // Detail Action
    public DetailAction()
    {
        // Default constructor
    }
        
    public abstract String doCreate();
    
    public abstract String doLoad();

    public abstract String doSave();

    public abstract String doDelete();

    // Optional overridable
    public String doCancel()
    {
        return RETURN;
    }
    
}

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
package org.apache.empire.struts2.actionsupport;

import java.util.ArrayList;
import java.util.List;

import org.apache.empire.db.DBCommand;
import org.apache.empire.db.DBReader;


/**
 * BeanListActionSupport
 * <p>
 * This class provides functions for handling list output from a database query through an list of JavaBeans.
 * </p> 
 * @author Rainer
 */
public class BeanListActionSupport<T> extends ListActionSupport
{
    protected ArrayList<T> list = null;
    
    protected Class<T> beanClass;

    public BeanListActionSupport(ActionBase action, Class<T> beanClass, String propertyName)
    {
        super(action, propertyName);
        // Set Bean Class
        this.beanClass = beanClass;
    }
    
    public ArrayList<T> getList()
    {
        if (list==null)
            log.warn("Bean List has not been initialized!");
        return list;
    }

    // SupplierReader
    public boolean initBeanList(DBCommand cmd)
    {
        DBReader reader = new DBReader();
        try {
            // Open Suppier Reader
            reader.open(cmd, action.getConnection());
            // Move to desired Position
            int first = this.getFirstItemIndex();
            if (first>0 && !reader.skipRows(first))
            {   // Page is not valid. Try again from beginning
                reader.close();
                setFirstItem(0);
                initBeanList(cmd);
            }
            // Read List
            list = reader.getBeanList(beanClass, getPageSize());
            return true;
            
        } catch(Exception e ) {
            action.setActionError(e);
            return false;
            
        } finally {
            reader.close();
        }
    }

	public void setList(List<T> list)
	{
		this.list = new ArrayList<T>(list);
	}
}

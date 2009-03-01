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
package org.apache.empire.db;

import org.apache.empire.commons.Options;
import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBTable;
import org.apache.empire.db.DBTableColumn;


/**
 * This is the basic database for testing
 *
 */
public class TestDB extends DBDatabase
{
    /**
     * This class represents the definition of the Departments table.
     */
    public static class Departments extends DBTable
    {
        public final DBTableColumn ID;
        public final DBTableColumn NAME;
        public final DBTableColumn HEAD;
        public final DBTableColumn BUSINESS_UNIT;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Departments(DBDatabase db)
        {
            super("DEPARTMENTS", db);
            // ID
            ID   = addColumn("DEPARTMENT_ID",    DataType.AUTOINC,       0, true, "DEP_ID_SEQUENCE");
            NAME            = addColumn("NAME",             DataType.TEXT,         80, true);
            HEAD            = addColumn("HEAD",             DataType.TEXT,         80, false);
            BUSINESS_UNIT   = addColumn("BUSINESS_UNIT",    DataType.TEXT,          4, true, "ITTK");
            UPDATE_TIMESTAMP= addColumn("UPDATE_TIMESTAMP", DataType.DATETIME,      0, true);

            // Primary Key
            setPrimaryKey(ID);
            // Set other Indexes
            addIndex("DEARTMENT_NAME_IDX", true, new DBColumn[] { NAME });
            // Set timestamp column for save updates
            setTimestampColumn(UPDATE_TIMESTAMP);
        }
    }

    /**
     * This class represents the definition of the Employees table.
     */
    public static class Employees extends DBTable
    {
        public final DBTableColumn ID;
        public final DBTableColumn SALUTATION;
        public final DBTableColumn FIRSTNAME;
        public final DBTableColumn LASTNAME;
        public final DBTableColumn DATE_OF_BIRTH;
        public final DBTableColumn DEPARTMENT_ID;
        public final DBTableColumn GENDER;
        public final DBTableColumn PHONE_NUMBER;
        public final DBTableColumn EMAIL;
        public final DBTableColumn SALARY;
        public final DBTableColumn RETIRED;
        public final DBTableColumn UPDATE_TIMESTAMP;

        public Employees(DBDatabase db)
        {
            super("EMPLOYEES", db);
            // ID
            ID     = addColumn("EMPLOYEE_ID",      DataType.AUTOINC,      0, true, "EMPLOYEE_ID_SEQUENCE");
            SALUTATION      = addColumn("SALUTATION",       DataType.TEXT,        20, false);
            FIRSTNAME       = addColumn("FIRSTNAME",        DataType.TEXT,        40, true);
            LASTNAME        = addColumn("LASTNAME",         DataType.TEXT,        40, true);
            DATE_OF_BIRTH   = addColumn("DATE_OF_BIRTH",    DataType.DATE,         0, false);
            DEPARTMENT_ID   = addColumn("ID",    DataType.INTEGER,      0, true);
            GENDER          = addColumn("GENDER",           DataType.TEXT,         1, false);
            PHONE_NUMBER    = addColumn("PHONE_NUMBER",     DataType.TEXT,        40, false);
            EMAIL           = addColumn("EMAIL",            DataType.TEXT,        80, false);
            SALARY          = addColumn("SALARY",           DataType.DECIMAL,   10.2, false);
            RETIRED         = addColumn("RETIRED",          DataType.BOOL,         0, true, false);
            UPDATE_TIMESTAMP= addColumn("UPDATE_TIMESTAMP", DataType.DATETIME,     0, true);

            // Primary Key
            setPrimaryKey(ID);
            // Set other Indexes
            addIndex("EMPLOYEE_NAME_IDX", true, new DBColumn[] { FIRSTNAME, LASTNAME, DATE_OF_BIRTH });
            // Set timestamp column for save updates
            setTimestampColumn(UPDATE_TIMESTAMP);

            // Create Options for GENDER column
            Options genders = new Options();
            genders.set("M", "Male");
            genders.set("F", "Female");
            GENDER.setOptions(genders);
        }
    }

    // Declare all Tables and Views here
    public final Departments  DEPARTMENT = new Departments(this);
    public final Employees    EMPLOYEE   = new Employees(this);

    /**
     * Constructor of the TestDB data model description
     *
     * Put all foreigen key realtions here.
     */
    public TestDB()
    {
        // Define Foreign-Key Relations
        addRelation( EMPLOYEE.DEPARTMENT_ID.referenceOn( DEPARTMENT.ID ));
    }

}
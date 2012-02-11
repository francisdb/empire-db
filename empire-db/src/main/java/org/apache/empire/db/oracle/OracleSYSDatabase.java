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
package org.apache.empire.db.oracle;

// java
import org.apache.empire.data.DataType;
import org.apache.empire.db.*;

/**
 * Represents the data model of the system tables. They are required to parse the data models.
 * <P>
 * 
 * 
 */
public class OracleSYSDatabase extends DBDatabase
{

    private final static long serialVersionUID = 1L;

    // Table for all tables of a schema with their comments
    public static class DBTabComments extends DBTable
    {
        private static final long serialVersionUID = 1L;

        public DBTableColumn      C_OWNER;
        public DBTableColumn      C_TABLE_NAME;
        public DBTableColumn      C_TABLE_TYPE;
        public DBTableColumn      C_COMMENTS;

        // Constructor
        public DBTabComments(DBDatabase db)
        {
            super("ALL_TAB_COMMENTS", db);
            // add all Colums
            C_OWNER = addColumn("OWNER", DataType.TEXT, 50, false);
            C_TABLE_NAME = addColumn("TABLE_NAME", DataType.TEXT, 50, false);
            C_TABLE_TYPE = addColumn("TABLE_TYPE", DataType.TEXT, 50, false);
            C_COMMENTS = addColumn("COMMENTS", DataType.TEXT, 500, false);
        }
    }

    // Table for all constraints
    public static class DBConstraints extends DBTable
    {
        private final static long serialVersionUID = 1L;

        public DBTableColumn      C_CONSTRAINT_NAME;
        public DBTableColumn      C_TABLE_NAME;
        public DBTableColumn      C_R_CONSTRAINT_NAME;
        public DBTableColumn      C_CONSTRAINT_TYPE;
        public DBTableColumn      C_STATUS;

        // Constructor
        public DBConstraints(DBDatabase db)
        {
            super("ALL_CONSTRAINTS", db);
            // add all Colums

            C_CONSTRAINT_NAME = addColumn("CONSTRAINT_NAME", DataType.TEXT, 100, false);
            C_TABLE_NAME = addColumn("TABLE_NAME", DataType.TEXT, 100, false);
            C_R_CONSTRAINT_NAME = addColumn("R_CONSTRAINT_NAME", DataType.TEXT, 100, false);
            C_CONSTRAINT_TYPE = addColumn("CONSTRAINT_TYPE", DataType.TEXT, 1, false);
            C_STATUS = addColumn("STATUS", DataType.TEXT, 20, false);
        }
    }

    // Table for Columns and Tables a constraint is associated to
    public static class DBUserConCol extends DBTable
    {
        private final static long serialVersionUID = 1L;

        public DBTableColumn      C_CONSTRAINT_NAME;
        public DBTableColumn      C_TABLE_NAME;
        public DBTableColumn      C_COLUMN_NAME;
        public DBTableColumn      C_OWNER;

        // Constructor
        public DBUserConCol(DBDatabase db)
        {
            super("USER_CONS_COLUMNS", db);
            // add all Colums

            C_CONSTRAINT_NAME = addColumn("CONSTRAINT_NAME", DataType.TEXT, 100, false);
            C_TABLE_NAME = addColumn("TABLE_NAME", DataType.TEXT, 100, false);
            C_COLUMN_NAME = addColumn("COLUMN_NAME", DataType.TEXT, 100, false);
            C_OWNER = addColumn("OWNER", DataType.TEXT, 100, false);
            setPrimaryKey(C_CONSTRAINT_NAME);
        }
    }

    // Table for all columns of a schema with their comments
    public static class DBColInfo extends DBTable
    {
        private final static long serialVersionUID = 1L;

        public DBTableColumn      C_OWNER;
        public DBTableColumn      C_TABLE_NAME;
        public DBTableColumn      C_COLUMN_NAME;
        public DBTableColumn      C_DATA_TYPE;
        public DBTableColumn      C_DATA_TYPE_MOD;
        public DBTableColumn      C_DATA_TYPE_OWNER;
        public DBTableColumn      C_NULLABLE;
        public DBTableColumn      C_DATA_LENGTH;
        public DBTableColumn      C_DATA_PRECISION;
        public DBTableColumn      C_DATA_SCALE;
        public DBTableColumn      C_CHAR_LENGTH;

        // Constructor
        public DBColInfo(DBDatabase db)
        {
            super("ALL_TAB_COLUMNS", db);
            // add all Colums
            C_OWNER = addColumn("OWNER", DataType.TEXT, 30, false);
            C_TABLE_NAME = addColumn("TABLE_NAME", DataType.TEXT, 30, false);
            C_COLUMN_NAME = addColumn("COLUMN_NAME", DataType.TEXT, 30, false);
            C_DATA_TYPE = addColumn("DATA_TYPE", DataType.TEXT, 50, false);
            C_DATA_TYPE_MOD = addColumn("DATA_TYPE_MOD", DataType.TEXT, 50, false);
            C_DATA_TYPE_OWNER = addColumn("DATA_TYPE_OWNER", DataType.TEXT, 50, false);
            C_NULLABLE = addColumn("NULLABLE", DataType.TEXT, 1, false);
            C_DATA_LENGTH = addColumn("DATA_LENGTH", DataType.DECIMAL, 0, false);
            C_DATA_PRECISION = addColumn("DATA_PRECISION", DataType.DECIMAL, 0, false);
            C_DATA_SCALE = addColumn("DATA_SCALE", DataType.DECIMAL, 0, false);
            C_CHAR_LENGTH = addColumn("CHAR_LENGTH", DataType.DECIMAL, 0, false);
        }
    }

    // Table for all columns of a schema with their comments
    public static class DBColComments extends DBTable
    {
        private final static long serialVersionUID = 1L;

        public DBTableColumn      C_OWNER;
        public DBTableColumn      C_TABLE_NAME;
        public DBTableColumn      C_COLUMN_NAME;
        public DBTableColumn      C_COMMENTS;

        // Constructor
        public DBColComments(DBDatabase db)
        {
            super("ALL_COL_COMMENTS", db);
            // add all Colums
            C_OWNER = addColumn("OWNER", DataType.TEXT, 50, false);
            C_TABLE_NAME = addColumn("TABLE_NAME", DataType.TEXT, 50, false);
            C_COLUMN_NAME = addColumn("COLUMN_NAME", DataType.TEXT, 50, false);
            C_COMMENTS = addColumn("COMMENTS", DataType.TEXT, 500, false);
        }
    }

    public DBTabComments TC = null;
    public DBColInfo     CI = null;
    public DBColComments CC = null;
    public DBConstraints CO = null;
    public DBUserConCol  UC = null;

    public OracleSYSDatabase(DBDatabaseDriverOracle driver)
    {
        // System schema
        super("SYS");
        // Set Driver (do not use open(...)!)
        this.driver = driver;
        // Add Tables
        TC = new DBTabComments(this);
        CI = new DBColInfo(this);
        CC = new DBColComments(this);
        CO = new DBConstraints(this);
        UC = new DBUserConCol(this);
    }

}
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

// java
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.empire.commons.Errors;
import org.apache.empire.commons.Options;
import org.apache.empire.data.DataType;
import org.w3c.dom.Element;


/**
 * This abstract class handles the creation of the SQL-Commands.
 * There are inner classes to construct different SQL-Commands.
 * <P>
 * 
 *
 */
public abstract class DBCommandExpr extends DBExpr
{
    // Internal Classes
    protected static class DBCmdQuery extends DBRowSet
    {
        private DBCommandExpr cmd;

        /**
         * Creates a new DBCmdQueryObject
         * @param cmd the command expression
         * @param colList 
         */
        public DBCmdQuery(DBCommandExpr cmd, DBColumnExpr[] colList)
        { // Set the column expressions
            super(cmd.getDatabase());
            this.cmd = cmd;
            // Add Expressions to vector
            for (int i = 0; i < colList.length; i++)
                columns.add(colList[i].getUpdateColumn());
        }

        /** Not applicable - returns null */
        @Override
        public String getName()
        {
            return null;
        }

        /** Not applicable - returns null */
        @Override
        public String getAlias()
        {
            return null;
        }

        /**
         * @see org.apache.empire.db.DBExpr#addReferencedColumns(Set)
         */
        @Override
        public void addReferencedColumns(Set<DBColumn> list)
        {
            list.addAll(columns);
        }

        /**
         * Creates the SQL-Command adds the select statement into the SQL-Command.
         * 
         * @param buf the SQL-Command
         * @param context the current SQL-Command context
         */
        @Override
        public void addSQL(StringBuilder buf, long context)
        {
            buf.append("(");
            buf.append(cmd.getSelect());
            buf.append(")");
        }

        /**
         * Prints the error message: ERR_NOTSUPPORTED.
         * 
         * @return null
         */
        @Override
        public DBColumn[] getKeyColumns()
        {
            error(Errors.NotSupported, "getKeyColumns");
            return null;
        }

        /**
         * Prints the error message: ERR_NOTSUPPORTED.
         * 
         * @return null
         */
        @Override
        public Object[] getRecordKey(DBRecord rec)
        {
            error(Errors.NotSupported, "getRecordKey");
            return null;
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public boolean initRecord(DBRecord rec, Object[] keyValues)
        {
            return error(Errors.NotSupported, "initRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public boolean createRecord(DBRecord rec, Connection conn)
        {
            return error(Errors.NotSupported, "addRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public boolean readRecord(DBRecord rec, Object[] keys, Connection conn)
        {
            return error(Errors.NotSupported, "getRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public boolean updateRecord(DBRecord rec, Connection conn)
        {
            return error(Errors.NotSupported, "updateRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public boolean deleteRecord(Object[] keys, Connection conn)
        {
            return error(Errors.NotSupported, "deleteRecord");
        }
    }

    /**
     * This class wrapps a column of sql command in a special command column object. 
     */
    protected static class DBCmdColumn extends DBColumn
    {
        private DBColumnExpr expr;

        /**
         * Constructs a new DBCmdColumn object
         * 
         * @param query the row set
         * @param expr the column
         */
        public DBCmdColumn(DBRowSet query, DBColumnExpr expr)
        { // call base
            super(query, expr.getName());
            // set Expression
            this.expr = expr;
        }

        /**
         * create the SQL-Command set the expression name to the SQL-Command
         * 
         * @param buf the SQL-Command
         * @param context the current SQL-Command context
         */
        @Override
        public void addSQL(StringBuilder buf, long context)
        { // append UNQUALIFIED name only!
            buf.append(expr.getName());
        }

        /**
         * Returns the data type of the DBColumnExpr object.
         * 
         * @return the data type
         */
        @Override
        public DataType getDataType()
        {
            return expr.getDataType();
        }

        /**
         * Not applicable - always returns 0.
         */
        @Override
        public double getSize()
        {
            return 0;
        }

        /**
         * Not applicable - always returns true
         */
        @Override
        public boolean isReadOnly()
        {
            return true; // expr.isReadOnly();
        }

        /**
         * Checks whether the column is mandatory.
         * 
         * @return true if the column is mandatory or false otherwise
         */
        @Override
        public boolean isRequired()
        {
            return false;
        }

        /**
         * Get Attributes of underlying table column.
         */
        @Override
        public Object getAttribute(String name)
        {
            if (attributes != null && attributes.containsKey(name))
                return attributes.get(name);
            // Otherwise ask expression
            DBColumn column = expr.getUpdateColumn();
            if (column!=null)
                return column.getAttribute(name);
            return null;
        }

        /**
         * Get Options of underlying table column.
         */
        @Override
        public Options getOptions()
        {
            if (options != null)
                return options;
            // Otherwise ask expression
            DBColumn column = expr.getUpdateColumn();
            if (column!=null)
                return column.getOptions();
            return null;
        }

        /**
         * Not applicable - always returns true.
         */
        @Override
        public boolean checkValue(Object value)
        {
            return true;
        }

        /**
         * Adds the expression definition to the xml element.
         */
        @Override
        public Element addXml(Element parent, long flags)
        {
            return expr.addXml(parent, flags);
        }

    }

    protected static class DBOrderByInfo extends DBExpr
    {
        public DBColumnExpr expr;
        public boolean desc;

        /**
         * Construct a new DBOrderByInfo object set the specified
         * parameters to this object.
         * 
         * @param expr the column 
         * @param desc set true for descending or false for ascending
         */
        public DBOrderByInfo(DBColumnExpr expr, boolean desc)
        {
            this.expr = expr;
            this.desc = desc;
        }

        /** 
         * Returns the current database object 
         */
        @Override
        public DBDatabase getDatabase()
        {
            return expr.getDatabase();
        }

        /*
         * @see org.apache.empire.db.DBExpr#addReferencedColumns(Set)
         */
        @Override
        public void addReferencedColumns(Set<DBColumn> list)
        {
            expr.addReferencedColumns(list);
        }

        /**
         * Creates the SQL-Command set "DESC" command after the order by statement.
         * 
         * @param buf the SQL-Command
         * @param context the current SQL-Command context
         */
        @Override
        public void addSQL(StringBuilder buf, long context)
        { 
            // Set SQL-Order By
            expr.addSQL(buf, context);
            // only need to add DESC as default is ASC
            if (desc)
            {
                buf.append(" DESC");
            }
        }
    }

    // Members
    protected DBCmdQuery          cmdQuery = null;
    protected List<DBOrderByInfo> orderBy  = null;

    /** Constructs an empty DBCommandExpr object */
    public DBCommandExpr()
    {
        // Default Construtor
    }

    // get Select SQL
    public abstract boolean isValid();

    public abstract boolean getSelect(StringBuilder buf);

    public abstract DBColumnExpr[] getSelectExprList();

    public DBColumnExpr getCmdColumn(DBColumnExpr col)
    {
        // Check the Instance of col
        if ((col instanceof DBCmdColumn))
        { // Check Owner
            DBCmdColumn c = (DBCmdColumn) col;
            if (c.getRowSet() == cmdQuery)
                return col; // it's aready a command column
            // extract the expression
            col = c.expr;
        }
        // Check if we already have a Command Query
        if (cmdQuery == null)
            cmdQuery = new DBCmdQuery(this, getSelectExprList());
        // create a command column
        return new DBCmdColumn(cmdQuery, col);
    }

    public DBColumnExpr getCmdColumn(int i)
    {
        DBColumnExpr[] list = getSelectExprList();
        if (i >= list.length)
            return null;
        return getCmdColumn(list[i]);
    }

    /**
     * returns an SQL select command for querying records.
     * @return the SQL-Command
     */
    public String getSelect()
    {
        StringBuilder buf = new StringBuilder();
        if (getSelect(buf) == false)
        {
            log.error(getErrorMessage());
            return null;
        }
        return buf.toString();
    }

    /**
     * Internally used to build a string from a list of database expressions
     * @param buf the sql target buffer
     * @param list list of database objects
     * @param context the sql command context
     * @param separator string to use as separator between list items
     */
    protected void addListExpr(StringBuilder buf, List<? extends DBExpr> list, long context, String separator)
    {
        for (int i = 0; i < list.size(); i++)
        { // Selectfelder zusammenbauen
            if (i > 0)
                buf.append(separator);
            list.get(i).addSQL(buf, context);
        }
    }

    /**
     * Creates the SQL-Command.
     * 
     * @param buf the SQL-Command
     * @param context the current SQL-Command context
     */
    @Override
    public void addSQL(StringBuilder buf, long context)
    {
        buf.append("(");
        buf.append(getSelect());
        buf.append(")");
    }

    /**
     * Constructs a new DBCombinedCmd object with this object,
     * the key word= "UNION" and the selected DBCommandExpr.
     * 
     * @see org.apache.empire.db.DBCombinedCmd
     * @param other the secend DBCommandExpr
     * @return the new DBCombinedCmd object
     */
    // Combinations
    public DBCommandExpr union(DBCommandExpr other)
    {
        return new DBCombinedCmd(this, "UNION", other);
    }

    /**
     * Constructs a new DBCombinedCmd object with this object,
     * the key word= "INTERSECT" and the selected DBCommandExpr.
     * 
     * @param other the secend DBCommandExpr
     * @return the new DBCombinedCmd object
     */
    public DBCommandExpr intersect(DBCommandExpr other)
    {
        return new DBCombinedCmd(this, "INTERSECT", other);
    }

    /**
     * Clears the list of order by expressions.
     */
    public void clearOrderBy()
    {
        orderBy = null;
    }

    /**
     * Adds an order by with ascending or descending order
     * 
     * @param expr the DBColumnExpr object
     * @param desc if true, the results from select statement will sort top down
     */
    public void orderBy(DBColumnExpr expr, boolean desc)
    {
        if (orderBy == null)
            orderBy = new ArrayList<DBOrderByInfo>();
        // Add order by excpression
        orderBy.add(new DBOrderByInfo(expr, desc));
    }

    /**
     * Adds an order by with ascending order
     * 
     * @param expr the column 
     * 
     * @see org.apache.empire.db.DBCommandExpr#orderBy(DBColumnExpr, boolean)
     */
    public void orderBy(DBColumnExpr expr)
    {
        orderBy(expr, false);
    }

    /**
     * Create the insert into SQL-Command which copies data
     * from a select statement to a destination table.
     * 
     * @return the insert into SQL-Command
     */
    protected String getInsertInto(DBTable table, DBColumnExpr[] select, List<DBColumnExpr> columns)
    {
        if (select == null)
        { // invalid Object
            error(Errors.ObjectNotValid, getClass().getName());
            return null;
        }
        StringBuilder buf = new StringBuilder("INSERT INTO ");
        table.addSQL(buf, CTX_FULLNAME);
        // destination columns
        if (columns != null && columns.size() > 0)
        { // Check Count
            if (columns.size() != select.length)
            {
                error(Errors.InvalidArg, columns, "columns");
                return null;
            }
            // Append Names
            buf.append(" (");
            addListExpr(buf, columns, CTX_NAME, ", ");
            buf.append(")");
        }
        // append select statement
        buf.append("\r\n");
        getSelect(buf);
        // done
        return buf.toString();
    }

    /**
     * Create the insert into SQL-Command which copies
     * data from a select statement to a destination table.
     * 
     * @param table the table 
     * @param columns the columns
     * 
     * @return the insert into SQL-Command
     */
    public final String getInsertInto(DBTable table, List<DBColumnExpr> columns)
    {
        return getInsertInto(table, getSelectExprList(), columns);
    }

    /**
     * Create the insert into SQL-Command which copies
     * data from a select statement to a destination table.
     * 
     * @param table the table 
     * 
     * @return the insert into SQL-Command
     */
    public final String getInsertInto(DBTable table)
    {
        DBColumnExpr[] select = getSelectExprList();
        if (select == null || select.length < 1)
        {
            error(Errors.ObjectNotValid, getClass().getName());
            return null;
        }
        // Match Columns
        List<DBColumnExpr> inscols = new ArrayList<DBColumnExpr>(select.length);
        for (int i = 0; i < select.length; i++)
        {
            DBColumnExpr expr = select[i];
            DBColumn col = table.getColumn(expr.getName());
            if (col == null)
            { // Cannot find a match for that name
                log.warn("InsertInto: Column " + expr.getName() + " not found!");
                col = table.getColumn(i);
            }
            inscols.add(col);
        }
        return getInsertInto(table, select, inscols);
    }
}

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

import org.apache.empire.commons.Options;
import org.apache.empire.data.DataType;
import org.apache.empire.db.expr.order.DBOrderByExpr;
import org.apache.empire.exceptions.InvalidArgumentException;
import org.apache.empire.exceptions.NotSupportedException;
import org.apache.empire.exceptions.ObjectNotValidException;
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
    private final static long serialVersionUID = 1L;

    // Internal Classes
    protected static class DBCmdQuery extends DBRowSet
    {
        private final static long serialVersionUID = 1L;
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
            throw new NotSupportedException(this, "getKeyColumns");
        }

        /**
         * Prints the error message: ERR_NOTSUPPORTED.
         * 
         * @return null
         */
        @Override
        public Object[] getRecordKey(DBRecord rec)
        {
            throw new NotSupportedException(this, "getRecordKey");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public void initRecord(DBRecord rec, Object[] keyValues)
        {
            throw new NotSupportedException(this, "initRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public void createRecord(DBRecord rec, Connection conn)
        {
            throw new NotSupportedException(this, "addRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public void readRecord(DBRecord rec, Object[] keys, Connection conn)
        {
            throw new NotSupportedException(this, "getRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public void updateRecord(DBRecord rec, Connection conn)
        {
            throw new NotSupportedException(this, "updateRecord");
        }

        /** Returns the error message: ERR_NOTSUPPORTED */
        @Override
        public void deleteRecord(Object[] keys, Connection conn)
        {
            throw new NotSupportedException(this, "deleteRecord");
        }
    }

    /**
     * This class wraps a column of sql command in a special command column object. 
     */
    protected static class DBCmdColumn extends DBColumn
    {
        private final static long serialVersionUID = 1L;
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
         * Not applicable - always returns false
         */
        @Override
        public boolean isAutoGenerated()
        {
            return false; // expr.isReadOnly();
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
        public void checkValue(Object value)
        {
            // Nothing to check.
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

    // Members
    protected DBCmdQuery          cmdQuery = null;
    protected List<DBOrderByExpr> orderBy  = null;

    /** Constructs an empty DBCommandExpr object */
    public DBCommandExpr()
    {
        // Default Constructor
    }

    // get Select SQL
    public abstract boolean isValid();

    public abstract void getSelect(StringBuilder buf);

    public abstract DBColumnExpr[] getSelectExprList();
    
    /**
     * returns an array holding all parameter values in the order of their occurrence.
     * To ensure the correct order, getSelect() should be called first.
     * @return an array of command parameter values 
     */
    public abstract Object[] getParamValues();

    /**
     * returns column expression that is specific for to this command and detached from its source.
     */
    protected DBColumnExpr getCmdColumn(DBColumnExpr col)
    {
        // Check the Instance of col
        if ((col instanceof DBCmdColumn))
        { // Check Owner
            DBCmdColumn c = (DBCmdColumn) col;
            if (c.getRowSet() == cmdQuery)
                return col; // it's already a command column
            // extract the expression
            col = c.expr;
        }
        // Check if we already have a Command Query
        if (cmdQuery == null)
            cmdQuery = new DBCmdQuery(this, getSelectExprList());
        // create a command column
        return new DBCmdColumn(cmdQuery, col);
    }

    /**
     * returns an SQL select command for querying records.
     * @return the SQL-Command
     */
    public final String getSelect()
    {
        StringBuilder sql = new StringBuilder();
        getSelect(sql);
        return sql.toString();
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
        {   // assemble select columns
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
     * @param other the second DBCommandExpr
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
     * Adds an order by expression the command
     * 
     * @param exprs vararg of orderBy expressions
     * 
     * @see org.apache.empire.db.DBCommandExpr#orderBy(DBColumnExpr, boolean)
     */
    public void orderBy(DBOrderByExpr... exprs)
    {
        if (orderBy == null)
            orderBy = new ArrayList<DBOrderByExpr>();
        // Add order by expression
        for (DBOrderByExpr expr : exprs)
        {
            orderBy.add(expr);
        }
    }

    /**
     * Adds a list of columns to the orderBy clause in ascending order
     * 
     * @param exprs vararg of column expressions
     */
    public final void orderBy(DBColumnExpr... exprs)
    {
        for (DBColumnExpr expr : exprs)
        {
            orderBy(new DBOrderByExpr(expr, false));
        }
    }

    /**
     * Adds an order by with ascending or descending order
     * 
     * @param expr the DBColumnExpr object
     * @param desc if true, the results from select statement will sort top down
     */
    public final void orderBy(DBColumnExpr expr, boolean desc)
    {
        orderBy(new DBOrderByExpr(expr, desc));
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
            throw new ObjectNotValidException(this);
        // prepare buffer
        StringBuilder buf = new StringBuilder("INSERT INTO ");
        table.addSQL(buf, CTX_FULLNAME);
        // destination columns
        if (columns != null && columns.size() > 0)
        { // Check Count
            if (columns.size() != select.length)
            {
                throw new InvalidArgumentException("columns", "size()!=select.length");
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
            throw new ObjectNotValidException(this);
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

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
package org.apache.empire.jsf2.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.empire.commons.ObjectUtils;
import org.apache.empire.commons.Options;
import org.apache.empire.commons.StringUtils;
import org.apache.empire.data.Column;
import org.apache.empire.data.ColumnExpr;
import org.apache.empire.data.DataType;
import org.apache.empire.data.Record;
import org.apache.empire.data.RecordData;
import org.apache.empire.db.DBDatabase;
import org.apache.empire.db.DBRecord;
import org.apache.empire.db.DBRowSet;
import org.apache.empire.exceptions.BeanPropertyGetException;
import org.apache.empire.exceptions.BeanPropertySetException;
import org.apache.empire.exceptions.InternalException;
import org.apache.empire.exceptions.InvalidArgumentException;
import org.apache.empire.exceptions.NotSupportedException;
import org.apache.empire.exceptions.PropertyReadOnlyException;
import org.apache.empire.jsf2.app.FacesApplication;
import org.apache.empire.jsf2.app.FacesUtils;
import org.apache.empire.jsf2.app.TextResolver;
import org.apache.empire.jsf2.components.InputTag;
import org.apache.empire.jsf2.components.LinkTag;
import org.apache.empire.jsf2.components.RecordTag;
import org.apache.empire.jsf2.controls.InputControl;
import org.apache.empire.jsf2.controls.InputControlManager;
import org.apache.empire.jsf2.controls.SelectInputControl;
import org.apache.empire.jsf2.controls.TextInputControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.faces.component.CompositeComponentStackManager;
import com.sun.faces.facelets.el.ContextualCompositeValueExpression;
import com.sun.faces.facelets.el.TagValueExpression;

public class TagEncodingHelper implements NamingContainer
{
    /**
     * Inner class that implements the ValueInfo
     * Only necessary because of name clash with value tag interface (i.e. getColumn())
     * 
     * @author Rainer
     */

    private static class ColumnExprWrapper implements Column
    {
        private final ColumnExpr expr;

        public ColumnExprWrapper(ColumnExpr expr)
        {
            this.expr = expr;
        }

        @Override
        public DataType getDataType()
        {
            return expr.getDataType();
        }

        @Override
        public String getName()
        {
            return expr.getName();
        }

        @Override
        public String getTitle()
        {
            return expr.getTitle();
        }

        @Override
        public String getControlType()
        {
            return expr.getControlType();
        }

        @Override
        public Object getAttribute(String name)
        {
            return expr.getAttribute(name);
        }

        @Override
        public Options getOptions()
        {
            return expr.getOptions();
        }

        @Override
        public String getBeanPropertyName()
        {
            return expr.getBeanPropertyName();
        }

        @Override
        public Column getSourceColumn()
        {
            return expr.getSourceColumn();
        }

        @Override
        public double getSize()
        {
            return 0;
        }

        @Override
        public boolean isRequired()
        {
            return false;
        }

        @Override
        public boolean isAutoGenerated()
        {
            return false;
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }

        @Override
        public void validate(Object value)
        {
        }
    }

    private class ValueInfoImpl implements InputControl.ValueInfo
    {
        public ValueInfoImpl(Column column, TextResolver resolver)
        {
            if (column==null)
                throw new InvalidArgumentException("column", resolver);
            if (resolver==null)
                throw new InvalidArgumentException("resolver", resolver);
        }

        /* Value Options */
        private boolean hasColumn()
        {
            return (column != null || getColumn() != null);
        }

        @Override
        public Column getColumn()
        {
            return column;
        }

        @Override
        public Object getValue(boolean evalExpression)
        {
            return getDataValue(evalExpression);
        }

        @Override
        public Options getOptions()
        {
            return getValueOptions();
        }

        /*
        @Override
        public Object getNullValue()
        {
            // null value
            Object attr = tag.getAttributes().get("default");
            if (attr != null)
                return attr;
            // Use Column default
            if (hasColumn())
                return column.getAttribute("default");
            // not available
            return null;
        }
        */

        @Override
        public String getFormat()
        {
            // null value
            Object attr = tag.getAttributes().get("format");
            if (attr != null)
                return attr.toString();
            // Use Column default
            if (hasColumn())
            { // from column
                Object format = column.getAttribute("format");
                if (format != null)
                    return format.toString();
            }
            // not available
            return null;
        }

        @Override
        public Locale getLocale()
        {
            return textResolver.getLocale();
        }

        @Override
        public String getText(String text)
        {
            return textResolver.resolveText(text);
        }
    }

    private class InputInfoImpl extends ValueInfoImpl implements InputControl.InputInfo
    {
        public InputInfoImpl(Column column, TextResolver resolver)
        {
            super(column, resolver);
        }

        @Override
        public void setValue(Object value)
        {
            setDataValue(value);
        }

        @Override
        public void validate(Object value)
        {
            // Make sure null values are not forced to be required
            boolean isNull = ObjectUtils.isEmpty(value);
            if (isNull && !isRequired())
                return;
            // validate through column
            column.validate(value);
        }

        @Override
        public boolean isRequired()
        {
            return isValueRequired();
        }

        @Override
        public boolean isDisabled()
        {
            return isReadOnly();
        }

        @Override
        public String getInputId()
        {
            Column c = getColumn();
            return c.getName(); // (c instanceof DBColumn) ? ((DBColumn)c).getFullName() : c.getName();
        }

        @Override
        public String getTabindex()
        {
            Object size = tag.getAttributes().get("tabindex");
            return StringUtils.toString(size);
        }

        @Override
        public String getStyleClass(String addlStyle)
        {
            String style = getTagStyleClass(addlStyle);
            if (isReadOnly())
                style += " eInpDis";
            return style; 
        }

        @Override
        public Object getAttribute(String name)
        {
            return getTagAttribute(name);
        }
    }

    // Logger
    private static final Logger log          = LoggerFactory.getLogger(TagEncodingHelper.class);

    public static final String COLATTR_ABBR_TITLE     = "ABBR_TITLE";       // Column title for abbreviations
    
    private final UIOutput      tag;
    private final String        tagCssStyle;
    private Column              column       = null;
    private Object              record       = null;
    private RecordTag           recordTag    = null;
    private Boolean             tagRequired  = null;
    private Boolean             hasValueExpr = null;
    private InputControl        control      = null;
    private TextResolver        textResolver = null;
    private Object              mostRecentValue = null;

    public TagEncodingHelper(UIOutput tag, String tagCssStyle)
    {
        this.tag = tag;
        this.tagCssStyle = tagCssStyle;
    }

    public void encodeBegin()
    {
        if (tag instanceof UIInput)
        {   // has local value?
            if (((UIInput)tag).isLocalValueSet())
            {   /* clear local value */
                if (log.isDebugEnabled())
                    log.debug("clearing local value for {}. value is {}.", getColumn().getName(), ((UIInput)tag).getLocalValue());
                ((UIInput)tag).setValue(null);
                ((UIInput)tag).setLocalValueSet(false);
            }
            // See if the Input is required 
            ValueExpression ve = findValueExpression("mandatory", true);
            if (ve!=null)
            {   Object req = ve.getValue(FacesContext.getCurrentInstance().getELContext());
                if (req!=null)
                    tagRequired = new Boolean(ObjectUtils.getBoolean(req));
            }    
        }
    }

    public InputControl getInputControl()
    {
        if (control != null)
        {   // Must check record!
            checkRecord();
            return control;
        }    
        // Create
        if (getColumn() == null)
        	throw new NotSupportedException(this, "getInputControl");
        // Get Control from column
        String controlType = getTagAttribute("controlType");
        if (controlType==null)
        {   controlType = column.getControlType();
            // Always use SelectInputControl
            if (TextInputControl.NAME.equalsIgnoreCase(controlType))
            {   Object attr = tag.getAttributes().get("options");
                if (attr != null && (attr instanceof Options) && !((Options)attr).isEmpty())
                    controlType = SelectInputControl.NAME;
            }
        }
        // find control type
        if (StringUtils.isNotEmpty(controlType))
            control = InputControlManager.getControl(controlType);
        if (control == null)
        {   // Auto-detect
            if (getValueOptions()!=null)
                controlType = SelectInputControl.NAME;
            else
            {   // get from data type
                DataType dataType = column.getDataType();
                controlType = FacesUtils.getFacesApplication().getDefaultControlType(dataType);
            }
            // get default control
            control = InputControlManager.getControl(controlType);
            // Still not? Use Text Control
            if (control == null)
                control = InputControlManager.getControl(TextInputControl.NAME);
            // debug
            if (log.isDebugEnabled() && !controlType.equals(TextInputControl.NAME))
                log.debug("Auto-detected field control for " + column.getName() + " is " + controlType);
        }
        // check record
        checkRecord();
        return control;
    }
    
    private void checkRecord()
    {
        // Record may change even for the same instance
        if (this.record== null)
            return;
        // Check direct record property
        Object rec = tag.getAttributes().get("record");
        if (rec!=null)
        {   // record directly specified
            if (rec!=this.record)
            {   // Record has changed
                if (log.isTraceEnabled())
                {   // Debug output
                    if ((rec instanceof DBRecord) && (this.record instanceof DBRecord))
                    {   // a database record change
                        String keyOld = StringUtils.toString(((DBRecord)this.record).getKeyValues());
                        String keyNew = StringUtils.toString(((DBRecord)rec).getKeyValues());
                        String rowSet = StringUtils.valueOf(((DBRecord)rec).getRowSet().getName());
                        log.trace("Changing "+tag.getClass().getSimpleName()+" record of rowset "+rowSet+" from {} to {}", keyOld, keyNew);
                    }
                    else
                    {   // probably a bean change
                        log.trace("Changing "+tag.getClass().getSimpleName()+" record of class "+rec.getClass().getSimpleName());
                    }
                }
                // change now
                setRecord(rec);
            }    
        }
        else 
        {   // Invalidate if not an instance of Record
            if (!(this.record instanceof Record))
                this.record = null;
        }
    }

    public InputControl.ValueInfo getValueInfo(FacesContext ctx)
    {
        return new ValueInfoImpl(getColumn(), getTextResolver(ctx));
    }

    public InputControl.InputInfo getInputInfo(FacesContext ctx)
    {
        return new InputInfoImpl(getColumn(), getTextResolver(ctx));
    }

    public boolean hasColumn()
    {
        if (column == null)
            column = findColumn();
        return (column != null);
    }

    public Column getColumn()
    {
        if (column == null)
            column = findColumn();
        if (column == null)
            throw new InvalidArgumentException("column", column);
        return column;
    }

    public void setColumn(Column column)
    {
        this.column = column;
    }

    public Object getRecord()
    {
        if (record == null)
            record = findRecord();
        return record;
    }

    public void setRecord(Object record)
    {
        this.record = record;
        this.mostRecentValue = null; 
    }

    public RecordTag getRecordComponent()
    {
        // already present?
        if (recordTag != null)
            return recordTag;
        // Check record
        if (record != null || tag.getAttributes().containsKey("record"))
            return null; // No record tag: Record has been specified!
        // walk upwards the parent component tree and return the first record component found (if any)
        UIComponent parent = tag;
        while ((parent = parent.getParent()) != null)
        {
            if (parent instanceof RecordTag)
            {
                recordTag = (RecordTag) parent;
                return recordTag;
            }
        }
        return null;
    }
    
    private boolean isDetectFieldChange()
    {
        Object v = this.getTagAttribute("detectFieldChange");
        if (v==null && recordTag != null)
            v = recordTag.getAttributes().get("detectFieldChange");
        return (v!=null ? ObjectUtils.getBoolean(v) : true);
    }

    public Object getDataValue(boolean evalExpression)
    {
        if (getRecord() != null)
        {   // value
            if (record instanceof RecordData)
            { // a record
                mostRecentValue = ((RecordData) record).getValue(getColumn());
                return mostRecentValue;
            }
            else
            { // a normal bean
                String prop = getColumn().getBeanPropertyName();
                return getBeanPropertyValue(record, prop);
            }
        }
        else
        {   // Get from tag
            if (evalExpression)
                return tag.getValue();
            else
            {   // return value or value expression
                Object value = tag.getLocalValue();
                if (value!=null && (tag instanceof UIInput) && !((UIInput)tag).isLocalValueSet())
                    value= null; /* should never come here! */
                if (value==null)
                    value = findValueExpression("value", false);
                
                // value = tag.getValue();
                return value;
            }
        }
    }

    public void setDataValue(Object value)
    {
        if (getRecord() != null)
        { // value
            if (record instanceof Record)
            {   /* special case
                if (value==null && getColumn().isRequired())
                {   // ((Record)record).isFieldRequired(column)==false
                    log.warn("Unable to set null for required field!");
                    return;
                }
                */
                if (isDetectFieldChange())
                {   // DetectFieldChange by comparing current and most recent value
                    Object currentValue = ((Record) record).getValue(getColumn());
                    if (!ObjectUtils.compareEqual(currentValue, mostRecentValue))
                    {   // Value has been changed by someone else!
                        log.info("Concurrent data change for "+getColumn().getName()+". Current Value is {}. Ignoring new value {}", currentValue, value);
                        return;
                    }
                }
                // a record
                ((Record) record).setValue(getColumn(), value);
                mostRecentValue = value;
            }
            else if (record instanceof RecordData)
            { // a record
                throw new PropertyReadOnlyException("record");
            }
            else
            { // a normal bean
                String prop = getColumn().getBeanPropertyName();
                setBeanPropertyValue(record, prop, value);
            }
        }
        else
        { // Get from tag
          // tag.setValue(value);
            ValueExpression ve = tag.getValueExpression("value");
            if (ve == null)
                throw new PropertyReadOnlyException("value");

            FacesContext ctx = FacesContext.getCurrentInstance();
            ve.setValue(ctx.getELContext(), value);
        }
    }

    public boolean isRecordReadOnly()
    {
        // Check tag
        if (!(tag instanceof UIInput))
            return true;
        // check attribute
        Object val = tag.getAttributes().get("readonly");
        if (val != null && ObjectUtils.getBoolean(val))
            return true;
        // Do we have a record?
        if (getRecord() instanceof RecordData)
        { // Only a RecordData?
            if (!(record instanceof Record) || ((Record) record).isReadOnly())
                return true;
        }
        else if (!hasValueExpression())
        { // No Value expression given
            return true;
        }
        // Check Component
        if (recordTag != null && recordTag.isReadOnly())
            return true;
        // column
        return false;
    }

    public boolean isVisible()
    {
        // reset record
        if (this.record!=null && (tag.getAttributes().get("record") instanceof Record))
            this.record=null;
        // Check Record
        if ((getRecord() instanceof Record))
        { // Ask Record
            Record r = (Record) record;
            return r.isFieldVisible(getColumn());
        }
        // column
        return true;
    }

    public boolean isReadOnly()
    {
        // check attribute
        Object val = tag.getAttributes().get("disabled");
        if (val != null && ObjectUtils.getBoolean(val))
            return true;
        // Check Record
        if (isRecordReadOnly())
            return true;
        // Check Record
        if ((getRecord() instanceof Record))
        { // Ask Record
            Record r = (Record) record;
            return r.isFieldReadOnly(getColumn());
        }
        // column
        return getColumn().isReadOnly();
    }

    public boolean isValueRequired()
    {
        // See if the tag is required
        if (tagRequired!=null)
            return tagRequired.booleanValue();
        // Check Read-Only first
        if (isReadOnly())
            return false;
        // Check Record
        if ((getRecord() instanceof Record))
        {   // Ask Record
            Record r = (Record) record;
            return r.isFieldRequired(getColumn());
        }
        // Check Value Attribute
        if (hasValueExpression())
            return false;
        // Required
        return getColumn().isRequired();
    }

    /* Helpers */
    protected Column findColumn()
    {
        // if parent is a record tag, get the record from there
        Object col = tag.getAttributes().get("column");
        if (col instanceof Column)
        { // cast to column
            return (Column) col;
        }
        if (col instanceof String)
        {   // parse String
            String name = String.valueOf(col);
            int dbix = name.indexOf('.');
            if (dbix<=0)
            {
                log.error("Invalid column expression '{}'!", name);
                return null; // not found
            }
            DBDatabase db = DBDatabase.findById(name.substring(0,dbix));
            if (db==null)
            {
                log.error("Database '{}' not found!", name.substring(0,dbix));
                return null; // not found
            }
            int co = name.lastIndexOf('.');
            int to = name.lastIndexOf('.', co - 1);
            String cn = name.substring(co + 1);
            String tn = name.substring(to + 1, co);
            DBRowSet rs = db.getRowSet(tn);
            if (rs == null)
            {
                log.error("Table/View '{}' not found in database!", tn);
                return null; // not found
            }
            Column column = rs.getColumn(cn);
            if (column == null)
            {
                log.error("Column '{}' not found in table/view '{}'!", cn, tn);
                return null; // not found
            }
            // done
            return column;
        }
        // When null, try value
        if (col == null)
        { // Try value
            col = tag.getValue();
            // Column supplied?
            if (col instanceof Column)
            {
                return (Column) col;
            }
            // Column expression supplied?
            if (col instanceof ColumnExpr)
            { // Use source column instead 
                Column source = ((ColumnExpr) col).getSourceColumn();
                if (source != null)
                    return source;
                // No source column? --> wrap 
                return new ColumnExprWrapper((ColumnExpr) col);
            }
        }
        // No column!
        if (log.isDebugEnabled() && !(tag instanceof LinkTag))
            log.warn("No Column provided for value tag!");
        return null;
    }

    protected Object findRecord()
    {
        Object rec = tag.getAttributes().get("record");

        if (rec == null && hasValueExpression())
        {   // See if the record is in value
            return null;
        }

        if (rec == null)
        { // if parent is a record tag, get the record from there
            RecordTag recordComponent = getRecordComponent();
            if (recordComponent != null)
            {
                rec = recordComponent.getRecord();
            }
            else
            {
                // not supplied;
            }
        }
        return rec;
    }
    
    protected boolean hasValueExpression()
    {
        // Find expression
        if (hasValueExpr != null)
            return hasValueExpr.booleanValue();
        // Find expression
        ValueExpression ve = findValueExpression("value", false);
        if (ve != null)
        {   // check
            if (log.isDebugEnabled())
            {
                FacesContext ctx = FacesContext.getCurrentInstance();
                boolean readOnly = ve.isReadOnly(ctx.getELContext());
                if (readOnly)
                    log.debug(tag.getClass().getSimpleName() + " for " + getColumn().getName() + " expression " + ve.getExpressionString()
                              + " is readOnly!");
                else
                    log.debug(tag.getClass().getSimpleName() + " for " + getColumn().getName() + " expression " + ve.getExpressionString()
                              + " is updateable!");
            }
        }
        /*
        else if (log.isDebugEnabled())
            log.debug(tag.getClass().getSimpleName()+" for "+getColumn().getName()+" has no value expression!");
        */
        // merken
        hasValueExpr = Boolean.valueOf(ve != null);
        return hasValueExpr.booleanValue();
    }
    
    private static final String CC_ATTR_EXPR = "#{cc.attrs.";

    protected ValueExpression findValueExpression(String attribute, boolean allowLiteral)
    {
        // Check for expression
        ValueExpression ve = tag.getValueExpression(attribute);
        if (ve == null)
            return null;
        // Find expression
        UIComponent parent = tag;
        String expr = ve.getExpressionString();
        while (expr.startsWith(CC_ATTR_EXPR))
        {
            // Unwrap
            if (ve instanceof TagValueExpression)
                ve = ((TagValueExpression)ve).getWrapped();
            // find parent
            if (ve instanceof ContextualCompositeValueExpression)
            {
                FacesContext ctx = FacesContext.getCurrentInstance();
                ContextualCompositeValueExpression ccve = (ContextualCompositeValueExpression)ve;
                CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(ctx);
                UIComponent cc = manager.findCompositeComponentUsingLocation(ctx, ccve.getLocation());
                // set Parent
                parent = cc;
            }
            else
            {   // find parent
                parent = UIComponent.getCompositeComponentParent(parent);
            }
            if (parent == null)
                return null;
            // check expression
            int end = expr.indexOf('}');
            String attrib = expr.substring(CC_ATTR_EXPR.length(), end);
            if (attrib.indexOf('.')>0)
                return ve; // do not investigate any further
            // find attribute
            ValueExpression next = parent.getValueExpression(attrib);
            if (next == null)
            {   // allow literal
                if (allowLiteral && (parent.getAttributes().get(attrib)!=null))
                    return ve;
                // not found
                return null;
            }
            // get new expression String
            ve = next;
            expr = ve.getExpressionString();
        }
        // found
        return ve;
    }
    
    protected Options getValueOptions()
    {
        // null value
        Object attr = tag.getAttributes().get("options");
        if (attr != null && (attr instanceof Options))
            return ((Options) attr);
        if (getColumn() != null)
        { // Do we have a record?
            if (getRecord() instanceof Record)
                return ((Record) record).getFieldOptions(column);
            // get From Column
            return column.getOptions();
        }
        // not available
        return null;
    }

    protected Object getBeanPropertyValue(Object bean, String property)
    {
        try
        { // Get Property Value
            PropertyUtilsBean pub = BeanUtilsBean.getInstance().getPropertyUtils();
            return pub.getSimpleProperty(bean, property);

        }
        catch (IllegalAccessException e)
        {
            log.error(bean.getClass().getName() + ": unable to get property '" + property + "'");
            throw new BeanPropertyGetException(bean, property, e);
        }
        catch (InvocationTargetException e)
        {
            log.error(bean.getClass().getName() + ": unable to get property '" + property + "'");
            throw new BeanPropertyGetException(bean, property, e);
        }
        catch (NoSuchMethodException e)
        {
            log.warn(bean.getClass().getName() + ": no getter available for property '" + property + "'");
            throw new BeanPropertyGetException(bean, property, e);
        }
    }

    protected void setBeanPropertyValue(Object bean, String property, Object value)
    {
        // Get Property Name
        try
        { // Get Property Value
            if (ObjectUtils.isEmpty(value))
                value = null;
            // Set Property Value
            if (value != null)
            { // Bean utils will convert if necessary
                BeanUtils.setProperty(bean, property, value);
            }
            else
            { // Don't convert, just set
                PropertyUtils.setProperty(bean, property, null);
            }
        }
        catch (IllegalArgumentException e)
        {
            log.error(bean.getClass().getName() + ": invalid argument for property '" + property + "'");
            throw new BeanPropertySetException(bean, property, e);
        }
        catch (IllegalAccessException e)
        {
            log.error(bean.getClass().getName() + ": unable to set property '" + property + "'");
            throw new BeanPropertySetException(bean, property, e);
        }
        catch (InvocationTargetException e)
        {
            log.error(bean.getClass().getName() + ": unable to set property '" + property + "'");
            throw new BeanPropertySetException(bean, property, e);
        }
        catch (NoSuchMethodException e)
        {
            log.error(bean.getClass().getName() + ": no setter available for property '" + property + "'");
            throw new BeanPropertySetException(bean, property, e);
        }
    }

    public String getValueTooltip(Object value)
    {
        if (value == null)
            return null;
        // is it a template?
        String templ = StringUtils.valueOf(value);
        int valIndex = templ.indexOf("{}");
        if (valIndex >= 0)
            value = getDataValue(true);
        // Check Options
        String text;
        Options options = getValueOptions();
        if (options != null && !hasFormat("notitlelookup"))
        { // Lookup the title
            String optValue = options.get(value);
            text = getDisplayText(optValue);
        }
        else
            text = getDisplayText(StringUtils.toString(value));
        // Check for template
        if (valIndex >= 0 && text!=null)
            text = StringUtils.replace(templ, "{}", text);
        return text;
    }

    public String getLabelTooltip(Column column)
    {
        Object title = tag.getAttributes().get("title");
        if (title != null)
            return getDisplayText(StringUtils.toString(title));
        // Check for short form
        if (hasFormat("short") && !ObjectUtils.isEmpty(column.getAttribute(COLATTR_ABBR_TITLE)))
            return getDisplayText(column.getTitle());
        // No Title
        return null;
    }

    public boolean hasFormat(String format)
    { // null value
        Object f = tag.getAttributes().get("format");
        return (f != null && String.valueOf(f).indexOf(format) >= 0);
    }

    public boolean hasFormat(InputControl.ValueInfo vi, String format)
    {
        String f = vi.getFormat();
        return (f != null && f.indexOf(format) >= 0);
    }

    public void writeAttribute(ResponseWriter writer, String attribute, Object value)
        throws IOException
    {
        if (value != null)
            writer.writeAttribute(attribute, value, null);
    }
    
    public String getDisplayText(String text)
    {
        if (textResolver==null)
            getTextResolver(FacesContext.getCurrentInstance());
        return textResolver.resolveText(text);
    }

    public TextResolver getTextResolver(FacesContext context)
    {
        if (textResolver==null)
            textResolver=((FacesApplication)context.getApplication()).getTextResolver(context);
        return textResolver;
    }
    
    public void addErrorMessage(FacesContext context, Exception e)
    {
        String msgText = getTextResolver(context).getExceptionMessage(e);
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msgText, msgText);
        context.addMessage(tag.getClientId(), msg);
    }
    
    public String getTagAttribute(String name, String defValue)
    {
        Object  v = tag.getAttributes().get(name);
        return (v!=null) ? StringUtils.toString(v) : defValue;
    }

    public String getTagAttribute(String name)
    {
        return getTagAttribute(name, null);
    }

    /* ********************** label ********************** */

    protected String getLabelValue(Column column, boolean colon)
    {
        String label = getTagAttribute("label");
        if (label==null)
        {   // Check for short form    
            if (hasFormat("short"))
            {
                label = StringUtils.toString(column.getAttribute(COLATTR_ABBR_TITLE));
                if (label==null)
                    log.warn("No Abbreviation available for column {}. Using normal title.", column.getName());
            }
            // Use normal title
            if (label==null)
                label=column.getTitle();
            // translate
            label = getDisplayText(label);
        }    
        // handle empty string
        if (StringUtils.isEmpty(label))
            return "";
        // getColon
        if (colon) 
            label = label.trim() + ":";
        // done
        return label;
    }
    
    public HtmlOutputLabel createLabelComponent(FacesContext context, String forInput, String styleClass, String style, boolean colon)
    {
        Column column = null;
        boolean readOnly=false;
        boolean required=false;
        // for
        if (StringUtils.isNotEmpty(forInput) && !forInput.equals("*"))
        {   // Set Label input Id
            UIComponent input = FacesUtils.getFacesApplication().findComponent(context, forInput, tag);
            if (input!=null && (input instanceof InputTag))
            {   // Check Read-Only
                InputTag inputTag = ((InputTag)input);
                column = inputTag.getInputColumn();
                readOnly = inputTag.isInputReadOnly();
                required = inputTag.isInputRequired();
            }
        }
        if (column==null)
        {   // Get from LinkTag
            column = getColumn();
            required = isValueRequired(); // does only check Attribute
        }
            
        // Check column
        if (column==null)
            throw new InvalidArgumentException("column", column);

        // create label now
        HtmlOutputLabel label;
        try {
            label = InputControlManager.getLabelComponentClass().newInstance();
        } catch (InstantiationException e1) {
            throw new InternalException(e1);
        } catch (IllegalAccessException e2) {
            throw new InternalException(e2);
        }
        
        // value
        String labelText = getLabelValue(column, colon);
        if (StringUtils.isEmpty(labelText))
            label.setRendered(false);
        else
            label.setValue(labelText);

        // styleClass
        if (StringUtils.isNotEmpty(styleClass))
            label.setStyleClass(styleClass);
        
        // for 
        if (StringUtils.isNotEmpty(forInput) && !readOnly)
        {   // Set Label input Id
            String inputId = getInputInfo(context).getInputId();
            if (forInput.equals("*"))
                label.setFor(inputId);
            else
                label.setFor(forInput+":"+inputId);
        }    

        // style
        if (StringUtils.isNotEmpty(style))
            label.setStyle(style);

        // title
        String title = getLabelTooltip(column);
        if (title!=null)
            label.setTitle(title);
        
        // required
        if (required)
        {
            HtmlPanelGroup span = new HtmlPanelGroup();
            span.setStyleClass("required");
            HtmlOutputText text = new HtmlOutputText();
            text.setValue("*");
            span.getChildren().add(text);
            label.getChildren().add(span);
        }

        return label;
    }
    
    /* ********************** CSS-generation ********************** */

    public static String getTagStyleClass(String tagCssStyle, String typeClass, String addlStyle, String userStyle)
    {
        // tag and type style class
        if (StringUtils.isEmpty(userStyle) && StringUtils.isEmpty(addlStyle))
            return StringUtils.isEmpty(typeClass) ? tagCssStyle : tagCssStyle + typeClass;
        // concatenate
        StringBuilder b = new StringBuilder(tagCssStyle);
        if (StringUtils.isNotEmpty(typeClass))
            b.append(typeClass);
        if (StringUtils.isNotEmpty(addlStyle))
        {
            b.append(" ");
            b.append(addlStyle);
        }
        if (StringUtils.isNotEmpty(userStyle))
        {
            b.append(" ");
            b.append(userStyle);
        }
        return b.toString();
    }

    public static final String CSS_DATA_TYPE_NONE     = "";
    public static final String CSS_DATA_TYPE_IDENT    = " eTypeIdent";
    public static final String CSS_DATA_TYPE_NUMBER   = " eTypeNumber";
    public static final String CSS_DATA_TYPE_TEXT     = " eTypeText";
    public static final String CSS_DATA_TYPE_LONGTEXT = " eTypeLongText";
    public static final String CSS_DATA_TYPE_DATE     = " eTypeDate";
    public static final String CSS_DATA_TYPE_BOOL     = " eTypeBool";

    private static String getDataTypeClass(DataType type)
    {
        switch (type)
        {
            case AUTOINC:
                return CSS_DATA_TYPE_IDENT;
            case INTEGER:
            case DECIMAL:
            case FLOAT:
                return CSS_DATA_TYPE_NUMBER;
            case TEXT:
            case CHAR:
                return CSS_DATA_TYPE_TEXT;
            case DATE:
            case DATETIME:
                return CSS_DATA_TYPE_DATE;
            case BOOL:
                return CSS_DATA_TYPE_BOOL;
            case CLOB:
                return CSS_DATA_TYPE_LONGTEXT;
            default:
                return CSS_DATA_TYPE_NONE; // Not provided
        }
    }

    public final String getTagStyleClass(DataType dataType, String addlStyle, String userStyle)
    {
        String typeClass = getDataTypeClass(dataType);
        return getTagStyleClass(tagCssStyle, typeClass, addlStyle, userStyle);
    }

    public final String getTagStyleClass(DataType dataType)
    {
        String userStyle = StringUtils.toString(tag.getAttributes().get("styleClass"));
        String typeClass = getDataTypeClass(dataType);
        return getTagStyleClass(tagCssStyle, typeClass, null, userStyle);
    }

    public final String getTagStyleClass(String addlStyle)
    {
        String userStyle = StringUtils.toString(tag.getAttributes().get("styleClass"));
        String typeClass = hasColumn() ? getDataTypeClass(column.getDataType()) : null;
        return getTagStyleClass(tagCssStyle, typeClass, addlStyle, userStyle);
    }

    public final String getTagStyleClass()
    {
        String userStyle = StringUtils.toString(tag.getAttributes().get("styleClass"));
        String typeClass = hasColumn() ? getDataTypeClass(column.getDataType()) : null;
        return getTagStyleClass(tagCssStyle, typeClass, null, userStyle);
    }

}

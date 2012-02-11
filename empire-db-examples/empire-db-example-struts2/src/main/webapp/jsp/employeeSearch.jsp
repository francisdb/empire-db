<!-- 
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
 --> 
<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.apache.empire.struts2.websample.db.*"%>
<%@ taglib prefix="e" uri="/empire-tags" %>
<jsp:useBean id="db" scope="application" type="org.apache.empire.struts2.websample.db.SampleDB"/>
<jsp:useBean id="action" scope="request" type="org.apache.empire.struts2.websample.web.actions.EmployeeListAction"/>
<% 
	SampleDB.Employees EMP = db.T_EMPLOYEES;
%>
<html>
<head>
    <link href="css/main.css" rel="stylesheet" type="text/css"/>
    <title><e:text value="!application.title"/></title>
</head>
<body>
<div class="titleDiv"><e:text value="!application.title"/></div>
<h1><e:text value="!page.label.search"/></h1>
<e:actionerrors />
<e:actionmessage />
<e:form id="searchForm" action="employeeList!doQuery">
    <e:control property="searchInfo.employeeId"	 	column="<%= EMP.C_EMPLOYEE_ID %>"></e:control>
    <e:control property="searchInfo.firstName" 		column="<%= EMP.C_FIRSTNAME %>" />
    <e:control property="searchInfo.lastName"  		column="<%= EMP.C_LASTNAME %>" />
    <e:control property="searchInfo.departmentId"  	column="<%= EMP.C_DEPARTMENT_ID %>" options="<%= action.getDepartments() %>" format="allownull" />
    <!-- Original Struts code -->
    <!-- s:textfield name="searchInfo.firstName" value="%{searchInfo.firstName}" label="%{getText('label.firstName')}" size="40"/ -->
    <!-- s:textfield name="searchInfo.lastName" value="%{searchInfo.lastName}" label="%{getText('label.lastName')}" size="40"/ -->
    <!-- s:select name="searchInfo.departmentId" value="%{searchInfo.departmentId}" list="departments" listKey="key" listValue="value"/ -->
    <e:submit text="!button.label.search" embed="true"/>
</e:form>
<p><e:text value="!page.label.logininfo"/>&nbsp;'<e:text value="%{session.user.userId}"/>'</p>
<e:link action="login!doLogout" text="!link.label.logout"/>
</body>
</html>

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
package org.apache.empire.db.codegen;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.empire.db.codegen.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class CodeGenAppTest {
	
	@Before
	public void cleanup() throws IOException{
		File generated = new File("target/generated");
		if(generated.exists()){
			boolean deleted = FileUtils.deleteDirectory(generated);
			if(!deleted){
				throw new IOException("Could not delete previously generated sources");
			}
		}
	}

	@Test
	public void testMain() {
		String[] args = new String[]{"src/test/resources/testconfig.xml"};
		
		// execute app
		CodeGenerator.main(args);
		
		// expected files
		File expected = new File("target/generated/dbsample/org/apache/empire/db/samples/dbsample/SampleDB.java");
		assertTrue("missing generated code", expected.exists());
	}
	
	@Test
	public void testMainDefaultTemplateFolderNested() {
		String[] args = new String[]{"src/test/resources/testconfig_default_template_folder_nested.xml"};
		CodeGenerator.main(args);
		File expectedFile = new File("target/generated/dbsample/org/apache/empire/db/samples/dbsample/SampleDB.java");
		
		// the string to be expected within the DB class file
		String expectedContent = "addColumn(";
		
		
		// this variable will hold the file's whole content
		String fileContent="";
		try{
			FileInputStream fis = new FileInputStream(expectedFile);
			byte[] buff=new byte[fis.available()];
			fis.read(buff);
			fileContent=new String(buff);
			fis.close();
		}
		// IO Exception can be ignored here
		catch(IOException e){}
		
		// we expect that there is at least one nested table is within the DB class that has at leas one column.
		assertTrue("missing generated code for nested table classes", fileContent.contains(expectedContent));
	}
	
	@Test
	public void testMainUsingTemplateFolder() {
		String[] args = new String[]{"src/test/resources/testconfig_using_template_folder.xml"};
		
		// execute app
		CodeGenerator.main(args);
		
		// expected files
		File expected = new File("target/generated/dbsample/org/apache/empire/db/samples/dbsample/SampleDB.java");
		assertTrue("missing generated code", expected.exists());
	}
	
	@Test
	public void testMainFailInvalidTemplateFolder() {
		String[] args = new String[]{"src/test/resources/testconfig_invalid_template_folder.xml"};
		try{
			CodeGenerator.main(args);
			fail("This should fail as the template path is missing");
		}catch(RuntimeException ex){
			assertTrue("Wrong message", ex.getMessage().startsWith("Provided template folder missing or not readable:"));
		}
	}

}

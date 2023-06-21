package com.igsl.configmigration.workflow.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.google.common.io.Files;
import com.igsl.configmigration.workflow.mapper.generated.Arg;
import com.igsl.configmigration.workflow.mapper.generated.Workflow;

public class Test {
	
	private static final String XML_HEADER = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">\r\n";
	
	private static final String XML_SOURCE = "C:\\Users\\IGS\\Desktop\\Test\\Approval.xml";
	
	public static String constructReplacement(String replacement, int groupCount, Map<Integer, String> replacementData) {
		String result = replacement;
		for (int i = 1; i <= groupCount; i++) {
			if (replacementData.containsKey(i)) {
				result = result.replaceAll("\\$" + i, replacementData.get(i));
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		List<String> workflowXML = Files.readLines(Paths.get(XML_SOURCE).toFile(), Charset.defaultCharset());
		StringBuilder xml = new StringBuilder();
		for (String s : workflowXML) {
			xml.append(s);
		}
		
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		// Disable DTD validation
		try {
			saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
			System.out.println("Error configuring SAX Parser");
			e.printStackTrace();
		}
		Source xmlSource = new SAXSource(saxParserFactory.newSAXParser().getXMLReader(),
                new InputSource(new StringReader(xml.toString())));
		JAXBContext ctx = JAXBContext.newInstance(Workflow.class);
		Unmarshaller parser = ctx.createUnmarshaller();
		Workflow wf = (Workflow) parser.unmarshal(xmlSource);
		
		Marshaller marshaller = ctx.createMarshaller();
		// Set XML header
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		sw.write(XML_HEADER);
		marshaller.marshal(wf, sw);
		System.out.println(sw.toString());
		
		// Test JXPath
		String xpath = "//arg[name='class.name'][value='com.atlassian.jira.workflow.condition.InGroupCFCondition']/../arg[name='groupcf']";
		JXPathContext context = JXPathContext.newContext(wf);
		Iterator<Pointer> it = (Iterator<Pointer>) context.iteratePointers(xpath);
		//Iterator<?> it = context.iterate(xpath);
		if (it != null) {
			if (it.hasNext()) {
				while (it.hasNext()) {
					Pointer p = it.next();
					WorkflowPart v = (WorkflowPart) p.getValue();
					System.out.println(p.asPath() + ": " + v + " (" + v.getWorkflowPartType() + ")");
				}
			} else {
				System.out.println("Not found");
			}
		} else {
			System.out.println(xpath + ": null");
		}
		
//		{
//			String s = "xx1Axx";
//			String regex = "([0-9]+)([A-Z]+)";
//			String replacement = "$1$2$2";
//			System.out.println(s);
//			StringBuffer sb = new StringBuffer();
//			Pattern p = Pattern.compile(regex);
//			Matcher m = p.matcher(s);
//			while (m.find()) {
//				System.out.println("Group Cnt: " + m.groupCount());
//				System.out.println("Group 0: " + m.group(0));
//				System.out.println("Group 1: " + m.group(1));
//				System.out.println("Group 2: " + m.group(2));
//				Map<Integer, String> replacementData = new HashMap<>();
//				replacementData.put(1, Integer.toString(Integer.parseInt(m.group(1)) * 10));
//				replacementData.put(2, m.group(2).toLowerCase());
//				String replace = constructReplacement(replacement, m.groupCount(), replacementData);
//				System.out.println("Replace: " + replace);
//				m.appendReplacement(sb, replace);
//			}
//			m.appendTail(sb);
//			System.out.println(sb.toString());
//		}
		
	}
}

package com.igsl.configmigration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class to print an object getters in generic way.
 */
public class GenericPrint {
	private static final ObjectMapper OM = new ObjectMapper();	
	private static final String NEWLINE = "\n";
	private static final String INDENT = "\t";
	private static String getIndent(int level) {
		StringBuilder indent = new StringBuilder(""); 
		for (int i = 0; i < level; i++) {
			indent.append(INDENT);
		}
		return indent.toString();
	}
	public static String genericPrint(String title, Object o) {
		return genericPrint(0, title, o);
	}
	public static String genericPrint(int level, String title, Map<?, ?> m) {
		String indent = getIndent(level);
		StringBuilder sb = new StringBuilder();
		if (title != null) {
			sb.append(indent).append(title).append(" (Map): ").append(NEWLINE);
		}
		indent = getIndent(level + 1);
		for (Map.Entry<?, ?> o : m.entrySet()) {
			sb.append(indent).append("#").append(o.getKey()).append(": ").append(NEWLINE);
			sb.append(genericPrint(level + 1, null, o.getValue()));
		}
		return sb.toString();
	}
	public static String genericPrint(int level, String title, Collection<?> c) {
		String indent = getIndent(level);
		StringBuilder sb = new StringBuilder();
		if (title != null) {
			sb.append(indent).append(title).append(" (Collection): ").append(NEWLINE);
		}
		indent = getIndent(level + 1);
		int count = 0;
		for (Object o : c) {
			sb.append(indent).append("#").append(count).append(": ").append(NEWLINE);
			sb.append(genericPrint(level + 1, null, o));
			count++;
		}
		return sb.toString();
	}
	public static String genericPrint(int level, String title, Object o) {
		String indent = getIndent(level);
		StringBuilder sb = new StringBuilder();
		if (title != null) {
			sb.append(indent).append(title).append((o != null)? " (" + o.getClass().getCanonicalName() + ")" : "").append(": ").append(NEWLINE);
		}
		indent = getIndent(level + 1);
		if (o == null) {
			sb.append(indent).append("null").append(NEWLINE);
		} else {
			sb.append(indent).append("hashCode(): [").append(o.hashCode()).append("]").append(NEWLINE);
			sb.append(indent).append("Class: [").append(o.getClass().getCanonicalName()).append("]").append(NEWLINE);
			for (Method m : o.getClass().getMethods()) {
				Class<?> rt = m.getReturnType();
				if (Modifier.isPublic(m.getModifiers()) && 						
					(
						m.getName().startsWith("get") || 
						m.getName().startsWith("is")
					) && 
					!m.getName().equals("getClass") && 
					m.getParameterCount() == 0) {
					sb.append(indent).append(m.getName()).append("(): ");
					Object v = null;
					try {
						v = m.invoke(o);
						if (v == null) {
							sb.append("[null]");
						} else {
							sb.append("[").append(v.toString()).append("]");
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						sb.append(e.getClass().getCanonicalName()).append(": ").append(e.getMessage());
					}
					sb.append(NEWLINE);
					if (v != null && level < 3) {
						if (Collection.class.isAssignableFrom(v.getClass())) {
							sb.append(genericPrint(level + 1, null, (Collection<?>) v));
						} else if (Map.class.isAssignableFrom(v.getClass())) {
							sb.append(genericPrint(level + 1, null, (Map<?, ?>) v));
						} else {
							sb.append(genericPrint(level + 1, null, v));
						}
					}
				}
			}
		}
		return sb.toString();
	}
}

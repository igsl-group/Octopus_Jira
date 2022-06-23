package com.igsl.customfieldtypes.productpackingnote;

import java.util.Comparator;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.google.gson.Gson;

public class ProductPackingNoteData implements Comparable<ProductPackingNoteData> {

	private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo); 

	private String name;
	private String version;
	private String md5Signature;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMd5Signature() {
		return md5Signature;
	}

	public void setMd5Signature(String md5Signature) {
		this.md5Signature = md5Signature;
	}

	@Override
	public int compareTo(ProductPackingNoteData o) {
		if (o == null) {
			return 1;
		}
		return 	nullSafeStringComparator.compare(this.name, o.name) | 
				nullSafeStringComparator.compare(this.version, o.version) | 
				nullSafeStringComparator.compare(this.md5Signature, o.md5Signature);
	}

	public static ProductPackingNoteData fromString(String s) throws FieldValidationException {
		if (s == null || s.isEmpty()) {
			return null;
		}
		try {
			ProductPackingNoteData data = new Gson().fromJson(s, ProductPackingNoteData.class);
			return data;
		} catch (Exception ex) {
			throw new FieldValidationException(ex.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
	public String toReadableString() {
		return this.name + " (" + version + "): " + md5Signature;
	}
	
}

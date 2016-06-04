package com.browser.engine.db;

import java.io.Serializable;

public class BMetadata implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2818688765725382613L;

	private String columnName;
	private String value;
	private int length;
	private BDataType dataType;
	private boolean required;
	private String filter;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public BDataType getDataType() {
		return dataType;
	}

	public void setDataType(BDataType dataType) {
		this.dataType = dataType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}

}

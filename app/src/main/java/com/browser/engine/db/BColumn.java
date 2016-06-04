package com.browser.engine.db;

import java.io.Serializable;

public class BColumn implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3065917675719275508L;
	private String name;
	private int index;
	private int color;
	private String value;
	private String showValue;
	private String prevValue;
	private boolean extendedData;

	public BColumn() {
	}
	
	public BColumn(String name) {
		this.name=name;
	}

	public BColumn(String name, int index, int color, String showValue,
			String value,boolean extendedData) {
		
		this.name = name;
		this.showValue = showValue;
		this.index = index;
		this.color = color;
		this.value = value;
		this.extendedData=extendedData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public void setShowValue(String showVale) {
		this.showValue = showVale;
	}

	public String getShowValue() {
		return showValue;
	}
	
	public void setPrevValue(String prevValue) {
		this.prevValue = prevValue;
	}

	public String getPrevValue() {
		return prevValue;
	}
	
	public void setNewData(String newValue){
		this.prevValue = this.getValue();
		this.setValue(newValue);
	}
	
	public void renderColumnData(String newValue){
		setNewData(newValue);
		
		if (newValue.length() * 8 >= 240) {
			int sb = newValue.length()>237?237:newValue.length();
			this.setShowValue(newValue.substring(0, sb) + "...");
		} else {
			this.setShowValue(newValue);
		}
	}

	public void setExtendedData(boolean extendedData) {
		this.extendedData = extendedData;
	}

	public boolean isExtendedData() {
		return extendedData;
	}



}

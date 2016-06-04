package com.browser.engine.db;

public enum BDataType {

	Null(0), Integer(1), Real(2), Text(3), Blob(4), Numeric(5);

	int type;

	BDataType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}

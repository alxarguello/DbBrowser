package com.browser.engine.ui;

import com.browser.engine.db.BMetadata;

import android.content.Context;
import android.util.AttributeSet;

public class CBTextView extends CTextView {

	private BMetadata columnMetadata = null;
	private String extendedData=null;

	public CBTextView(Context context) {
		this(context,false);
	}

	public CBTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CBTextView(Context context,boolean createMetadata) {
		super(context);
		this.columnMetadata = new BMetadata();
	}


	public BMetadata getColumnMetadata() {
		return columnMetadata;
	}

	public void setExtendedData(String extendedData) {
		this.extendedData = extendedData;
	}

	public String getExtendedData() {
		return extendedData;
	}


}

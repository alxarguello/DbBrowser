package com.browser.engine.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class CButton extends Button{

	private String customFont = null;
	private static final Logger logger = Logger.getLogger(CButton.class.getName());
	
	public CButton(Context context) {
		super(context);
	}

	public CButton(Context context, AttributeSet attrs) {
		super(context);
		for (int a = 0; a < attrs.getAttributeCount(); a++) {
			String atr = attrs.getAttributeName(a);
			if (atr != null) {
				if (atr.equals("customFont")) {
					customFont = attrs.getAttributeValue(a);
				}
			}
		}
		try {
			if (customFont != null && customFont.equals("") == false) {
				this.setTypeface(Typeface.createFromAsset(getContext().getAssets(), String.format("fonts/%s.ttf", customFont)));
			}
			this.setTextSize(15);
		} catch (RuntimeException e) {
			logger.log(Level.WARNING, "Font not loaded: " + e);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Font not loaded: " + e);
		}
	}
	
	public CButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public void setCustomFont(String customFont) {
		this.customFont = customFont;
	}

	public String getCustomFont() {
		return customFont;
	}
	

}

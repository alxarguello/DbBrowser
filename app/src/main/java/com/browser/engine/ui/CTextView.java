package com.browser.engine.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.browser.app.R;
import com.browser.app.dao.BrowserDao;

import com.browser.app.util.Util;

import android.content.Context;

import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CTextView extends TextView {

	private String customFont = null;
	private boolean colored = false;
	private static final Logger logger = Logger.getLogger(CTextView.class.getName());

	public CTextView(Context context) {
		super(context);
	}

	public CTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		customFont = "Roboto-Bold";
		for (int a = 0; a < attrs.getAttributeCount(); a++) {
			String atr = attrs.getAttributeName(a);
			if (atr != null) {
				if (atr.equals("customFont")) {
					customFont = attrs.getAttributeValue(a);
				}
				if (atr.equals("colored")) {
					colored = Util.stringToBoolean(attrs.getAttributeValue(a));
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

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (colored) {
			if (text != null && ((String) text).endsWith(BrowserDao.C_ACTIVE)) {
				this.setTextColor(getResources().getColor(R.color.blueActive));
			}
		}
	}

	public CTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setCustomFont(String customFont) {
		this.customFont = customFont;
	}

	public String getCustomFont() {
		return customFont;
	}

}

package com.browser.engine.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

public class ParallelScrollView extends HorizontalScrollView {

	private View parallelView;

	public ParallelScrollView(Context context) {
		super(context);
	}

	public ParallelScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (parallelView != null) {
			parallelView.scrollTo(l, oldl);
		}
	}
	
	public void linkViews(View parallelView){
		this.parallelView = parallelView;
		if(parallelView instanceof  ParallelScrollView){
			((ParallelScrollView) parallelView).setParallelView(this);
		}
	}

	public View getParallelView() {
		return parallelView;
	}

	public void setParallelView(View parallelView) {
		this.parallelView = parallelView;
	}

}

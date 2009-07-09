/*
	ConnectBot: simple, powerful, open-source SSH client for Android
	Copyright (C) 2007-2008 Kenny Root, Jeffrey Sharkey

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.connectbot;

import java.util.Arrays;
import java.util.List;

import org.connectbot.bean.HostBean;
import org.connectbot.util.HostDatabase;
import org.connectbot.util.UberColorPickerDialog;
import org.connectbot.util.UberColorPickerDialog.OnColorChangedListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Kenny Root
 *
 */
public class ColorsActivity extends Activity implements OnItemClickListener, OnColorChangedListener, OnItemSelectedListener {
	private GridView mColorGrid;
	private Spinner mFgSpinner;
	private Spinner mBgSpinner;

	private HostBean mHost;

	private List<Integer> mColorList;
	private HostDatabase hostdb;

	private int mCurrentColor = 0;

	private int[] mDefaultColors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.act_colors);

		this.setTitle(String.format("%s: %s",
				getResources().getText(R.string.app_name),
				getResources().getText(R.string.title_colors)));

		mHost = null;

		hostdb = new HostDatabase(this);

		mColorList = Arrays.asList(hostdb.getColorsForHost(mHost));
		mDefaultColors = hostdb.getDefaultColorsForHost(mHost);

		mColorGrid = (GridView) findViewById(R.id.color_grid);
		mColorGrid.setAdapter(new ColorsAdapter(true));
		mColorGrid.setOnItemClickListener(this);

		mFgSpinner = (Spinner) findViewById(R.id.fg);
		mFgSpinner.setAdapter(new ColorsAdapter(false));
		mFgSpinner.setSelection(mDefaultColors[0]);
		mFgSpinner.setOnItemSelectedListener(this);

		mBgSpinner = (Spinner) findViewById(R.id.bg);
		mBgSpinner.setAdapter(new ColorsAdapter(false));
		mBgSpinner.setSelection(mDefaultColors[1]);
		mBgSpinner.setOnItemSelectedListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (hostdb != null) {
			hostdb.close();
			hostdb = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (hostdb == null)
			hostdb = new HostDatabase(this);
	}

	private class ColorsAdapter extends BaseAdapter {
		private boolean mSquareViews;

		public ColorsAdapter(boolean squareViews) {
			mSquareViews = squareViews;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ColorView c;

			if (convertView == null) {
				c = new ColorView(ColorsActivity.this, mSquareViews);
			} else {
				c = (ColorView) convertView;
			}

			c.setColor(mColorList.get(position));
			c.setNumber(position + 1);

			return c;
		}

		public int getCount() {
			return mColorList.size();
		}

		public Object getItem(int position) {
			return mColorList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}

	private class ColorView extends View {
		private boolean mSquare;

		private Paint mTextPaint;
		private Paint mShadowPaint;

		// Things we paint
		private int mBackgroundColor;
		private String mText;

		private int mAscent;
		private int mWidthCenter;
		private int mHeightCenter;

		public ColorView(Context context, boolean square) {
			super(context);

			mSquare = square;

			mTextPaint = new Paint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setTextSize(16);
			mTextPaint.setColor(0xFFFFFFFF);
			mTextPaint.setTextAlign(Paint.Align.CENTER);

			mShadowPaint = new Paint(mTextPaint);
			mShadowPaint.setStyle(Paint.Style.STROKE);
			mShadowPaint.setStrokeCap(Paint.Cap.ROUND);
			mShadowPaint.setStrokeJoin(Paint.Join.ROUND);
			mShadowPaint.setStrokeWidth(4f);
			mShadowPaint.setColor(0xFF000000);

			setPadding(10, 10, 10, 10);
		}

		public void setColor(int color) {
			mBackgroundColor = color;
		}

		public void setNumber(int number) {
			mText = Integer.toString(number);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int width = measureWidth(widthMeasureSpec);

			int height;
			if (mSquare)
				height = width;
			else
				height = measureHeight(heightMeasureSpec);

			mAscent = (int) mTextPaint.ascent();
			mWidthCenter = width / 2;
			mHeightCenter = height / 2 - mAscent / 2;

			setMeasuredDimension(width, height);
		}

		private int measureWidth(int measureSpec) {
			int result = 0;
			int specMode = MeasureSpec.getMode(measureSpec);
			int specSize = MeasureSpec.getSize(measureSpec);

			if (specMode == MeasureSpec.EXACTLY) {
				// We were told how big to be
				result = specSize;
			} else {
				// Measure the text
				result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
						+ getPaddingRight();
				if (specMode == MeasureSpec.AT_MOST) {
					// Respect AT_MOST value if that was what is called for by
					// measureSpec
					result = Math.min(result, specSize);
				}
			}

			return result;
		}

		private int measureHeight(int measureSpec) {
			int result = 0;
			int specMode = MeasureSpec.getMode(measureSpec);
			int specSize = MeasureSpec.getSize(measureSpec);

			mAscent = (int) mTextPaint.ascent();
			if (specMode == MeasureSpec.EXACTLY) {
				// We were told how big to be
				result = specSize;
			} else {
				// Measure the text (beware: ascent is a negative number)
				result = (int) (-mAscent + mTextPaint.descent())
						+ getPaddingTop() + getPaddingBottom();
				if (specMode == MeasureSpec.AT_MOST) {
					// Respect AT_MOST value if that was what is called for by
					// measureSpec
					result = Math.min(result, specSize);
				}
			}
			return result;
		}


		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.drawColor(mBackgroundColor);

			canvas.drawText(mText, mWidthCenter, mHeightCenter, mShadowPaint);
			canvas.drawText(mText, mWidthCenter, mHeightCenter, mTextPaint);
		}
	}

	private void editColor(int colorNumber) {
		mCurrentColor = colorNumber;
		new UberColorPickerDialog(this, this, mColorList.get(colorNumber)).show();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		editColor(position);
	}

	public void onNothingSelected(AdapterView<?> arg0) { }

	public void colorChanged(int value) {
		hostdb.setGlobalColor(mCurrentColor, value);
		mColorList.set(mCurrentColor, value);
		mColorGrid.invalidateViews();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		boolean needUpdate = false;
		if (parent == mFgSpinner) {
			if (position != mDefaultColors[0]) {
				mDefaultColors[0] = position;
				needUpdate = true;
			}
		} else if (parent == mBgSpinner) {
			if (position != mDefaultColors[1]) {
				mDefaultColors[1] = position;
				needUpdate = true;
			}
		}

		if (needUpdate)
			hostdb.setDefaultColorsForHost(mHost, mDefaultColors[0], mDefaultColors[1]);
	}
}
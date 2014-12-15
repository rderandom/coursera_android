package com.example.momatest3;

import java.math.BigDecimal;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	Random randomColor = new Random();
	int colorBlue;
	int colorRed;
	int colorYellow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		colorBlue = Color.parseColor(getString(R.string.hex_blue));
		colorRed = Color.parseColor(getString(R.string.hex_red));
		colorYellow = Color.parseColor(getString(R.string.hex_yellow));

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				repaintColors(progress);
			}
		});
	}

	/**
	 * Change background color of painted squares.
	 * 
	 * @param progress
	 * 
	 */
	private void repaintColors(int progress) {
		int newColor = modifyColor(colorRed, progress);
		getTextViewAndSetColor(R.id.textView1, newColor);
		getTextViewAndSetColor(R.id.textView2, newColor);
		getTextViewAndSetColor(R.id.textView3, newColor);

		newColor = modifyColor(colorBlue, progress);
		getTextViewAndSetColor(R.id.textView13, newColor);
		getTextViewAndSetColor(R.id.textView14, newColor);

		newColor = modifyColor(colorYellow, progress);
		getTextViewAndSetColor(R.id.textView20, newColor);
	}

	/**
	 * Modify a rgb color gradually 
	 * @param rgb
	 * @param progress
	 * @return
	 */
	private int modifyColor(int rgb, int progress) {
		java.math.BigDecimal amount = new BigDecimal(progress);
		amount = amount.divide(BigDecimal.TEN);

		float[] hsv = new float[3];
		Color.colorToHSV(rgb, hsv);

		int red = (rgb >> 16) & 0xFF;
		int green = (rgb >> 8) & 0xFF;
		int blue = rgb & 0xFF;

		float factor;
		if (progress > 5) {
			factor = amount.floatValue();
		} else {
			factor = amount.floatValue() * -1;
		}

		return Color.argb(255, (int) Math.min(255, red + 255 * factor),
				(int) Math.min(255, green + 255 * factor),
				(int) Math.min(255, blue + 255 * factor));
	}

	/**
	 * Calls findViewById on de R.id given and sets the background color passed
	 * by parameter.
	 * 
	 * @param textview1
	 * @param newColor
	 * @param color
	 */
	private void getTextViewAndSetColor(int textview1, int newColor) {
		TextView upper = (TextView) findViewById(textview1);
		upper.setBackgroundColor(newColor);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.more) {
			// Show dialog
			new AlertDialog.Builder(this)
					.setMessage(getString(R.string.inspiredby))
					.setPositiveButton(getString(R.string.visitmoma),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String url = "http://www.moma.org";
									Intent intentBrowser = new Intent(
											Intent.ACTION_VIEW);
									intentBrowser.setData(Uri.parse(url));
									startActivity(intentBrowser);
								}
							})
					.setNegativeButton(getString(R.string.notnow),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		}
		return super.onOptionsItemSelected(item);
	}
}

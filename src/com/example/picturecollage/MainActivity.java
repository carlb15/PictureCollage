package com.example.picturecollage;

import java.io.IOException;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements SurfaceTextureListener {
	private ScrollView mScrollView;
	private TextureView mTextureView;
	private ImageView mImageView;
	private ArrayList<ImageView> mImageViewArray;
	private int mId;
	private Camera mCamera;
	private RelativeLayout mRelativeLayout;
	private RelativeLayout.LayoutParams mParams;
	private int mHeight = 200;
	private int mWidth = 240;
	private float mPrevTouchX, mDX, mPosX, mPrevX;
	private float mPrevTouchY, mDY, mPosY, mPrevY;
	private int row = 1;
	private int column = 1;
	private String collageName = "CollageName";
	private int value = 1;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Create the starting views.
		createStartingViewsAndLayouts();
		// Setup on the onTouch listener for the TextureView.
		startTextureViewOnTouchListener();
	}

	private void startTextureViewOnTouchListener() {
		mTextureView.setOnTouchListener(new TextureView.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent me) {
				if (me.getAction() == MotionEvent.ACTION_DOWN) {

					addNewImageView();

					// Add the image view and remove the texture view.
					mRelativeLayout.removeView(mTextureView);
					mRelativeLayout.addView(mImageView, mParams);

					if (mImageViewArray.size() < 5) {
						enableDragFeatureForImageView(mImageView);
					}
					else if (mImageViewArray.size() == 5) {
						disableDragFeatureAndRealignImagViews();
					}

					// Set new layout for new texture view
					mParams = new RelativeLayout.LayoutParams(mParams.width + mWidth,
							mHeight);

					// Align with new image view.
					if (column != 5) {
						mParams.addRule(RelativeLayout.RIGHT_OF,
								mImageViewArray.get(mId - 1).getId());
						mParams.addRule(RelativeLayout.ALIGN_TOP,
								mImageViewArray.get(mId - 1).getId());
						mParams.addRule(RelativeLayout.ALIGN_BOTTOM,
								mImageViewArray.get(mId - 1).getId());
					}
					else {
						mParams.addRule(RelativeLayout.BELOW, mImageViewArray.get(mId - 3)
								.getId());
					}

					// Add the texture view and configure its orientation.
					mRelativeLayout.addView(mTextureView, mParams);
					mTextureView.setAlpha(1.0f);
					mTextureView.setRotation(0f);

					// Increase ID count for next image.
					mId++;
					mScrollView.removeAllViews();
					mScrollView = new ScrollView(getApplicationContext());
					mScrollView.addView(mRelativeLayout);
					setContentView(mScrollView);
				}

				return true;
			}

		});
	}

	private void createStartingViewsAndLayouts() {
		mId = 1;
		column = 1;
		row = 1;
		mImageViewArray = new ArrayList<ImageView>();
		mTextureView = new TextureView(this);
		mTextureView.setSurfaceTextureListener(this);
		mTextureView.setAlpha(1.0f);
		mTextureView.setRotation(0f);

		mParams = new RelativeLayout.LayoutParams(mWidth, mHeight);

		mRelativeLayout = new RelativeLayout(this);
		mRelativeLayout.addView(mTextureView, mParams);

		mScrollView = new ScrollView(this);
		mScrollView.addView(mRelativeLayout);
		setContentView(mScrollView);
	}

	private Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) {

		int w = 0, h = 0;
		for (int i = 0; i < bitmap.size(); i++) {
			if (i < bitmap.size() - 1) {
				w = bitmap.get(i).getWidth() > bitmap.get(i + 1).getWidth() ? bitmap
						.get(i).getWidth() : bitmap.get(i + 1).getWidth();
			}
			h += bitmap.get(i).getHeight();
		}
		Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(temp);
		int top = 0;
		for (int i = 0; i < bitmap.size(); i++) {
			Log.w("this", "Combine: " + i + "/" + bitmap.size() + 1);
			top = (i == 0 ? 0 : top + bitmap.get(i).getHeight());
			canvas.drawBitmap(bitmap.get(i), 0f, top, null);
		}
		return temp;
	}

	private void enableDragFeatureForImageView(ImageView imgView) {
		imgView.setOnTouchListener(new TextureView.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent me) {
				if (me.getAction() == MotionEvent.ACTION_DOWN) {
					mPrevTouchX = me.getX();
					mPrevTouchY = me.getY();
				}
				else if (me.getAction() == MotionEvent.ACTION_MOVE) {
					mDX = me.getX() - mPrevTouchX;
					mDY = me.getY() - mPrevTouchY;

					mPosX = mPrevX + mDX;
					mPosY = mPrevY + mDY;

					// Only if there's a significant difference do we
					// update it
					if (mDX > 4 || mDY < 4 || mDX < 4 && mDY > 4) {

						RelativeLayout.LayoutParams newLayout = new RelativeLayout.LayoutParams(
								mWidth, mHeight);

						newLayout.setMargins((int) mPosX, (int) mPosY, 0, 0);

						mPrevX = mPosX;
						mPrevY = mPosY;
						v.setLayoutParams(newLayout);
					}
				}

				return true;
			}

		});
	}

	private void disableDragFeatureAndRealignImagViews() {

		column = 1;
		row = 1;

		// Disable drag feature for each image view.
		for (int i = 0; i < mImageViewArray.size(); i++) {
			mImageViewArray.get(i).setOnTouchListener(null);
			mRelativeLayout.removeView(mImageViewArray.get(i));
			reconfigureImageViews(mImageViewArray.get(i), i);
			mRelativeLayout.addView(mImageViewArray.get(i), mParams);
		}
	}

	private void reconfigureImageViews(ImageView img, int prevImageIndex) {
		mParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		if (prevImageIndex == 0) {
			column++;
		}
		else if (column < 5 && row == 1) {
			mParams.addRule(RelativeLayout.RIGHT_OF,
					mImageViewArray.get(prevImageIndex - 1).getId());
			column++;
		}
		else if (column < 5 && row > 1) {
			mParams.addRule(RelativeLayout.RIGHT_OF,
					mImageViewArray.get(prevImageIndex - 1).getId());
			mParams.addRule(RelativeLayout.BELOW,
					mImageViewArray.get(prevImageIndex - 4).getId());
			column++;
		}
		else {
			mParams.addRule(RelativeLayout.BELOW,
					mImageViewArray.get(prevImageIndex - 4).getId());
			row++;
			column = 2;
		}
	}

	private void addNewImageView() {
		// Add the image view.
		mImageView = new ImageView(getApplicationContext());
		mImageView.setId(mId);
		mImageViewArray.add(mImageView);
		// Retrieve the camera preview and set to an image view
		mImageView.setImageBitmap(mTextureView.getBitmap(mParams.width,
				mParams.height));
		mImageView.setAlpha(1.0f);
		mImageView.setRotation(0f);

		// Configure the new layout.
		mParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		if (mId == 1) {
			column++;
		}
		else if (column < 5 && row == 1 && mId != 1) { // First row
			mParams.addRule(RelativeLayout.RIGHT_OF, mImageViewArray.get(mId - 2)
					.getId());
			column++;
		}
		else if (column < 5 && row > 1) { // Rows > 1
			mParams.addRule(RelativeLayout.RIGHT_OF, mImageViewArray.get(mId - 2)
					.getId());
			mParams.addRule(RelativeLayout.BELOW, mImageViewArray.get(mId - 5)
					.getId());
			column++;
		}
		else {
			mParams.addRule(RelativeLayout.BELOW, mImageViewArray.get(mId - 5)
					.getId());
			row++;
			column = 2;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@SuppressLint("NewApi")
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		mCamera = Camera.open();

		try {
			mCamera.setPreviewTexture(surface);
			mCamera.startPreview();
		}
		catch (IOException ioe) {
			// Something bad happened
		}
	}

	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// Ignored, Camera does all the work for us
	}

	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mCamera.stopPreview();
		mCamera.release();
		return true;
	}

	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// Invoked every time there's a new Camera preview frame
	}

	private void clean() {
		// Clears the views and removes the listeners for each image view.
		if (mImageViewArray.size() < 5) {
			for (int i = 0; i < mImageViewArray.size(); i++) {
				mImageViewArray.get(i).setOnTouchListener(null);
				mRelativeLayout.removeView(mImageViewArray.get(i));
			}
		}
		else {
			// Clear all the image views.
			mRelativeLayout.removeAllViews();
		}
		// Reset the layouts and return to initial state.
		createStartingViewsAndLayouts();
		startTextureViewOnTouchListener();
	}

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.clean:
			clean();
			return true;
		case R.id.save:
			ArrayList<Bitmap> bitmap = new ArrayList();
			// Save the image views as a collage and save it in the gallery.
			for (int i = 0; i < mImageViewArray.size(); i++) {
				bitmap.add(((BitmapDrawable) (mImageViewArray.get(i).getDrawable()))
						.getBitmap());
			}
			Bitmap bmp = combineImageIntoOne(bitmap);
			MediaStore.Images.Media.insertImage(getContentResolver(), bmp,
					collageName + value, "New Collage");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
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

/**
 * 
 * @author Carl Barbee
 * @assignment Homework 4 Implementing an Android app that takes pictures, shows
 *             results, allows dragging of image views and lets you combine
 *             multiple pictures into one and save onto persistent storage.
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity implements SurfaceTextureListener {
	/**
	 * Scrollview for the app.
	 */
	private ScrollView mScrollView;
	/**
	 * TextureView for the app.
	 */
	private TextureView mTextureView;
	/**
	 * Imageview for the app.
	 */
	private ImageView mImageView;
	/**
	 * Array list of image views for the app.
	 */
	private ArrayList<ImageView> mImageViewArray;
	/**
	 * Id field for each image view.
	 */
	private int mId;
	/**
	 * Camera for the app.
	 */
	private Camera mCamera;
	/**
	 * Relative layout for the app.
	 */
	private RelativeLayout mRelativeLayout;
	/**
	 * Parameters for the app.
	 */
	private RelativeLayout.LayoutParams mParams;
	/**
	 * Height field for each image view.
	 */
	private int mHeight = 200;
	/**
	 * Width field for each image view.
	 */
	private int mWidth = 240;
	/**
	 * X fields for the onTouch
	 */
	private float mPrevTouchX, mDX, mPosX, mPrevX;
	/**
	 * Y fields for the onTouch
	 */
	private float mPrevTouchY, mDY, mPosY, mPrevY;
	/**
	 * Row field for adding new elements to the layout.
	 */
	private int row = 1;
	/**
	 * Column field for adding new elements to the layout.
	 */
	private int column = 1;
	/**
	 * Collage name for each new image file added.
	 */
	private String collageName = "CollageName";
	/**
	 * Value to add for each new image file added.
	 */
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

			/**
			 * Overriding the onTouch method for adding new image views.
			 */
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
						// Moves the image view next to previous image view.
						mParams.addRule(RelativeLayout.RIGHT_OF,
								mImageViewArray.get(mId - 1).getId());
						mParams.addRule(RelativeLayout.ALIGN_TOP,
								mImageViewArray.get(mId - 1).getId());
						mParams.addRule(RelativeLayout.ALIGN_BOTTOM,
								mImageViewArray.get(mId - 1).getId());
					}
					else {
						// Moves the image view to the next row.
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

	/**
	 * Creates the default layouts for the app. Called when the app starts up and
	 */
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

	/**
	 * Combines the images together.
	 * 
	 * @param bitmap
	 *          The bitmap images.
	 * @return
	 */
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

	/**
	 * Enables the drag feature when there are only 4 image views in the activity.
	 * 
	 * @param imgView
	 */
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

	/**
	 * Disables the image views drag feature once five image views are displayed.
	 */
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

	/**
	 * Reconfigures the image views after 5 image views are displayed to the
	 * original gird layout.
	 * 
	 * @param img
	 *          The image to be reconfigured
	 * @param prevImageIndex
	 *          The previous image views index in the image view array.
	 */
	private void reconfigureImageViews(ImageView img, int prevImageIndex) {
		mParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		// Check for first image view in grid layout.
		if (prevImageIndex == 0) {
			column++;
		}
		else if (column < 5 && row == 1) {
			// First row of image views.
			mParams.addRule(RelativeLayout.RIGHT_OF,
					mImageViewArray.get(prevImageIndex - 1).getId());
			column++;
		}
		else if (column < 5 && row > 1) {
			// Row > 1
			mParams.addRule(RelativeLayout.RIGHT_OF,
					mImageViewArray.get(prevImageIndex - 1).getId());
			mParams.addRule(RelativeLayout.BELOW,
					mImageViewArray.get(prevImageIndex - 4).getId());
			column++;
		}
		else {
			// Moving the image view to the next row.
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

		// First image view.
		if (mId == 1) {
			column++;
		}
		else if (column < 5 && row == 1 && mId != 1) {
			// First row of image views.
			mParams.addRule(RelativeLayout.RIGHT_OF, mImageViewArray.get(mId - 2)
					.getId());
			column++;
		}
		else if (column < 5 && row > 1) {
			// Rows > 1
			mParams.addRule(RelativeLayout.RIGHT_OF, mImageViewArray.get(mId - 2)
					.getId());
			mParams.addRule(RelativeLayout.BELOW, mImageViewArray.get(mId - 5)
					.getId());
			column++;
		}
		else {
			// Moves the image view to the next row.
			mParams.addRule(RelativeLayout.BELOW, mImageViewArray.get(mId - 5)
					.getId());
			row++;
			column = 2;
		}
	}

	/**
	 * Setups the drop down menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Closes out the camera preview when app pauses
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * Setups the camera preview.
	 */
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

	/**
	 * Closes the camera preview when the app is closed.
	 */
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
		return true;
	}

	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// Invoked every time there's a new Camera preview frame
	}

	/**
	 * The clean option for the app. Clears all the image views from the layout
	 * and resets the layout to original startup configuration.
	 */
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
	 * Saves the image views as one picture to the gallery on your phone.
	 */
	private void save() {
		ArrayList<Bitmap> bitmap = new ArrayList<Bitmap>();
		// Save the image views as a collage and save it in the gallery.
		for (int i = 0; i < mImageViewArray.size(); i++) {
			bitmap.add(((BitmapDrawable) (mImageViewArray.get(i).getDrawable()))
					.getBitmap());
		}
		// Name the new image an arbitrary file name.
		Bitmap bmp = combineImageIntoOne(bitmap);
		MediaStore.Images.Media.insertImage(getContentResolver(), bmp, collageName
				+ value, "New Collage");
	}

	/**
	 * On selecting action bar icons. Clean option clears all the image views and
	 * save will save the image views as one picture to the gallery section of
	 * your phone.
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.clean:
			clean();
			return true;
		case R.id.save:
			save();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
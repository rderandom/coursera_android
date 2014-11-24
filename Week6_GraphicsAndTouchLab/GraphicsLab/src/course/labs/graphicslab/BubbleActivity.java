package course.labs.graphicslab;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BubbleActivity extends Activity {

	// These variables are for testing purposes, do not modify
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final String TAG = "Lab-Graphics";

	// The Main view
	private RelativeLayout mFrame;

	// Bubble image's bitmap
	private Bitmap mBitmap;

	// Display dimensions
	private int mDisplayWidth, mDisplayHeight;

	// Sound variables

	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID for the bubble popping sound
	private int mSoundID;
	// Audio volume
	private float mStreamVolume;

	
	// touchedBubbleView
	BubbleView touchedBubbleView;
	
	// Gesture Detector
	private GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Set up user interface
		mFrame = (RelativeLayout) findViewById(R.id.frame);

		// Load basic bubble Bitmap
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Manage bubble popping sound
		// Use AudioManager.STREAM_MUSIC as stream type

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		mStreamVolume = (float) mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// TODO OK___- make a new SoundPool, allowing up to 10 streams
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		// TODO OK___- set a SoundPool OnLoadCompletedListener that calls
		// setupGestureDetector()
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener(){

			@Override
			public void onLoadComplete(SoundPool soundPool, int arg1, int arg2) {
				setupGestureDetector();
			}	
		});

		// TODO OK___ - load the sound from res/raw/bubble_pop.wav
		mSoundID = mSoundPool.load(this, R.raw.bubble_pop, 1);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Get the size of the display so this View knows where borders are
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

		}
	}

	// Set up GestureDetector
	private void setupGestureDetector() {
		mGestureDetector = new GestureDetector(this,
		new GestureDetector.SimpleOnGestureListener() {

			// If a fling gesture starts on a BubbleView then change the
			// BubbleView's velocity
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,	float velocityX, float velocityY) {
				// TODO - Implement onFling actions.
				// You can get all Views in mFrame one at a time
				// using the ViewGroup.getChildAt() method
				float touchedX = event1.getRawX();
				float touchedY = event1.getRawY();
				int numOfBubbles = mFrame.getChildCount();
				boolean isIntersecting = false;
				for (int i = 0; (i < numOfBubbles) && !isIntersecting; i++) {
					BubbleView bubble = (BubbleView) mFrame.getChildAt(i);
					isIntersecting = bubble.intersects(touchedX, touchedY);
					if(isIntersecting){
						bubble.deflect(velocityX, velocityY);
					}

				}

				return isIntersecting;
			}

			// If a single tap intersects a BubbleView, then pop the BubbleView
			// Otherwise, create a new BubbleView at the tap's location and add
			// it to mFrame. You can get all views from mFrame with
			// ViewGroup.getChildAt()
			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {
				// TODO - Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

				float touchedX = event.getRawX();
				float touchedY = event.getRawY();
				int numOfBubbles = mFrame.getChildCount();
				boolean isIntersecting = false;
				for (int i = 0; (i < numOfBubbles) && !isIntersecting; i++) {
					BubbleView bubble = (BubbleView) mFrame.getChildAt(i);
					isIntersecting = bubble.intersects(touchedX, touchedY);
					touchedBubbleView = bubble;

				}
				
				if(isIntersecting) {
					touchedBubbleView.stopMovement(true);

				} else {
					//new bubble
					BubbleView bubbleView = new BubbleView(mFrame.getContext(), touchedX, touchedY);
					mFrame.addView(bubbleView);
					bubbleView.startMovement();

				}


				return true;
			}
		});//END OF mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {__________
	}
	

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO OK ___ - Delegate the touch to the gestureDetector
        mGestureDetector.onTouchEvent(event);
        return false;
	}

	@Override
	protected void onPause() {
		// TODO OK__- Release all SoundPool resources
		mSoundPool.release();
		super.onPause();
	}

	// BubbleView is a View that displays a bubble.
	// This class handles animating, drawing, and popping amongst other actions.
	// A new BubbleView is created for each bubble on the display

	public class BubbleView extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mScaledBitmapWidth;
		private Bitmap mScaledBitmap;

		// location, speed and direction of the bubble
		private float mXPos, mYPos, mDx, mDy, mRadius, mRadiusSquared;
		private long mRotate, mDRotate;

		BubbleView(Context context, float x, float y) {
			super(context);

			// Create a new random number generator to
			// randomize size, rotation, speed and direction
			Random r = new Random();

			// Creates the bubble bitmap for this BubbleView
			createScaledBitmap(r);

			// Radius of the Bitmap
			mRadius = mScaledBitmapWidth / 2;
			mRadiusSquared = mRadius * mRadius;		
			// Adjust position to center the bubble under user's finger
			mXPos = x -  mScaledBitmapWidth / 2;
			mYPos = y -  mScaledBitmapWidth / 2;
			// Set the BubbleView's speed and direction
			setSpeedAndDirection(r);
			// Set the BubbleView's rotation
			setRotation(r);
			mPainter.setAntiAlias(true);
		}

		

		private void setRotation(Random r) {
			if (speedMode == RANDOM) {
				// TODO OK___- set rotation in range [1..3]
				mDRotate = r.nextInt(3) + 1;
				
			} else {
				mDRotate = 0;
			}
		}

		private void setSpeedAndDirection(Random r) {
			// Used by test cases
			switch (speedMode) {
			case SINGLE:
				mDx = 10;
				mDy = 10;
				break;
			case STILL:
				// No speed
				mDx = 0;
				mDy = 0;
				break;
			default:
				// TODO - Set mDx and mDy to indicate movement direction and speed 
				// Limit speed in the x and y direction to [-3..3] pixels per movement.
				if( (r.nextInt(2)+1)>1 ){
					mDx = r.nextInt(5) + 1;
					mDy = r.nextInt(5) + 1;
				} else {
					mDx = r.nextInt(5) + 1;
					mDx*= -1;
					mDy = r.nextInt(5) + 1;
					mDy*= -1;
				}

			}
		}

		private void createScaledBitmap(Random r) {
			if (speedMode != RANDOM) {
				mScaledBitmapWidth = BITMAP_SIZE * 3;
			} else {
				// TODO OK___- set scaled bitmap size in range [1..3] * BITMAP_SIZE
				mScaledBitmapWidth = BITMAP_SIZE *  (r.nextInt(3) + 1);	
			}

			// TODO OK___- create the scaled bitmap using size set above
			mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, mScaledBitmapWidth, mScaledBitmapWidth, true);

		}

		// Start moving the BubbleView & updating the display
		private void startMovement() {

			// Creates a WorkerThread
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

			// Execute the run() in Worker Thread every REFRESH_RATE
			// milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {

					// TODO - implement movement logic.
					// Each time this method is run the BubbleView should
					// move one step. If the BubbleView exits the display,
					// stop the BubbleView's Worker Thread.
					// Otherwise, request that the BubbleView be redrawn.

					boolean isOutOfScreen = moveWhileOnScreen();
					if(isOutOfScreen){
						mMoverFuture.cancel(true);
						stopMovement(false);
					} else {
						BubbleView.this.postInvalidate();
						
					}
					
					
				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}

	
		
		// Returns true if the BubbleView intersects position (x,y)
		private synchronized boolean intersects(float x, float y) {
			// TODO OK___ - Return true if the BubbleView intersects position (x,y)
//			Rect rect = new Rect(); 
//			this.getDrawingRect(rect); 
//			return rect.contains((int) x, (int) y);			
	
			if( (x > mXPos && x < mXPos + mScaledBitmapWidth) && (y > mYPos && y < mYPos + mScaledBitmapWidth)){
				return true;
	
			}else {
				return false;

			}

		}

		// Cancel the Bubble's movement
		// Remove Bubble from mFrame
		// Play pop sound if the BubbleView was popped

		private void stopMovement(final boolean wasPopped) {

			if (null != mMoverFuture) {
				if (!mMoverFuture.isDone()) {
					mMoverFuture.cancel(true);
				}

				// This work will be performed on the UI Thread
				mFrame.post(new Runnable() {
					@Override
					public void run() {
						// TODO OK___ - Remove the BubbleView from mFrame
						mFrame.removeView(BubbleView.this);


						// TODO OK___- If the bubble was popped by user,
						// play the popping sound
						if (wasPopped) {
							mSoundPool.play(mSoundID, mStreamVolume, mStreamVolume, 0, 0, 1.0f);						
						}
					}
				});
			}
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) {
			mDx = velocityX / REFRESH_RATE;
			mDy = velocityY / REFRESH_RATE;
		}

		// Draw the Bubble at its current location
		@Override
		protected synchronized void onDraw(Canvas canvas) {
			// TODO - save the canvas
			canvas.save();
			
			// TODO - increase the rotation of the original image by mDRotate
			mRotate += mDRotate;
			
			// TODO Rotate the canvas by current rotation
			// Hint - Rotate around the bubble's center, not its position
			canvas.rotate(mRotate,mXPos + (mScaledBitmapWidth / 2), mYPos + (mScaledBitmapWidth / 2));
			
			// TODO - draw the bitmap at it's new location
			canvas.drawBitmap(mScaledBitmap, mXPos, mYPos, mPainter);

			
			// TODO - restore the canvas
			canvas.restore();	
		}

		// Returns true if the BubbleView is still on the screen after the move
		// operation
		private synchronized boolean moveWhileOnScreen() {
			// TODO - Move the BubbleView
			mXPos += mDx;
			mYPos += mDy;
			return isOutOfView();

		}

		// Return true if the BubbleView is still on the screen after the move
		// operation
		private boolean isOutOfView() {
			// TODO - Return true if the BubbleView is still on the screen after
			// the move operation

	
				
//			if (BubbleView.this.getVisibility() == View.VISIBLE){
//				return true;
//			} else {
//				return false;
//			}
			 
				if( mXPos > mDisplayWidth || 			// check the right edge
						mXPos + mScaledBitmapWidth < 0 ||	// check the left edge
						mYPos > mDisplayHeight ||			//   "    "  bottom edge
						mYPos + mScaledBitmapWidth < 0		//   "    "  top edge
						){
					return true;

				}{
					return false;
		
				}

			

		}
	} //END OF BubbleView________________________________________________________________________________

	
	
	
	
	
	
	// Do not modify below here

	@Override
	public void onBackPressed() {
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_still_mode:
			speedMode = STILL;
			return true;
		case R.id.menu_single_speed:
			speedMode = SINGLE;
			return true;
		case R.id.menu_random_mode:
			speedMode = RANDOM;
			return true;
		case R.id.quit:
			exitRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void exitRequested() {
		super.onBackPressed();
	}
}
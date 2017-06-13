package pro.shpin.kirill.simplerendering.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.concurrent.Semaphore;

/**
 * Created by wiish on 6/9/2017.
 */

public class GameView extends GLSurfaceView{

	public static Vector3f origin = new Vector3f(0, 0);
	public static Vector3f originT = new Vector3f(0, 0);

	public static float originDX = 0;
	public static float originDY = 0;
	public static boolean originDown = false;
	public static boolean canMove = true;

	public static float pinX = 0;
	public static float pinY = 0;
	public static float pinDX = 0;
	public static float pinDY = 0;
	public static boolean pinDown = false;


	public static float offX = 0;
	public static float offY = 0;
	public static float angle = 0;

	public static Semaphore semaphore = new Semaphore(1);

	public static float totalScale = 1;
	public static float offsetX = 0;
	public static float offsetY = 0;

	public ScaleGestureDetector scaleDetector;

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float cx = (((((detector.getFocusX()/ GLESRenderer.width) - 0.5f) * 2) * GLESRenderer.aspectX) + 1)/2.0f;
			float cy = (((((detector.getFocusY()/ GLESRenderer.height) - 0.5f) * 2) * GLESRenderer.aspectY) + 1)/2.0f;
			cy = 1-cy;

			Vector3f temp = new Vector3f(cx, cy);
			Vector3f tempT = new Vector3f(temp.x*totalScale + offsetX, temp.y*totalScale + offsetY);

			float sc = 1.0f/detector.getScaleFactor();

			try {
				semaphore.acquire();

				totalScale *= sc;

				semaphore.release();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return true;
		}
	}

	public GameView(Context context) {
		super(context);

		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int index = event.getActionIndex();

		int action = event.getAction();

		int id = event.getPointerId(index);

		origin.x = (((((event.getX(0)/ GLESRenderer.width) - 0.5f) * 2) * GLESRenderer.aspectX) + 1)/2.0f;
		origin.y = (((((event.getY(0)/ GLESRenderer.height) - 0.5f) * 2) * GLESRenderer.aspectY) + 1)/2.0f;
		origin.y = 1 - origin.y;

		originT = new Vector3f(origin.x*totalScale + offsetX, origin.y*totalScale + offsetY);

		scaleDetector.onTouchEvent(event);

		if(event.getPointerCount() > 1) {
			canMove = false;
			return true;
		}

		if(action == MotionEvent.ACTION_DOWN) {
			originDX = event.getX(0);
			originDY = event.getY(0);

			originDown = true;
		} else if (action == MotionEvent.ACTION_UP) {
			originDown = false;
			canMove = true;
		} else if (action == MotionEvent.ACTION_MOVE && canMove) {
			float dx = event.getX(0) - originDX;
			float dy = event.getY(0) - originDY;

			float tdx = (dx/ GLESRenderer.width) * GLESRenderer.aspectX;
			float tdy = (dy/ GLESRenderer.height) * GLESRenderer.aspectY;

			Vector3f temp = new Vector3f(-tdx, tdy);
			temp.z = 0;
			Vector3f tempT = new Vector3f(temp.x*totalScale, temp.y*totalScale);

			offX -= tdx;
			offY += tdy;

			try {
				semaphore.acquire();

				offsetX += tempT.x;
				offsetY += tempT.y;

				semaphore.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			originDX = event.getX(0);
			originDY = event.getY(0);
		}

		return true;
	}
}

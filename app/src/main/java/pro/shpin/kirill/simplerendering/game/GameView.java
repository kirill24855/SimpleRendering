package pro.shpin.kirill.simplerendering.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
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
	public static Matrix3f transform = new Matrix3f();

	public ScaleGestureDetector scaleDetector;

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {

			float cx = (((((detector.getFocusX()/ GLESRenderer.width) - 0.5f) * 2) * GLESRenderer.aspectX) + 1)/2.0f;
			float cy = (((((detector.getFocusY()/ GLESRenderer.height) - 0.5f) * 2) * GLESRenderer.aspectY) + 1)/2.0f;
			cy = 1-cy;

			Vector3f temp = new Vector3f(cx, cy);
			Vector3f tempT = transform.mult(temp);

			float sc = 1.0f/detector.getScaleFactor();

			try {
				semaphore.acquire();

				transform.move(-tempT.x, -tempT.y);
				transform.scale(sc);
				transform.move(tempT.x, tempT.y);

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

		originT = transform.mult(origin);

		/*

		if (event.getPointerCount() > 1) {
			pinX = (((((event.getX(1)/GLES20Renderer.width) - 0.5f) * 2) * GLES20Renderer.aspectX) + 1)/2.0f;
			pinY = (((((event.getY(1)/GLES20Renderer.height) - 0.5f) * 2) * GLES20Renderer.aspectY) + 1)/2.0f;
			pinY = 1 - pinY;

			if(action == MotionEvent.ACTION_POINTER_2_DOWN) {
				pinDX = event.getX(1);
				pinDY = event.getY(1);

				pinDown = true;
			} else if (action == MotionEvent.ACTION_POINTER_2_UP) {
				pinDown = false;
			} else if (action == MotionEvent.ACTION_MOVE && pinDown) {
				float dx = event.getX(1) - pinDX;
				float dy = event.getY(1) - pinDY;

				float tdx = (dx/GLES20Renderer.width) * GLES20Renderer.aspectX;
				float tdy = (dy/GLES20Renderer.height) * GLES20Renderer.aspectY;

				float cx1 = pinX - tdx - origin.x;
				float cy1 = pinY - tdy - origin.y;
				float cx2 = pinX - origin.x;
				float cy2 = pinY - origin.y;

				float pa1 = (float)Math.atan2(cy1, cx1);
				float pa2 = (float)Math.atan2(cy2, cx2);

				float da = pr2 - pr1;

				transform.move(-originT.x, -originT.y);
				transform.rotate(da);
				transform.move(originT.x, originT.y);

				float pr1 = (float)Math.sqrt(cx1*cx1 + cy1*cy1);
				float pr2 = (float)Math.sqrt(cx2*cx2 + cy2*cy2);

				float sc = pr2/pr1;

				transform.sacle(sc);

				pinDX = event.getX(1);
				pinDY = event.getY(1);
			}
		}

		*/

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
			Vector3f tempT = transform.mult(temp);

			offX -= tdx;
			offY += tdy;

			try {
				semaphore.acquire();

				transform.move(tempT.x, tempT.y);

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

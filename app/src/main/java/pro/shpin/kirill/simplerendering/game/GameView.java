package pro.shpin.kirill.simplerendering.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.concurrent.Semaphore;

import pro.shpin.kirill.simplerendering.GameActivity;

/**
 * Created by wiish on 6/9/2017.
 */

public class GameView extends GLSurfaceView{

	public static Vector3d origin = new Vector3d(0, 0);
	public static Vector3d originT = new Vector3d(0, 0);

	public static double originDX = 0;
	public static double originDY = 0;
	public static boolean originDown = false;
	public static boolean canMove = true;

	public static Semaphore semaphore = new Semaphore(1);
	public static Matrix3d transform = new Matrix3d();

	public static double totalScale = 1;
	public static double offsetX = 0;
	public static double offsetY = 0;

	public ScaleGestureDetector scaleDetector;

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			double cx = (((((detector.getFocusX()/ GLESRenderer.width) - 0.5f) * 2) * GLESRenderer.aspectX) + 1)/2.0f;
			double cy = (((((detector.getFocusY()/ GLESRenderer.height) - 0.5f) * 2) * GLESRenderer.aspectY) + 1)/2.0f;
			cy = 1-cy;

			Vector3d temp = new Vector3d(cx, cy);
			Vector3d tempT = transform.mult(temp);

			double sc = 1.0f/detector.getScaleFactor();

			try {
				semaphore.acquire();

				transform.move(-tempT.x, -tempT.y);
				transform.scale(sc);
				transform.move(tempT.x, tempT.y);

				semaphore.release();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Log.i("Scale", "Scale: " + totalScale);

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

		origin.x = (((((event.getX(0)/ GLESRenderer.width) - 0.5f) * 2) * GLESRenderer.aspectX) + 1)/2.0f;
		origin.y = (((((event.getY(0)/ GLESRenderer.height) - 0.5f) * 2) * GLESRenderer.aspectY) + 1)/2.0f;
		origin.y = 1 - origin.y;

		originT = transform.mult(origin);

		scaleDetector.onTouchEvent(event);

		totalScale = transform.get(0, 0);

		offsetX = transform.get(0,2);
		offsetY = transform.get(1,2);

		if(event.getPointerCount() > 1) {
			canMove = false;
			return true;
		}

		if(action == MotionEvent.ACTION_DOWN) {
			originDX = event.getX(0);
			originDY = event.getY(0);

			originDown = true;


			GameActivity.renderer.setRenderMode(GLESRenderer.RENDER_DOWNSCALE);
		} else if (action == MotionEvent.ACTION_UP) {
			originDown = false;
			canMove = true;
			GameActivity.renderer.setRenderMode(GLESRenderer.RENDER_UPSCALE);
		} else if (action == MotionEvent.ACTION_MOVE && canMove) {
			double dx = event.getX(0) - originDX;
			double dy = event.getY(0) - originDY;

			double tdx = (dx/ GLESRenderer.width) * GLESRenderer.aspectX;
			double tdy = (dy/ GLESRenderer.height) * GLESRenderer.aspectY;

			Vector3d temp = new Vector3d(-tdx, tdy);
			temp.z = 0;
			Vector3d tempT = transform.mult(temp);

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

		offsetX = transform.get(0,2);
		offsetY = transform.get(1,2);

		totalScale = transform.get(0, 0);

		return true;
	}
}

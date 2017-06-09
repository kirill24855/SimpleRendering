package pro.shpin.kirill.simplerendering.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by wiish on 6/9/2017.
 */

public class GameView extends GLSurfaceView{

	public static float originX = 0;
	public static float originY = 0;
	public static float originDX = 0;
	public static float originDY = 0;
	public static boolean originDown = false;

	public static float pinX = 0;
	public static float pinY = 0;
	public static float pinDX = 0;
	public static float pinDY = 0;
	public static boolean pinDown = false;


	public static float offX = 0;
	public static float offY = 0;
	public static float angle = 0;

	public static Matrix3f transform = new Matrix3f();

	public GameView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int index = event.getActionIndex();

		int action = event.getAction();

		int id = event.getPointerId(index);

		if (event.getPointerCount() > 1) {
			pinX = (((((event.getX(1)/GLES20Renderer.width) - 0.5f) * 2) * GLES20Renderer.aspectX) + 1)/2.0f;
			pinY = (((((event.getY(1)/GLES20Renderer.height) - 0.5f) * 2) * GLES20Renderer.aspectY) + 1)/2.0f;
			originY = 1 - originY;

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

				float cx1 = pinX - tdx - originX;
				float cy1 = pinY - tdy - originY;
				float cx2 = pinX - originX;
				float cy2 = pinY - originY;

				float pr1 = (float)Math.atan2(cy1, cx1);
				float pr2 = (float)Math.atan2(cy2, cx2);

				float da = pr2 - pr1;

				//transform.move(-originX*2, -originY*2);
				transform.rotate(da);
				//transform.move(originX*2, originY*2);

				pinDX = event.getX(1);
				pinDY = event.getY(1);
			}
		}

		originX = (((((event.getX(0)/GLES20Renderer.width) - 0.5f) * 2) * GLES20Renderer.aspectX) + 1)/2.0f;
		originY = (((((event.getY(0)/GLES20Renderer.height) - 0.5f) * 2) * GLES20Renderer.aspectY) + 1)/2.0f;
		originY = 1 - originY;

		if(action == MotionEvent.ACTION_DOWN) {
			originDX = event.getX(0);
			originDY = event.getY(0);

			originDown = true;
		} else if (action == MotionEvent.ACTION_UP) {
			originDown = false;
		} else if (action == MotionEvent.ACTION_MOVE) {
			float dx = event.getX(0) - originDX;
			float dy = event.getY(0) - originDY;

			float tdx = (dx/GLES20Renderer.width) * GLES20Renderer.aspectX;
			float tdy = (dy/GLES20Renderer.height) * GLES20Renderer.aspectY;

			offX -= tdx;
			offY += tdy;

			transform.move(-tdx, tdy);

			originDX = event.getX(0);
			originDY = event.getY(0);
		}

		return true;
	}
}

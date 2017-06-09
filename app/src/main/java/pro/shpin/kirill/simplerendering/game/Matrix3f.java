package pro.shpin.kirill.simplerendering.game;

public class Matrix3f {

	private float[] data;

	public Matrix3f() {
		data = new float[9];

		for(int i = 0;i < 3;i++) {
			for(int j = 0;j < 3;j++) {
				if(i == j) {
					data[i * 3 + j] = 1;
				} else {
					data[i *3 + j] = 0;
				}
			}
		}
	}

	public void move(float x, float y) {
		Matrix3f temp = new Matrix3f();
		temp.set(0, 2, x);
		temp.set(1, 2, y);

		data = mult(temp, this).getData();
	}

	public void rotate(float angle) {
		Matrix3f temp = new Matrix3f();

		temp.set(0, 0, (float)Math.cos(angle));
		temp.set(0, 1, (float)-Math.sin(angle));
		temp.set(1, 0, (float)Math.sin(angle));
		temp.set(1, 1, (float)Math.cos(angle));

		data = mult(temp, this).getData();
	}

	public void scale(float sc) {
		Matrix3f temp = new Matrix3f();

		temp.set(0, 0, sc);
		temp.set(1, 1, sc);

		data = mult(temp, this).getData();
	}

	public Vector3f mult(Vector3f vert) {
		Vector3f result = new Vector3f(0, 0);

		result.x = vert.x * get(0, 0) + vert.y * get(0, 1) + vert.z * get(0, 2);
		result.y = vert.x * get(1, 0) + vert.y * get(1, 1) + vert.z * get(1, 2);
		result.z = vert.x * get(2, 0) + vert.y * get(2, 1) + vert.z * get(2, 2);

		return result;
	}

	public static Matrix3f mult(Matrix3f m1, Matrix3f m2) {
		Matrix3f result = new Matrix3f();

		float acc = 0;

		for(int c = 0; c < 3;c++) {
			for(int r = 0; r < 3;r++) {
				acc = 0;

				for(int i = 0;i < 3;i++) {
					acc += m1.get(c, i)*m2.get(i, r);
				}

				result.set(c, r, acc);
			}
		}

		return result;
	}

	public String toString() {
		String result = "";
		result += "\n";

		result += "[ " + get(0, 0) + " , " + get(0, 1) + " , " + get(0, 2) +  " ]\n";
		result += "[ " + get(1, 0) + " , " + get(1, 1) + " , " + get(1, 2) +  " ]\n";
		result += "[ " + get(2, 0) + " , " + get(2, 1) + " , " + get(2, 2) +  " ]\n";

		return result;
	}

	public float get(int c, int r) {
		return data[r * 3 + c];
	}

	public void set(int c, int r, float number) {
		data[r * 3 + c] = number;
	}

	public float[] getData() {
		return data;
	}
}

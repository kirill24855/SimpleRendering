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

		data = mult(this, temp).getData();
	}

	public void rotate(float angle) {
		Matrix3f temp = new Matrix3f();

		temp.set(0, 0, (float)Math.cos(angle));
		temp.set(0, 1, (float)-Math.sin(angle));
		temp.set(1, 0, (float)Math.sin(angle));
		temp.set(1, 1, (float)Math.cos(angle));

		data = mult(temp, this).getData();
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

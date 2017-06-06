package pro.shpin.kirill.simplerendering.game;

/**
 * Created by wiish on 10/16/2016.
 */

public class Matrix4f {
    float[] data;

    public Matrix4f() {
        data = new float[16];

        for(int i = 0;i < 4;i++) {
            for(int j = 0;j < 4;j++) {
                if(i == j) {
                    data[i * 4 + j] = 1;
                } else {
                    data[i *4 + j] = 0;
                }
            }
        }
    }

    public void move(float x, float y, float z) {
        Matrix4f temp = new Matrix4f();
        temp.set(0, 3, x);
        temp.set(1, 3, y);
        temp.set(2, 3, z);

        data = mult(this, temp).getData();
    }

    public void rotateX(float angle) {
        Matrix4f temp = new Matrix4f();

        temp.set(1, 1, (float)Math.cos(angle));
        temp.set(1, 2, (float)-Math.sin(angle));
        temp.set(2, 1, (float)Math.sin(angle));
        temp.set(2, 2, (float)Math.cos(angle));

        data = mult(this, temp).getData();
    }

    public void rotateY(float angle) {
        Matrix4f temp = new Matrix4f();

        temp.set(2, 2, (float)Math.cos(angle));
        temp.set(2, 0, (float)-Math.sin(angle));
        temp.set(0, 2, (float)Math.sin(angle));
        temp.set(0, 0, (float)Math.cos(angle));

        data = mult(this, temp).getData();
    }

    public void rotateZ(float angle) {
        Matrix4f temp = new Matrix4f();

        temp.set(0, 0, (float)Math.cos(angle));
        temp.set(0, 1, (float)-Math.sin(angle));
        temp.set(1, 0, (float)Math.sin(angle));
        temp.set(1, 1, (float)Math.cos(angle));

        data = mult(this, temp).getData();
    }

    public static Matrix4f mult(Matrix4f m1, Matrix4f m2) {
        Matrix4f result = new Matrix4f();

        float acc = 0;

        for(int c = 0; c < 4;c++) {
            for(int r = 0; r < 4;r++) {
                acc = 0;

                for(int i = 0;i < 4;i++) {
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

        result += "[ " + get(0, 0) + " , " + get(0, 1) + " , " + get(0, 2) + " , " + get(0, 3) + " ]\n";
        result += "[ " + get(1, 0) + " , " + get(1, 1) + " , " + get(1, 2) + " , " + get(1, 3) + " ]\n";
        result += "[ " + get(2, 0) + " , " + get(2, 1) + " , " + get(2, 2) + " , " + get(2, 3) + " ]\n";
        result += "[ " + get(3, 0) + " , " + get(3, 1) + " , " + get(3, 2) + " , " + get(3, 3) + " ]\n";

        return result;
    }

    public float get(int c, int r) {
        return data[r * 4 + c];
    }

    public void set(int c, int r, float number) {
        data[r * 4 + c] = number;
    }

    public float[] getData() {
        return data;
    }
}

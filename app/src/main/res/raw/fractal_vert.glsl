attribute vec2 position;

varying vec2 uv;

uniform vec3 trans;

void main() {
	gl_Position = vec4(position.x / trans.z + trans.x, position.y / trans.z + trans.y, 0.0, 1.0);
	uv = (position + 1.0)/2.0;
}
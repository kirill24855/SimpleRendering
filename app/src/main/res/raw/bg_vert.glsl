precision highp float;

attribute vec2 position;

varying vec2 uv;

void main() {
	gl_Position = vec4(position.xy, 0.0, 1.0);
	uv = (position + 1.0)/2.0;
}
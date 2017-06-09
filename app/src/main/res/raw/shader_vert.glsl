attribute vec2 position;

varying vec2 uv;

uniform vec2 aspect;

void main() {
	gl_Position = vec4(position.xy, 0.0, 1.0);
	uv = vec2(position.x*aspect.x, position.y*aspect.y);
}
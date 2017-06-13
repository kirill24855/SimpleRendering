attribute vec2 position;

varying vec2 uv;

uniform vec2 aspect;
uniform float sc;
uniform vec2 off;

uniform float scale;

void main() {
	vec2 finalPos = ((position + 1.0) * scale) - 1.0;

	gl_Position = vec4(position.xy, 0.0, 1.0);
	vec3 ptp = vec3(finalPos.x*aspect.x*0.5 + 0.5, finalPos.y*aspect.y*0.5 + 0.5, 1.0);
	vec3 tp = vec3(ptp.x*sc + off.x, ptp.y*sc + off.y, 1.0);
	uv = vec2(tp.xy) * 4.0 - 2.0;
}
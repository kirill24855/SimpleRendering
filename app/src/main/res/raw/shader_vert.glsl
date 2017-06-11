precision highp float;

attribute vec2 position;

varying vec2 uv;

uniform vec2 aspect;
uniform mat3 transform;

void main() {
	gl_Position = vec4(position.xy, 0.0, 1.0);
	vec3 ptp = vec3(position.x*aspect.x*0.5 + 0.5, position.y*aspect.y*0.5 + 0.5, 1.0);
	vec3 tp = transform * ptp;
	uv = vec2(tp.xy) * 4.0 - 2.0;
}
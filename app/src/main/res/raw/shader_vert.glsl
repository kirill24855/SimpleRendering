#version 310 es

in vec2 position;

out vec4 ptp;

uniform vec2 aspect;
uniform float scale;

void main() {
	vec2 finalPos = ((position + 1.0) * scale) - 1.0;

	gl_Position = vec4(position.xy, 0.0, 1.0);

	ptp = vec4(0);
	ptp.x = finalPos.x*aspect.x*0.5 + 0.5;
	ptp.y = finalPos.y*aspect.y*0.5 + 0.5;
	ptp.z = 0.0;
	ptp.w = 0.0;
}
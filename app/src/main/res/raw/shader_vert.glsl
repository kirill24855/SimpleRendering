attribute vec2 position;

varying vec2 uv;

uniform vec2 aspect;
uniform mat3 transform;
uniform float scale;

void main() {
	vec2 finalPos = ((position + 1.0) * scale) - 1.0;

	gl_Position = vec4(position.xy, 0.0, 1.0);
	vec3 ptp = vec3(finalPos.x*aspect.x*0.5 + 0.5, finalPos.y*aspect.y*0.5 + 0.5, 1.0);

	//ptp  = ptp * 16.0;

	//ptp.x = ptp.x * 16.0;
	//ptp.y = ptp.y * 16.0;

	vec3 tp = transform * ptp;/*vec3(
				transform[0][1] * ptp[0] + transform[0][1] * ptp[1] + transform[0][2] * ptp[2],
				transform[1][1] * ptp[0] + transform[1][1] * ptp[1] + transform[1][2] * ptp[2],
				1.0);*/
	uv = vec2(tp.xy) * 4.0 - 2.0;//*16.0;
}
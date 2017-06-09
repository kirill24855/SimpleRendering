precision mediump float;

varying vec2 uv;

uniform mat3 transform;

void main() {
	vec3 clr = transform * vec3((uv.xy + 1.0) / 2.0, 1.0);

	float blue = 0.0;

	if(clr.x < 0.0 || clr.x > 1.0 || clr.y < 0.0 || clr.y > 1.0) {
		blue = 1.0;
	}

	gl_FragColor = vec4(clr.xy, blue, 1.0);
}
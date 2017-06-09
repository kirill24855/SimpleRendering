precision mediump float;

varying vec2 uv;

void main() {
	float blue = 0.0;

	if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
		blue = 1.0;
	}

	gl_FragColor = vec4(uv.xy, blue, 1.0);
}
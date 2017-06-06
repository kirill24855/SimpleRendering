attribute vec4 position;

uniform mat4 projection;
uniform mat4 transformation;

void main() {
	gl_Position = projection * transformation * position;
}
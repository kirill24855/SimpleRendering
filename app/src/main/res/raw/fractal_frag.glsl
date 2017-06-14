#version 310 es

precision mediump float;

out vec4 outColor;

uniform int colorScheme;
uniform vec3 colorInside;
uniform vec3 colorOutside;

layout(r32i, binding = 0) uniform iimage2D ziTex;

float hue2rgb(float p, float q, float h) {
    if (h < 0.0) h += 1.0;
    if (h > 1.0 ) h -= 1.0;
    if (6.0 * h < 1.0) return p + ((q - p) * 6.0 * h);
    if (2.0 * h < 1.0 ) return  q;
    if (3.0 * h < 2.0) return p + ((q - p) * 6.0 * ((2.0 / 3.0) - h));
    return p;
}

vec4 HSVtoRGB(float hue, float saturation, float brightness) {
    float h = hue * 6.0;
    float f = hue * 6.0 - h;
    float p = brightness * (1.0 - saturation);
    float q = brightness * (1.0 - f * saturation);
    float t = brightness * (1.0 - (1.0 - f) * saturation);

    float value = brightness;

    int comparator = int(mod(h, 6.0));
    if (comparator == 0)
    {
        return vec4(value, t, p, 0.0);
    }
    else if (comparator == 1)
    {
        return vec4(q, value, p, 0.0);
    }
    else if (comparator == 2)
    {
        return vec4(p, value, t, 0.0);
    }
    else if (comparator == 3)
    {
        return vec4(p, q, value, 0.0);
    }
    else if (comparator == 4)
    {
        return vec4(t, p, value, 0.0);
    }
    else if (comparator == 5)
    {
        return vec4(value, p, q, 0.0);
    }
    else
    {
        return vec4(1.0, 0.0, 1.0, 0.0); //Error
    }
}

vec4 HSLtoRGB(float hue, float saturation, float luminosity) {
    hue = mod(hue, 6.0);

    float q = 0.0;

    if (luminosity < 0.5)
    {
        q = luminosity * (1.0 + saturation);
    }
    else
    {
        q = (luminosity + saturation) - (saturation * luminosity);
    }

    float p = 2.0 * luminosity - q;

    float rf = max(0.0, hue2rgb(p, q, hue + (1.0 / 3.0)));
    float gf = max(0.0, hue2rgb(p, q, hue));
    float bf = max(0.0, hue2rgb(p, q, hue - (1.0 / 3.0)));

    vec3 finalColor = vec3(min(rf, 1.0), min(gf, 1.0), min(bf, 1.0));

    return vec4(finalColor, 1.0);
}

void main() {
	int iteration = imageLoad(ziTex, ivec2(gl_FragCoord.xy)).x;

	if (iteration == -1) {
		outColor = vec4(colorInside, 1.0);
	} else {
		float stepValue = float(iteration);

		if (colorScheme == 2) {
			outColor =  HSLtoRGB(mod(stepValue*0.01, 1.0), 0.7, 0.7);
		} else if (colorScheme == 1) {
			outColor =  HSVtoRGB(stepValue*0.015, 1.0, 1.0);
		}
	}
}
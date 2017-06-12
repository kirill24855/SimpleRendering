precision highp float;

varying vec2 uv;

uniform vec2 c;
uniform int maxIteration;
uniform int colorScheme;
uniform vec3 colorInside;
uniform vec3 colorOutside;

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

vec4 handleColors(int curIteration, int maxIteration, float zAbsSquared) {
    float stepValue = float(curIteration);

    float colorFactor = stepValue / float(maxIteration);
    if (colorFactor < 0.0) return vec4(colorInside, 1.0);

    if (colorScheme == 2) return HSLtoRGB(stepValue*0.015, 1.0, colorFactor);
    else if (colorScheme == 1) return HSVtoRGB(stepValue*0.015, 1.0, colorFactor/(colorFactor+0.1));
    else return vec4(colorFactor*colorOutside, 1.0);
}

void main() {
	//float blue = 0.0;

	//if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
	//	blue = 1.0;
	//}

	//gl_FragColor = vec4(uv.xy, blue, 1.0);

	vec2 z = c;
	vec2 tz = vec2(z.x, z.y);
	float x2 = 0.0;
	float y2 = 0.0;

	int iteration = -1;

	for (int i = 0; i < maxIteration; i++) {
		x2 = tz.x*tz.x;
        y2 = tz.y*tz.y;
		z.x = x2 - y2 + uv.x;//*16.0;
		z.y = 2.0*tz.x*tz.y + uv.y;//*16.0;

		tz.x = z.x;
		tz.y = z.y;

		x2 = tz.x*tz.x;
        y2 = tz.y*tz.y;
        z.x = x2 - y2 + uv.x;
        z.y = 2.0*tz.x*tz.y + uv.y;

        tz.x = z.x;
        tz.y = z.y;

        x2 = tz.x*tz.x;
		y2 = tz.y*tz.y;
		z.x = x2 - y2 + uv.x;
		z.y = 2.0*tz.x*tz.y + uv.y;

		tz.x = z.x;
		tz.y = z.y;

		x2 = tz.x*tz.x;
		y2 = tz.y*tz.y;
		z.x = x2 - y2 + uv.x;
		z.y = 2.0*tz.x*tz.y + uv.y;

		tz.x = z.x;
		tz.y = z.y;

		x2 = tz.x*tz.x;
		y2 = tz.y*tz.y;
		z.x = x2 - y2 + uv.x;
		z.y = 2.0*tz.x*tz.y + uv.y;

		tz.x = z.x;
		tz.y = z.y;

		if(x2 + y2 > 4.0) {
			iteration = i;
			break;
		}
	}

	if (iteration == -1) {
		gl_FragColor = vec4(colorInside, 1.0);
	} else {
		gl_FragColor = handleColors(iteration*5, maxIteration*5, x2 + y2);
	}
}
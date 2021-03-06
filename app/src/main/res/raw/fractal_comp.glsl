#version 310 es

layout(r32i, binding = 0) restrict uniform iimage2D ziTex;
layout(r32f, binding = 1) restrict uniform image2D zxTex;
layout(r32f, binding = 2) restrict uniform image2D zyTex;
layout(r32f, binding = 3) restrict uniform image2D zzTex;
layout(r32f, binding = 4) restrict uniform image2D zwTex;

uniform vec2 aspect;
uniform vec2 size;

uniform vec2 c;
uniform int maxIteration;
uniform ivec3 renderMode;

//emulated double precision
uniform vec2 sc;
uniform vec4 off;

layout (local_size_x = 8, local_size_y = 8, local_size_z = 1) in;

vec2 ds_add (vec2 dsa, vec2 dsb) {
	vec2 dsc;
	float t1, t2, e;

	t1 = dsa.x + dsb.x;
	e = t1 - dsa.x;
	t2 = ((dsb.x - e) + (dsa.x - (t1 - e))) + dsa.y + dsb.y;

	dsc.x = t1 + t2;
	dsc.y = t2 - (dsc.x - t1);
	return dsc;
}

vec2 ds_sub (vec2 dsa, vec2 dsb) {
	vec2 dsc;
	float e, t1, t2;

	 t1 = dsa.x - dsb.x;
	 e = t1 - dsa.x;
	 t2 = ((-dsb.x - e) + (dsa.x - (t1 - e))) + dsa.y - dsb.y;

	 dsc.x = t1 + t2;
	 dsc.y = t2 - (dsc.x - t1);
	 return dsc;
}

vec2 ds_mul (vec2 dsa, vec2 dsb) {
	vec2 dsc;
	float c11, c21, c2, e, t1, t2;
	float a1, a2, b1, b2, cona, conb, split = 8193.;

	cona = dsa.x * split;
	conb = dsb.x * split;
	a1 = cona - (cona - dsa.x);
	b1 = conb - (conb - dsb.x);
	a2 = dsa.x - a1;
	b2 = dsb.x - b1;

	c11 = dsa.x * dsb.x;
	c21 = a2 * b2 + (a2 * b1 + (a1 * b2 + (a1 * b1 - c11)));

	c2 = dsa.x * dsb.y + dsa.y * dsb.x;

	t1 = c11 + c2;
	e = t1 - c11;
	t2 = dsa.y * dsb.y + ((c2 - e) + (c11 - (t1 - e))) + c21;

	dsc.x = t1 + t2;
	dsc.y = t2 - (dsc.x - t1);

	return dsc;
}

void main() {
	ivec2 storePos = ivec2(gl_GlobalInvocationID.xy);

	if(renderMode.y == 1) {
		imageStore(ziTex, storePos, ivec4(-1));
		imageStore(zxTex, storePos, vec4(c.x));
		imageStore(zyTex, storePos, vec4(c.y));
		imageStore(zzTex, storePos, vec4(0.0));
		imageStore(zwTex, storePos, vec4(0.0));
	}

	vec2 pos = vec2(storePos);
	pos.x = ((((pos.x / size.x * 2.0) - 1.0) * aspect.x) + 1.0) / 2.0;
	pos.y = ((((pos.y / size.y * 2.0) - 1.0) * aspect.y) + 1.0) / 2.0;

	vec4 ptp = vec4(0.0);
	ptp.x = pos.x;
	ptp.y = pos.y;
	ptp.z = 0.0;
	ptp.w = 0.0;

	vec2 tpx = ds_mul(vec2(ptp.x, ptp.z), sc);
	vec2 tpy = ds_mul(vec2(ptp.y, ptp.w), sc);

	tpx = ds_add(tpx, vec2(off.x, off.z));
	tpy = ds_add(tpy, vec2(off.y, off.w));

	tpx = ds_mul(tpx, vec2(4.0, 0.0));
	tpy = ds_mul(tpy, vec2(4.0, 0.0));

	tpx = ds_sub(tpx, vec2(2.0, 0.0));
	tpy = ds_sub(tpy, vec2(2.0, 0.0));

	vec4 uv = vec4(tpx.x, tpy.x, tpx.y, tpy.y);

	vec4 z = vec4(imageLoad(zxTex, storePos).x, imageLoad(zyTex, storePos).x, imageLoad(zzTex, storePos).x, imageLoad(zwTex, storePos).x);
	int iteration = imageLoad(ziTex, storePos).x;

	if(iteration != -1) {
		return;
	}

	vec4 tz = vec4(z.x, z.y, z.z, z.w);
	vec2 x2 = vec2(0.0);
	vec2 y2 = vec2(0.0);

	float c11, c21, c2, e, t1, t2;
	float a1, a2, b1, b2, cona, conb, split = 8193.0;

	vec2 zx = vec2(0.0);
	vec2 zy = vec2(0.0);

	int itCount = maxIteration/renderMode.x;

	for (int i = 0; i < itCount; i++) {
		//x2 = tz.xz * tz.xz
		cona = tz.x * split;
		a1 = cona - (cona - tz.x);
		a2 = tz.x - a1;

		c11 = tz.x * tz.x;
		c21 = a2 * a2 + (a2 * a1 + (a1 * a2 + (a1 * a1 - c11)));

		c2 = tz.x * tz.z * 2.0;

		t1 = c11 + c2;
		e = t1 - c11;
		t2 = tz.z * tz.z + ((c2 - e) + (c11 - (t1 - e))) + c21;

		x2.x = t1 + t2;
		x2.y = t2 - (x2.x - t1);

		//y2 = tz.yw * tz.yw
		cona = tz.y * split;
		a1 = cona - (cona - tz.y);
		a2 = tz.y - a1;

		c11 = tz.y * tz.y;
		c21 = a2 * a2 + (a2 * a1 + (a1 * a2 + (a1 * a1 - c11)));

		c2 = tz.y * tz.w * 2.0;

		t1 = c11 + c2;
		e = t1 - c11;
		t2 = tz.w * tz.w + ((c2 - e) + (c11 - (t1 - e))) + c21;

		y2.x = t1 + t2;
		y2.y = t2 - (y2.x - t1);

		//zx = x2 - y2
		t1 = x2.x - y2.x;
		e = t1 - x2.x;
		t2 = ((-y2.x - e) + (x2.x - (t1 - e))) + x2.y - y2.y;

		zx.x = t1 + t2;
		zx.y = t2 - (zx.x - t1);

		//zx = zx + uv.xz
		t1 = zx.x + uv.x;
		e = t1 - zx.x;
		t2 = ((uv.x - e) + (zx.x - (t1 - e))) + zx.y + uv.z;

		zx.x = t1 + t2;
		zx.y = t2 - (zx.x - t1);

		//zy = tz.xz * tz.yw
		cona = tz.x * split;
		conb = tz.y * split;
		a1 = cona - (cona - tz.x);
		b1 = conb - (conb - tz.y);
		a2 = tz.x - a1;
		b2 = tz.y - b1;

		c11 = tz.x * tz.y;
		c21 = a2 * b2 + (a2 * b1 + (a1 * b2 + (a1 * b1 - c11)));

		c2 = tz.x * tz.w + tz.z * tz.y;

		t1 = c11 + c2;
		e = t1 - c11;
		t2 = tz.z * tz.w + ((c2 - e) + (c11 - (t1 - e))) + c21;

		zy.x = t1 + t2;
		zy.y = t2 - (zy.x - t1);

		//zy = zy * 2.0
		cona = zy.x * split;
		conb = 2.0 * split;
		a1 = cona - (cona - zy.x);
		b1 = conb - (conb - 2.0);
		a2 = zy.x - a1;
		b2 = 2.0 - b1;

		c11 = zy.x * 2.0;
		c21 = a2 * b2 + (a2 * b1 + (a1 * b2 + (a1 * b1 - c11)));

		c2 = zy.y * 2.0;

		t1 = c11 + c2;
		e = t1 - c11;
		t2 = ((c2 - e) + (c11 - (t1 - e))) + c21;

		zy.x = t1 + t2;
		zy.y = t2 - (zy.x - t1);

		//zy = zy + uv.yw
		t1 = zy.x + uv.y;
		e = t1 - zy.x;
		t2 = ((uv.y - e) + (zy.x - (t1 - e))) + zy.y + uv.w;

		zy.x = t1 + t2;
		zy.y = t2 - (zy.x - t1);

		z.x = zx.x;
		z.y = zy.x;
		z.z = zx.y;
		z.w = zy.y;

		tz.x = z.x;
		tz.y = z.y;
		tz.z = z.z;
		tz.w = z.w;

		if(x2.x + y2.x > 4.0) {
			iteration = i;
			imageStore(ziTex, storePos, ivec4(iteration + renderMode.z * itCount));
			break;
		}
	}

	imageStore(zxTex, storePos, vec4(z.x));
	imageStore(zyTex, storePos, vec4(z.y));
	imageStore(zzTex, storePos, vec4(z.z));
	imageStore(zwTex, storePos, vec4(z.w));
}

#version 130

varying vec3 N;
varying vec3 v;
varying vec4 clr;

void main(void)
{
	vec3 best_diff = vec3(0);
	float best_ml = 10000000;

	for (int i = 0; i < 5; i++) {
		vec3 L = normalize(gl_LightSource[i].position.xyz - v);
		vec4 Idiff = vec4(1.0 * max(dot(N,L), 0.0));
		Idiff = clamp(Idiff, 0.0, 1.0);
		float c = distance(gl_LightSource[i].position.xyz, v) / 40.0;
		float ml = clamp(1.0 - c*c, 0.0, 1.0);

		best_diff += Idiff.xyz * ml;
	}

//	gl_FragColor = vec4(clr.xyz * Idiff.xyz * ml * 0.4 + clr.xyz * 0.6, clr.a);
	gl_FragColor = vec4(clr.xyz * best_diff * 0.4 + clr.xyz * 0.6, clr.a);
//	gl_FragColor = vec4(clr.xyz * best_diff * 0.4 + clr.xyz * 0.6, min(clr.a, 0.3 + 0.7 * (1.0 - v.y / 20.0)));
}
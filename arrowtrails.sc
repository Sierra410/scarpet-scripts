__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

_distance(f, t) -> sqrt(sum(...map((t - f), _^2)));

_trail(prt, p1, p2, v) -> (
	particle(prt, p1 - v, 1, 0, 0);

	for(entity_area('player', p1, 16, 16, 16), (
		pp = pos(_);
		was = _distance(pp, p1);
		now = _distance(pp, p2);
		rate = (now - was) * 0.2;

		run(str(
			'playsound block.powder_snow.place player %s %f %f %f 1 %f',
			_, ...p2, 1 - rate,
		));
	));
);

_arrow_flies(e, v, p1, p2) -> (
	fire = query(e, 'fire');

	in_dimension(query(e, 'dimension'), if(fire > 0,
		_trail('flame', p1, p2, v),
		_trail('crit', p1, p2, v),
	));
);

_on_arrow(e, new) -> (
	if(!new, return());

	entity_event(e, 'on_move', '_arrow_flies')
);

entity_load_handler('arrow', '_on_arrow');
entity_load_handler('spectral_arrow', '_on_arrow');

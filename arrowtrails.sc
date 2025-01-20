__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

_owner(e) -> (
	entity_selector(
		str(
			'@p[nbt={UUID:%s}]',
			e~'nbt':'Owner'
		)
	):0
);

_arrow_flies(e, v, p1, p2) -> (
	fire = query(e, 'fire');

	if(fire > 0,
		particle_line('flame', p1, p2, 1),
		particle_line('crit', p1, p2, 1),
	);
);

_on_arrow(e, new) -> (
	if(!new, return());
	owner = _owner(e);

	if(owner == null, return());

	entity_event(e, 'on_move', '_arrow_flies')
);

entity_load_handler('arrow', '_on_arrow');
entity_load_handler('spectral_arrow', '_on_arrow');

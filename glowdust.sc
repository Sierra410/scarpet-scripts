__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'player',
};

global_face_offset = {
	'up' -> [0, 1, 0],
	'down' -> [0, -1, 0],
	'south' -> [0, 0, 1],
	'north' -> [0, 0, -1],
	'east' -> [1, 0, 0],
	'west' -> [-1, 0, 0],
};

_glowstone_dust(p, item, b, bp, face) -> (
	l = 1;
	if(
		air(b), null, // No-op
		block(b) == 'light', (
			l = number(block_state(b, 'level')) + 1;
			if(l > 15, return(null));
		),
		return(null);
	);

	set(b, 'light', {'level' -> l}); // Set light
	if(query(p, 'gamemode_id') != 1,
		inventory_set('equipment', p, 0, item:1-1), // Use 1 dust
	);
	particle( // Particles
		'wax_on',
		bp + [0.5, 0.5, 0.5] - (global_face_offset:face * 0.5),
		3, 0.2, 0
	);
	sound('minecraft:item.brush.brushing.sand', bp, 1, 1, 'block'); // Sound

	'cancel'
);

_brush(p, item, b, bp, face) -> (
	if(b != 'light', return(null));

	n = number(block_state(b, 'level'));
	spawn(
		'item', bp + [0.5, 0.5, 0.5],
		str('{Item:{id:"minecraft:glowstone_dust", Count:%d}}', n),
	);
	sound('minecraft:block.composter.ready', bp, 1, 1, 'block');

	set(b, 'air');

	'cancel'
);

__on_player_right_clicks_block(p, i, hand, block, face, hv) -> (
	if(hand != 'mainhand', return(null));

	item = inventory_get('equipment', p, 0);
	if(
		item:0 == 'glowstone_dust', f = '_glowstone_dust',
		item:0 == 'brush',          f = '_brush',
		return(null),
	);

	bp = pos(block) + global_face_offset:face;
	b = block(bp);

	call(f, p, item, b, bp, face)
);

_glimmer(x, y, z, p) -> (
	particle(
		'wax_off',
		x+0.4, y+0.4, z+0.4,
		1, 0.2, 0,
		p,
	);
);

global_glimmer_window = 30;

_glimmer_rng(x, y, z, p) -> (
	schedule(floor(rand(global_glimmer_window)), '_glimmer', x, y, z, p);
	schedule(floor(rand(global_glimmer_window)), '_glimmer', x, y, z, p);
);

_show_dust(p) -> (
	item = inventory_get('equipment', p, 0);
	if(item:0 != 'glowstone_dust', return(null));

	scan(pos(p), [10, 5, 10], (
		if(_ == 'light', _glimmer_rng(_x, _y, _z, p));
	));
);

_show_dust_loop() -> (
	p = player();
	if(p == null, return(null)); // Break loop if player isn't online
	schedule(global_glimmer_window, '_show_dust_loop');

	_show_dust(p);
);

__on_start() -> (
	// Start loop the moment script's loaded
	_show_dust_loop();
);

__on_player_connects(p) -> (
	// Re-start loop fore re-joining players
	_show_dust_loop();
);

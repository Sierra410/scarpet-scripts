__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
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
	sound('minecraft:item.brush.brushing.sand', bp, 1, 1, 'block'); // Sound
);

_brush(p, item, b, bp, face) -> (
	if(b != 'light', return(null));

	l = max(number(block_state(b, 'level')) - 1, 0);

	spawn(
		'item', bp + [0.5, 0.5, 0.5],
		'{Item:{id:"minecraft:glowstone_dust", Count:1}}',
	);

	if(l == 0,
		set(b, 'air'),
		set(b, 'light', {'level' -> l});
	);
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

	call(f, p, item, b, bp, face);
);

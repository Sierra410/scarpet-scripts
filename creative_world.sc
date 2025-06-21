__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'player',
	'libraries' -> [{
		'source' -> 'libchatter.sc'
	}],
};

import('libchatter',
	'action_msg',
	'chat_msg',
	'echo',
);

_run_as_at(p, ...cmd) -> (
	run(join(' ', 'execute as', p, 'at @s run', ...cmd))
);

_run_as_at_eyes(p, ...cmd) -> (
	run(join(' ', 'execute as', p, 'at @s anchored eyes run', ...cmd))
);

_update_block(b, state) -> (
	set(b, b, state);
	for(neighbours(b), update(_));

	for([[2, 0, 0], [-2, 0, 0], [0, 0, 2], [0, 0, -2]], (
		p = pos(b) + _;
		c = block(p);
		if(c == 'comparator', update(c));
	));
);

global_awl_leaves = 'oak_leaves'; // Auto-waterlogging leaves
global_waterlog_leaves = false;

global_bell_block = 'yellow_stained_glass';
global_bell_last_rang = 0;
global_bell = null;
global_bell_powered = false;

global_place_replacing = false;

_bell_track(b) -> (
	if(global_bell == null, schedule(0, '_bell_tick'));

	global_bell_last_rang = tick_time();
	global_bell = pos(b);

	action_msg(player(), 'Tracking the bell at ', pos(b));
);

_bell_show_ticks() -> (
	action_msg(
		player(),
		format('br '+power(global_bell)),
		'  ',
		tick_time() - global_bell_last_rang,
	);
);

_bell_tick() -> (
	if(block(global_bell) != global_bell_block;, (
		action_msg(player(), 'No longer tracking the bell at ', global_bell);
		global_bell = null;
		return();
	));

	schedule(0, '_bell_tick');

	powered = power(global_bell) > 0;
	if(powered == global_bell_powered, return());

	global_bell_powered = powered;
	if(powered, (
		_bell_show_ticks();
		global_bell_last_rang = tick_time();
	));
);

__on_player_right_clicks_block(p, i, h, b, face, hitvec) -> (
	if(
		h != 'mainhand'
		|| query(p, 'gamemode_id') != 1,
			return()
	);

	if(
		!query(p, 'sneaking'),
		_magic_clicks(p, i, h, b, face, hitvec)
	) || if(
		global_place_replacing,
		_place_replacing(p, i, h, b, face, hitvec)
	)
);

global_must_support = [
	'redstone_wire',
	'redstone_torch',
	'repeater',
	'comparator',
];

_must_be_supported(b) -> (
	(global_must_support ~ b) != null
	|| block_tags(b, 'pressure_plates')
	|| block_tags(b, 'rails')
	|| (
		(b == 'lever' || block_tags(b, 'buttons'))
		&& block_state(b, 'face') == 'floor'
	)
);

global_inverted_facing = [
	'dropper',
	'dispenser',
	'piston',
	'sticky_piston',
];

_has_inverted_facing(b) -> (
	(global_inverted_facing ~ b) != null
);

_which_half(b, hitvec) -> (
	above = block(pos(b) + [0, 1, 0]);
	if(
		hitvec:1 > 0.5 || _must_be_supported(above),
		'top',
		'bottom',
	)
);

__on_player_swaps_hands(p) -> (
	h = query(p, 'holds', 'mainhand');

	if(h == null, return());

	try(
		nb = block(h:0),
		'unknown_block',
		return(),
	);

	global_place_replacing = !global_place_replacing;
	action_msg(p, 'Block-replacing mode: ', global_place_replacing);

	'cancel';
);

_place_replacing(p, i, h, b, face, hitvec) -> (
	if(i == null, return());

	try(
		nb = block(i:0),
		'unknown_block',
		return(),
	);

	state = {};

	// Special case
	if(nb == 'target' && b == 'target',
		set(b, 'iron_block', state);
		return('cancel');
	);

	if(
		block_tags(nb, 'slabs'), (
			if(block_tags(b, 'slabs'),
				state:'type' = block_state(b):'type',
				state:'type' = _which_half(b, hitvec),
			)
		),
		block_tags(nb, 'stairs'), (
			state:'half' = _which_half(b, hitvec);
		)
	);

	if((block_state(nb)~'facing') != null, (
		order = if(_has_inverted_facing(nb), -1, 0);
		state:'facing' = query(p, 'facing', order);
	));

	set(b, nb, state);

	'cancel'
);

_magic_clicks(p, i, h, b, face, hitvec) -> (
	if(
		// Cycle composter level
		i:0 == 'composter' && b == 'composter', (
			l = number(block_state(b, 'level'));

			l = (l+1) % 9;
			if(l == 7, l = 8); // 7 always transforms to 8 anyway

			_update_block(b, {'level'->l});
			action_msg(p, l);
		),
		(b ~'copper_bulb$') != null, (
			l = bool(block_state(b, 'lit'));
			_update_block(b, {'lit'->!l});
		),
		b == global_bell_block, (
			if(global_bell != pos(b),
				_bell_track(b),
				_bell_show_ticks(),
			);
		),
		return(); // No cancel
	);

	'cancel'
);

__on_player_places_block(p, i, h, b) -> (
	if(query(p, 'gamemode_id') != 1,
		return(),
	);

	if(
		b == global_awl_leaves && global_waterlog_leaves,
			_update_block(b, {'waterlogged'->true}),
	);
);

__on_player_breaks_block(p, b) -> (
	if(query(p, 'gamemode_id') != 1,
		return(),
	);

	if(b == global_awl_leaves && global_waterlog_leaves,
		set(b, b, {'waterlogged'->'false'}),
	);
);

entity_load_handler('item', _(e, new) -> (
	if(!new, return());

	thrower = e~'nbt':'Thrower';
	if(thrower == null, return());

	p = entity_selector(str('@p[nbt={UUID:%s}]', thrower)):0;
	if(p == null, return());

	item = query(e, 'item'):0;
	if(
		item == 'structure_block', (
			_run_as_at_eyes(p, 'fill ^ ^ ^2 ^ ^ ^2 iron_block replace #air');
		),
		item == 'jigsaw', (
			d = 100;

			l = query(p, 'look');
			r = query(
				p, 'trace', d,
				'exact', 'blocks', 'liquids', 'entities',
			);

			if(r != null,
				_run_as_at(p, 'tp @s', ...(r - l)),
				_run_as_at(p, 'tp @s ^ ^ ^'+d),
			);
		),
		item == global_awl_leaves, (
			global_waterlog_leaves = !global_waterlog_leaves;
			action_msg(p, 'Auto-waterlog leaves: ', global_waterlog_leaves);

			if(query(p, 'holds') == null,
				inventory_set('equipment', p, 0, 1, global_awl_leaves),
			);
		),
		return();
	);

	modify(e, 'kill');
));

// _benchmark(func) -> (
// 	t0 = time();
// 	call(func, 1000000);
// 	_say('took', time() - t0);
// );

// __on_start() -> (
// 	b = block(-178, 94, -11);

// 	_say(bool(b~'_leaves$'));
// 	_benchmark(_(outer(b), n)->(
// 		for(range(n), bool(b~'_leaves$'));
// 	));

// 	_say(_in_list(global_leaves, b));
// 	_benchmark(_(outer(b), n)->(
// 		for(range(n), _in_list(global_leaves, b));
// 	));

// 	_say((global_leaves ~ b) != null);
// 	_benchmark(_(outer(b), n)->(
// 		for(range(n), (global_leaves ~ b) != null);
// 	));
// );

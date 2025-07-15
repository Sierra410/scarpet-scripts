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
	'player_msg',
	'msg',
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

global_place_replacing_name = [
	'Off',
	'On',
	'W/O updates',
];
global_place_replacing = 0;

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
		|| query(p, 'gamemode_id') != 1
		|| query(p, 'sneaking'),
			return()
	);

	_magic_clicks(p, i, h, b, face, hitvec) || if(
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

_cant_replace_blocks(b) -> (
	b == 'lever'
	|| block_tags(b, 'buttons')
	|| _must_be_supported(b)
);

_can_replace(b, nb) -> (
	_cant_replace_blocks(b) == _cant_replace_blocks(nb)
);

global_faces_player_when_placed = [
	'dropper',
	'dispenser',
	'piston',
	'sticky_piston',
	'observer',
];

_faces_player_when_placed(b) -> (
	(global_faces_player_when_placed ~ b) != null
);

_facing_horizontal_only(p, reverse) -> (
	for((reverse && range(-1, -7, -1) || range(6)),
		f = query(p, 'facing', _);

		if(f != 'up' && f != 'down', return(f));
	)
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

	global_place_replacing = (global_place_replacing+1)%3;
	action_msg(
		p, 'Block-replacing mode:',
		['%%%l', '%%%y', '%%%d']:global_place_replacing,
		global_place_replacing_name:global_place_replacing,
	);

	'cancel';
);

_set_with_mode_respect(p, b, s) -> (
	if(global_place_replacing == 2,
		without_updates(set(p, b, s)),
	global_place_replacing == 1,
		set(p, b, s),
	)
);

_place_replacing(p, i, h, b, face, hitvec) -> (
	if(i == null, return());

	try(
		nb = block(i:0),
		'unknown_block',
		return(),
	);

	if(!_can_replace(b, nb), return());

	state = {};

	if(
		nb == 'target' && b == 'target', (
			_set_with_mode_respect(b, 'iron_block', state);
			return('cancel');
		),
		block_tags(nb, 'slabs'), (
			if(block_tags(b, 'slabs'),
				state:'type' = block_state(b):'type',
				state:'type' = _which_half(b, hitvec),
			)
		),
		block_tags(nb, 'stairs'), (
			state:'half' = _which_half(b, hitvec);
			state:'facing' = _facing_horizontal_only(p, false);
		),
		block_tags(nb, 'trapdoors'), (
			if(block_tags(b, 'trapdoors'), (
				_set_with_mode_respect(b, nb, block_state(b));
			), (
				state:'facing' = _facing_horizontal_only(p, true);
				state:'half' = _which_half(b, hitvec);
				_set_with_mode_respect(b, nb, state);
			));

			return('cancel');
		),
		_faces_player_when_placed(nb), (
			state:'facing' = query(p, 'facing', -1);
		),
		block_tags(nb, 'beds'), (
			// Beds can only replace beds
			// Trying to replace any random block doesn't really work
			if(!block_tags(b, 'beds'), return());

			facing = block_state(b, 'facing');
			if(block_state(b, 'part') == 'foot', (
				off = 1;
				part_this = 'foot';
				part_other = 'head';
			),(
				off = -1;
				part_this = 'head';
				part_other = 'foot';
			));

			without_updates((
				_set_with_mode_respect(
					b, nb,
					{'facing'->facing, 'part'->part_this},
				);

				_set_with_mode_respect(
					pos_offset(b, facing, off), nb,
					{'facing'->facing, 'part'->part_other},
				);
			));

			return('cancel');
		),
	);

	_set_with_mode_respect(b, nb, state);

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

global_switchable = [
	['iron_block', 'white_stained_glass'],
	['piston', 'sticky_piston'],
	['dropper', 'dispenser'],
	['repeater', 'comparator'],
	['redstone', 'redstone_torch']
];

_switch_holding_block(p, i) -> (
	for(global_switchable, (
		ind = _ ~ i;
		if(ind != null, (
			inventory_set('equipment', p, 0, 1, _:(ind+1));
			return(true);
		));
	));

	false // Didn't switch
);

_on_new_entity(e, new) -> (
	if(!new, return());

	thrower = e~'nbt':'Thrower';
	if(thrower == null, return());

	p = entity_selector(str('@p[nbt={UUID:%s}]', thrower)):0;
	if(p == null, return());

    if(query(p, 'gamemode_id') != 1, return());

	if(query(p, 'sneaking'), return());

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
		!_switch_holding_block(p, item), (
			return()
		),
	);

	modify(e, 'kill');
);

entity_load_handler('item', '_on_new_entity');

// _benchmark(func) -> (
// 	t0 = time();
// 	call(func, 1000000);
// 	echo('took', time() - t0);
// );

// __on_start() -> (
// 	b = 'comparator';

// 	_benchmark(_(outer(b), n)->(
// 		for(range(n), ([
// 			'redstone_wire',
// 			'redstone_torch',
// 			'repeater',
// 			'comparator',
// 		] ~ b) != null);
// 	));

// 	_benchmark(_(outer(b), n)->(
// 		for(range(n), (global_must_support ~ b) != null);
// 	));
// );

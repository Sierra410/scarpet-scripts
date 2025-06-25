__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'global',
};

global_looking_at = {};

__on_tick() -> (
	for(player('all'), _on_tick_player(_));
);

__on_player_disconnects(p, r) -> (
	delete(global_looking_at, p);
);

_on_tick_player(p) -> (
	looking_at = query(p, 'trace');
	if(type(looking_at) != 'block',
		looking_at = null;
	);

	if(
		looking_at != null && (
			(global_looking_at:p == null)
			|| pos(global_looking_at:p) != pos(looking_at)
		), (
			_on_looked_at_block(p, looking_at);
		)
	);

	global_looking_at:p = looking_at;
);

_on_looked_at_block(p, b) -> (
	name = block_data(b):'CustomName';
	if(name == null, return());

	_show_block_name(p, name);
);

_show_block_name(p, name) -> (
	display_title(p, 'actionbar', format('b '+parse_nbt(name)));
);

__on_player_swaps_hands(p) -> (
	if(_can_rename(global_looking_at:p),
		_rewrite_name(p, global_looking_at:p);
		'cancel'
	)
);

global_can_rename = [
	'dispenser',
	'dropper',
	'crafter',
	'hopper',
	'chest',
	'barrel',
	'furnace',
	'trapped_chest',
];

_can_rename(b) -> (
	(global_can_rename ~ b) != null
);

_rename_block(b, n) -> (
	run(str(
		'data merge block %d %d %d {CustomName:\'%s\'}',
		...pos(b),
		n,
	));
);

_get_chest_second_half(b) -> (
	if(b != 'chest', return(null));

	t = block_state(b):'type';
	if(
		t == 'left', d = 1,
		t == 'right', d = -1,
		return(null)
	);

	f = block_state(b):'facing';
	if(
		f == 'north', o = [1*d, 0, 0],
		f == 'south', o = [-1*d, 0, 0],
		f == 'east', o = [0, 0, 1*d],
		f == 'west', o = [0, 0, -1*d],
		return(null),
	);

	nb = block(pos(b) + o);
	if(nb == 'chest',
		nb,
		null,
	)
);

_rewrite_name(p, b) -> (
	_on_ui_interact(s, p, a, d, outer(b)) -> (
		if(d:'slot' != 2, return('cancel'));

		if(a == 'slot_update', (
			schedule(0, _(outer(s)) -> (
				screen_property(s, 'level_cost', 0);
			));
		),
		a == 'pickup' || a == 'quick_move', (
			i = inventory_get(s, 2);
			if(i == null, return('cancel'));

			nn = i:2:'components':'minecraft:custom_name';

			_rename_block(b, nn);
			sh = _get_chest_second_half(b);
			if(sh != null,
				_rename_block(sh, nn);
			);

			sound(
				'minecraft:entity.villager.work_cartographer',
				pos(p),
			);

			close_screen(s);
		));

		'cancel'
	);

	s = create_screen(
		p, 'anvil', 'Renaming',
		'_on_ui_interact',
	);

	old = block_data(b):'CustomName';

	inventory_set(
		s, 0, 1, b, if(old != null, str(
			'{components:{"minecraft:custom_name":\'%s\'},count:1,id:"%s"}',
			old,
			b,
		)),
	);
);

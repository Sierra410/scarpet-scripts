__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'player',
};

__on_start() -> (
	schedule(rand(20), '_tick');
);

global_looking_at = null;

_tick() -> (
	p = player();
	if(p == null, return());

	looking_at = query(p, 'trace');
	if(type(looking_at) != 'block',
		looking_at = null;
	);

	if(
		looking_at != null && (
			(global_looking_at == null)
			|| pos(global_looking_at) != pos(looking_at)
		), (
			_on_looked_at_block(p, looking_at);
		)
	);

	global_looking_at = looking_at;

	schedule(0, '_tick');
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
	h = query(p, 'holds', 'mainhand');

	if(h == null && _can_rename(global_looking_at),
		_rewrite_name(p, global_looking_at);
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

			run(str(
				'data merge block %d %d %d {CustomName:\'%s\'}',
				...pos(b),
				nn,
			));

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

__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

__on_player_uses_item(p, i, h) -> (
	if(i:0 != 'slime_ball', return());

	at = pos(p) + [0, p~'eye_height', 0];
	dir = p~'look';

	_launch_slimeball(at, dir, p);

	if(p~'gamemode_id' != 1,
		inventory_set(
			'equipment', p,
			if(h == 'mainhand', 0, 5),
			i:1 - 1
		),
	);
);

_launch_slimeball(at, dir, by) -> (
	sound('entity.slime.attack', at);
	uuid = by~'nbt':'UUID';
	e = spawn(
		'snowball', at,
		nbt('{Item:{id:"minecraft:slime_ball"},Owner:'+uuid+'}'),
	);
	modify(e, 'motion', dir);
	entity_event(e, 'on_removed', '_on_slimeball_hit');
);

_on_slimeball_hit(e) -> (
	at = pos(e);

	if(in_slime_chunk(at),
		spawn(
			'slime', at,
			nbt('{Size:0,DeathLootTable:"minecraft:empty"}'),
		),
		spawn(
			'item', at,
			nbt('{Item:{id:"minecraft:slime_ball",Count:1}}'),
		)
	);

	sound('entity.slime.squish', at);
);

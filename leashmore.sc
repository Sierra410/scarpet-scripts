__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'player',
};

_leash(master, pet) -> (
	run(str(
		'data modify entity %s leash.UUID set from entity %s UUID',
		query(pet, 'uuid'),
		query(master, 'uuid')
	))
);

global_leashable = [
	'villager'
];

__on_player_interacts_with_entity(p, e, h) -> (
	if((e~'type' ~ global_leashable) == null, return());
	if(e~'nbt':'leash', return());

	hand = null;
	for([0, -1], (
		item = inventory_get('equipment', player(), _);
		if(item:0 == 'lead', (
			hand = _;
			break();
		));
	));

	if(hand == null, return());

	_leash(p, e);
	if(query(p, 'gamemode_id') != 1,
		inventory_set('equipment', player(), hand, item:1-1);
	);

	'cancel'
);

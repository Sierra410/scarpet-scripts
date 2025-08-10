__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
    'command_permission' -> 'all',
    'commands' -> {
        '' -> _() -> _hat(player()),
    },
};

global_lore = 'But A Hat!';
global_lore_nbt = '"'+global_lore+'"';

_is_a_hat(i) -> (
	lore = i:2:'components':'minecraft:lore';
	parse_nbt(lore) == [global_lore_nbt]
);

_make_hat(p, i) -> (
	if(i:1 > 1,
		return(format('r At most 1 item can be turned into a hat.'))
	);

	nbt = parse_nbt(i:2);
	comps = nbt:'components' || {};
	nbt:'components' = comps; // In case components was null

	if(
		has(comps, 'minecraft:lore'),
		return(format('y This item is too unique to make it into a hat!'))
	);

	comps:'minecraft:equippable' = {
    	'slot' -> 'head'
	};

	comps:'minecraft:lore' = [
		global_lore_nbt,
	];

	comps:'minecraft:max_stack_size' = 1;

	inventory_set('equipment', p, 0, i:1, i:0, encode_nbt(nbt));

	di = item_display_name(i:0);
	format(str('im %s, But A Hat!', di))
);

_unmake_hat(p, i) -> (
	nbt = parse_nbt(i:2);
	comps = nbt:'components';

	delete(comps, 'minecraft:lore');
	delete(comps, 'minecraft:equippable');
	delete(comps, 'minecraft:max_stack_size');

	inventory_set('equipment', p, 0, i:1, i:0, encode_nbt(nbt));

	di = item_display_name(i:0);
	format(str('iy %s... Just %s.', di, di))
);

_hat(p) -> (
	i = inventory_get('equipment', p, 0);
	if(i == null, return());


	print(p, if(_is_a_hat(i),
		_unmake_hat(p, i),
		_make_hat(p, i)
	));
);

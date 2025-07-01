__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
    'command_permission' -> 'ops',
    'commands' -> {
		'<from> <to> <content>' -> _(f, t, c) -> _setinv_volume(f, t, c),
    },
    'arguments' -> {
        'from' -> {
            'type' -> 'pos',
        },
        'to' -> {
            'type' -> 'pos',
        },
		'content' -> {
			'suggest' -> [
				'stone; mud 2; chest[container=[{item:{id:tnt,count:1},slot:0}]] 1',
			],
			'type' -> 'text',
		}
    },
};

import('libchatter',
	'player_msg',
	'msg',
);

_update_comparators(b) -> (
	for(neighbours(b), (
		c = block(_);
		if(c == 'comparator', update(c));
	));

	for([[2, 0, 0], [-2, 0, 0], [0, 0, 2], [0, 0, -2]], (
		c = block(pos(b) + _);
		if(c == 'comparator', update(c));
	));
);

_fmt_item(s, i) -> (
	if(
		t == 'string',
			{'Slot' -> s, 'id' -> i, 'count' -> 1},
		t == 'list',
			{'Slot' -> s, 'id' -> i:0, 'count' -> i:1},
		t == 'map', i,
		throw('Invalid type')
	)
);

_parse_items(s) -> (
	items = map(split(';', s), (
		args = split(' +', _);
		args = filter(args, _);

		l = length(args);
		if(
			l == 1, [1, args:0, null],
			l == 2, [number(args:1) || 1, args:0, null],
			l == 3, [number(args:1) || 1, args:0, args:2],
			throw('Invalid item tuple')
		)
	));
);

_inv_add(inv, i) -> (
	free = inventory_find(inv, null);
	if(free != null, (
		inventory_set(inv, free, ...i);
		1
	),
		0
	)
);

_inv_add_list(inv, l) -> (
	sum(...map(l,
		_inv_add(inv, _)
	))
);

_setinv_volume(f, t, c) -> (
	items = _parse_items(c);

	found = 0;
	affected = 0;
	partial = 0;

	volume(f, t, (
		if(inventory_size(_) < 1, continue());
		found += 1;

		n = _inv_add_list(_, items);
		if(n > 0, affected += 1);
		if(n != length(items), partial += 1);
	));

	player_msg(
		player(),
		found, 'found,', affected, 'affected,', partial, 'partial.',
	);
);

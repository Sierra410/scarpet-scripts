__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
    'command_permission' -> 'ops',
    'commands' -> {
        '<from>' -> _(f) -> _clearinv_volume(f, f),
		'<from> <to>' -> _(f, t) -> _clearinv_volume(f, t),
    },
    'arguments' -> {
        'from' -> {
            'type' -> 'pos',
        },
        'to' -> {
            'type' -> 'pos',
        },
    },
};

import('libchatter',
	'chat_msg',
	'say',
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

_clearinv(target) -> (
	ret = [0, 0]; // slot, item count

	is = inventory_size(target);
	if(is > 0, for(range(is),
		was = inventory_set(target, _, 0);
		if(was != null, (
			ret:0 += 1;
			ret:1 += was:1;
		));
	));

	if(ret != [0, 0], (
		_update_comparators(target);
		ret
	))
);

_clearinv_volume(f, t) -> (
	b = 0; // blocks affected
	s = 0; // slots
	i = 0; // items

	volume(f, t, (
		cleared = _clearinv(_);
		if(b == null, continue());

		b += 1;
		s += cleared:0;
		i += cleared:1;
	));

	bs = if(b == 1, '', 's');
	ss = if(s == 1, '', 's');
	is = if(i == 1, '', 's');

	chat_msg(
		player(),
		b, '%%%b', ' block', bs, ' cleared\n  ',
		'%%%g', '(', i, ' item', is, ' removed from ', s, ' slot', ss, ')',
	);
);

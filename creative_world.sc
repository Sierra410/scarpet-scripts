__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

_say(...args) -> (
	print(player('all'), join(' ', args));
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
);

_sfmt(arg) -> (
	t = type(arg);
	if(t == 'bool',
		arg && format('bl True') || format('br False'),
		str(arg),
	)
);

_action_msg(p, ...msg) -> (
	display_title(p, 'actionbar', sum(...map(msg, _sfmt(_))), 0, 10, 0);
);

// Magic actions on RMB
__on_player_right_clicks_block(p, i, h, b, face, hitvec) -> (
	if(
		h != 'mainhand'
		|| query(p, 'gamemode_id') != 1
		|| query(p, 'sneaking'),
		return(),
	);

	if(
		// Cycle composter level
		i:0 == 'composter' && b == 'composter', (
			l = number(block_state(b, 'level'));

			l = (l+1) % 9;
			if(l == 7, l = 8); // 7 always transforms to 8 anyway

			_update_block(b, {'level'->l});
			_action_msg(p, l);
		),
		(b ~'copper_bulb$') != null, (
			l = bool(block_state(b, 'lit'));
			_update_block(b, {'lit'->!l});
		),
		return(); // No cancel
	);

	'cancel'
);

global_awl_leaves = 'oak_leaves'; // Auto-waterlogging leaves
global_waterlog_leaves_for = {};
__on_player_places_block(p, i, h, b) -> (
	if(query(p, 'gamemode_id') != 1,
		return(),
	);

	if(b == global_awl_leaves && global_waterlog_leaves_for:p,
		_update_block(b, {'waterlogged'->true}),
	);
);

__on_player_breaks_block(p, b) -> (
	if(query(p, 'gamemode_id') != 1,
		return(),
	);

	if(b == global_awl_leaves && global_waterlog_leaves_for:p,
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
			waterlog = !global_waterlog_leaves_for:p;
			global_waterlog_leaves_for:p = waterlog;
			_action_msg(p, 'Auto-waterlog leaves: ', waterlog);

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

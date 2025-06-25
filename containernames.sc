__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'global',
	'event_priority' -> 1000,
};

global_mc_colors = {
	'black' -> 'k',
	'dark_blue' -> 'v',
	'dark_green' -> 'e',
	'dark_aqua' -> 'q',
	'dark_red' -> 'n',
	'dark_purple' -> 'p',
	'gold' -> 'd',
	'gray' -> 'g',
	'dark_gray' -> 'f',
	'blue' -> 't',
	'green' -> 'l',
	'aqua' -> 'c',
	'red' -> 'r',
	'light_purple' -> 'm',
	'yellow' -> 'y',
	'white' -> 'w',
};

_mc_color_to_format_color(c) -> (
	fc = global_mc_colors:c;
	if(fc != null, return(fc));

	c = upper(c);
	c ~ '^#[0-9A-F]{6}$' || ''
);

global_scarpet_colors = {
	'k' -> 'black',
	'v' -> 'dark_blue',
	'e' -> 'dark_green',
	'q' -> 'dark_aqua',
	'n' -> 'dark_red',
	'p' -> 'dark_purple',
	'd' -> 'gold',
	'g' -> 'gray',
	'f' -> 'dark_gray',
	't' -> 'blue',
	'l' -> 'green',
	'c' -> 'aqua',
	'r' -> 'red',
	'm' -> 'light_purple',
	'y' -> 'yellow',
	'w' -> 'white',
};

_txtc_fmt(c, def_color) -> (
	// Note: this fomratter is meant for this app specifically.
	// Bold/italic/etc. value are ignored. The resulting text is always bold.

	t = type(c);
	if(
		t == 'string', (
			format(def_color + 'b ' + c)
		),
		t == 'map', (
			color = _mc_color_to_format_color(c:'color') || def_color;
			text = c:'text' || '';

			x = c:'extra';
			extra = if(
				type(x) == 'list',
				sum(...map(x, _txtc_fmt(_, color))),
				'',
			);


			format(color + 'b ' + text) + extra
		),
		t == 'list', (
			sum(...map(c, _txtc_fmt(_, def_color)))
		),
		throw('Fucked text component type')
	)
);

_text_component_to_format(c) -> (
	_txtc_fmt(decode_json(c), 'w')
);

_format_into_text_component(s) -> (
	encode_json(if(
		s ~ '^ ', slice(s, 1),
		pfx = s ~ '^([kveqnpdgftlcrmyw]|#[0-9A-F]{6})?(?= )', {
			'text' -> slice(s, length(pfx)+1),
			'color' -> global_scarpet_colors:pfx || pfx,
		},
		s,
	))
);

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
	if(_can_rename(b),
		_show_block_name(p, b)
	);
);

_show_block_name(p, b) -> (
	name = block_data(b):'CustomName';
	if(name == null, return());

	display_title(
		p, 'actionbar',
		_text_component_to_format(name)
	);
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
		'data merge block %d %d %d {CustomName:%s}',
		...pos(b),
		escape_nbt(n),
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
			if(nn != null, (
				// custom_name is always a string here, so passing it to
				// _format_into_text_component is fine
				nn = _format_into_text_component(decode_json(nn));

				print(player('all'), str('nn: "%s"', nn));
			));

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
			'{components:{"minecraft:custom_name":%s},count:1,id:"%s"}',
			escape_nbt(old),
			b,
		)),
	);
);

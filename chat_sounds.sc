__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
	'commands' -> {
		'' -> _()
			-> _print_user_settings(player()),

		'reset all' -> _()
			-> _set_user_setting_cmd(player(), null, null),

		'reset <type>' -> _(t)
			-> _set_user_setting_cmd(player(), t, null),

		'mute <type>' -> _(t)
			-> _set_user_setting_cmd(player(), t, ['Muted', 0, 0]),

		'set selfping <bool>' -> _(b)
			-> _set_user_setting_cmd(player(), 'selfping', b),

		'set <type> <sound> <volume>' -> _(t, s, v)
			-> _set_user_setting_cmd(player(), t, [s, v, 1]),

		'set <type> <sound> <volume> <pitch>' -> _(t, s, v, p)
			-> _set_user_setting_cmd(player(), t, [s, v, p]),
    },
    'arguments' -> {
        'sound' -> {
            'type' -> 'sound',
        },
		'type' -> {
            'type' -> 'term',
            'options' -> ['chat', 'join', 'leave'],
        },
		'bool' -> {
			'type' -> 'bool',
		},
		'volume' -> {
            'type' -> 'float',
            'min' -> 0,
            'max' -> 1,
			'suggest' -> [],
        },
        'pitch' -> {
            'type' -> 'float',
            'min' -> 0.5,
            'max' -> 2,
			'suggest' -> [],
        },
    }
};

global_user_settings = {};

_load_user_settings() -> (
    global_user_settings = read_file('user_settings', 'json') || {};
);

_save_user_settings() -> (
	write_file('user_settings', 'json', global_user_settings);
);

_set_user_setting(p, key, value) -> (
	uuid = query(p, 'uuid');

	us = get(global_user_settings, uuid) || {};

	if(value != null,
		put(us, key, value),
		delete(us, key),
	);

	if(us,
		put(global_user_settings, uuid, us),
		delete(global_user_settings, uuid),
	);

	_save_user_settings();
);

_reset_user_settings(p) -> (
	uuid = query(p, 'uuid');

	delete(global_user_settings, uuid);

	_save_user_settings();
);

_get_user_setting(p, key) -> (
	uuid = query(p, 'uuid');

	get(global_user_settings, uuid, key)
		|| get(global_default_user_settings, key)
);

global_default_user_settings = {
	'selfping' -> true,
	// Sound, Volume, Pitch
	'chat' -> ['minecraft:block.amethyst_cluster.fall', 1, 2],
	'join' -> ['minecraft:entity.player.levelup', 0.5, 2],
	'leave' -> ['minecraft:entity.arrow.hit_player', 0.1, 0.5],
};

fmt_sound(s) -> (
	format(
		str('y "%s" ', replace_first(s:0, '^minecraft:')),
		'&' + join(' ', s:0, s:1, s:2),
		'^ Click to copy',
		'd (🕪:',
		str('c %.2f', s:1),
		'^ Volume',
		'd , 🎚:',
		str('c %.2f', s:2),
		'^ Pitch',
		'd )',
	)
);

_print_user_settings(p) -> (
	sp = _get_user_setting(p, 'selfping');
	chat = _get_user_setting(p, 'chat');
	join = _get_user_setting(p, 'join');
	leave = _get_user_setting(p, 'leave');

	print(p,
		format('b Chat Sound Settings:') +
		format('l \n  Chat: ') + fmt_sound(chat) +
		format('l \n  Join: ') + fmt_sound(join) +
		format('l \n  Leave: ') + fmt_sound(leave) +
		format('l \n  Self-Ping: ') + sp,
	);
);

_set_user_setting_cmd(p, key, value) -> (
	if(key == null,
		_reset_user_settings(),
		_set_user_setting(p, key, value);
	);

	_print_user_settings(p);
);

_play_global_sound(type, ...skip_players) -> (
	for(player('all'), (
		if(skip_players~_, continue());

		s = _get_user_setting(_, type);
		if(s:1 > 0,
			pos = join(' ', pos(_));
			run(str(
				'playsound %s master %s %s %f %f %f',
				   s:0,   _,      pos, s:1,    s:2,   s:1,
				// Sound, player, pos, volume, pitch, minVolume
			));
		);
	));
);

__on_player_message(p, m) -> (
    skip = [];
	if(!_get_user_setting(p, 'selfping'),
		skip += p;
	);

	_play_global_sound('chat', ...skip);
);

__on_player_connects(p) -> (
	_play_global_sound('join', p);
);

__on_player_disconnects(p, r) -> (
	_play_global_sound('leave');
);

__on_start() -> (
	_load_user_settings();
);

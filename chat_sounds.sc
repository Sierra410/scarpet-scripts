__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
	'commands' -> {
		'' ->                             _() ->        _print_user_setting(player()),
		'reset' ->                        _() ->        _del_user_setting(player()),
		'mute' ->                         _() ->        _set_user_setting(player(), 'None', 0, 0),
		'set <sound> <volume>' ->         _(s, v) ->    _set_user_setting(player(), s, v, 1),
		'set <sound> <volume> <pitch>' -> _(s, v, p) -> _set_user_setting(player(), s, v, p),
    },
    'arguments' -> {
        'sound' -> {
            'type' -> 'sound',
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
//                      Sound, Volume, Pitch
global_default_sound = ['minecraft:block.amethyst_cluster.fall', 1, 2];

fmt_sound(s) -> (
	format(
		str('y "%s" ', replace_first(s:0, '^minecraft:')),
		'd (🕪:',
		str('c %.2f', s:1),
		'^ Volume',
		'd , 🎚:',
		str('c %.2f', s:2),
		'^ Pitch',
		'd )',
	)
);

_load_user_settings() -> (
    global_user_settings = read_file('user_settings', 'json') || {};
);

_save_user_settings() -> (
	write_file('user_settings', 'json', global_user_settings);
);

_set_user_setting(p, sound, vol, pitch) -> (
	put(global_user_settings, query(p, 'uuid'), [sound, vol, pitch]);
	_save_user_settings();
	_print_user_setting(p);
);

_del_user_setting(p) -> (
	delete(global_default_sound, query(p, 'uuid'));
	_save_user_settings();
	_print_user_setting(p);
);

_get_user_setting(p) -> (
	get(global_user_settings, query(p, 'uuid')) || global_default_sound;
);

_print_user_setting(p) -> (
	s = _get_user_setting(p);
	print(p, format('l Using ') + fmt_sound(s));
);

__on_player_message(p, m) -> (
    for(player('all'), (
		if(_ == p, continue()); // Don't notify the sender

		s = _get_user_setting(_);
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

__on_start() -> (
	_load_user_settings();
);

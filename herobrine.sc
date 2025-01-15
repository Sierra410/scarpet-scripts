__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
    'command_permission' -> 'ops',
    'commands' -> {
        '<players> <message>' -> _(p, m) -> _herobrine_say(p, m),
    },
    'arguments' -> {
		'players' -> {
            'type' -> 'players',
        },
        'message' -> {
            'type' -> 'text',
			'suggest' -> [],
        },
    },
};

_mark_player(p) -> (
	put(global_marked_players, query(p, 'uuid'), true);
	write_file('marked_players', 'json', global_marked_players);
);

_is_marked(p) -> (
	has(global_marked_players, query(p, 'uuid'))
);

_vector_len2(vec) -> (
	(sum(...map(vec, _*_)))
);

_herobrine_say(to, fmt, ...args) -> (
	print(to, format(
		' <',
		'y Herobrine',
		'^ Herobrine\nType: §kGhost§r\n00000000-0000-0000-0000-000000000000',
		str(' > '+fmt, args),
	));
);

_herobrine_warn(p, d) -> (
	_herobrine_say(
		p,
		'We are aware of your freecam cheats.',
		floor(d),
	);
);

_herobrine_greets(p) -> (
	_herobrine_say(p, 'Hello, dearest player.');
);

__on_player_clicks_block(p, b, f) -> (
	d = _vector_len2((pos(b) - pos(p)));
	if(d > 10000 && !_is_marked(p),
		_mark_player(p);
		schedule(70, '_herobrine_warn', p, sqrt(d));
	);
);

__on_start() -> (
	global_marked_players = read_file('marked_players', 'json') || {};
);

__on_player_connects(p) -> (
	if(!bool(rand(50)), (
		schedule(50, '_herobrine_greets', p);
	));
);

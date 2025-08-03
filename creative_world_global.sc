__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
	'libraries' -> [{
		'source' -> 'libchatter.sc'
	}],
	'event_priority' -> 99,
};

import('libchatter',
	'action_msg',
	'player_msg',
	'msg',
);

// Tracking per-player variables manually since player scope is bugged to hell
// <bullshit>
global_pvars = {};

__on_start() -> (
	for(player('all'), global_pvars:_ = {
		'last_hurt' -> [null, 0];
	});
);

__on_player_connects(p) -> (
	global_pvars:p = {};
);

__on_player_disconnects(p, r) -> (
	delete(global_pvars, p);
);
// </bullshit>

__on_player_deals_damage(p, a, e) -> (
	if(query(p, 'gamemode_id') != 1, return());

	if(a == 1, (
		modify(e, 'kill');
	));
);

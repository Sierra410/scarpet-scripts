__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

global_stopwatch_users = {};

__on_player_uses_item(p, i, h) -> (
	if(i:0 != 'clock', return());

	if(has(global_stopwatch_users, p),
		_stop_stopwatch(p),
		_start_stopwatch(p),
	);
);

_start_stopwatch(p) -> (
	global_stopwatch_users:p = system_info('world_time');
);

_stop_stopwatch(p) -> (
	delete(global_stopwatch_users, p);
);

_fmt_time(ticks) -> (
	s = ticks / 20;
	t = ticks % 20 * 5;

	format('y '+if(
		t < 3600, str('%02d:%02d.%02d', s / 60, s % 60, t),
		str('%d:%02d:%02d.%02d', s / 3600, s % 3600 / 60, s % 60, t),
	))
);

_stopwatch_tick() -> (
	t = system_info('world_time');
	for(pairs(global_stopwatch_users), (
		if(inventory_find(_:0, 'clock') != null,
			display_title(_:0, 'actionbar', _fmt_time(t - _:1), 0, 10, 0),
			_stop_stopwatch(_:0), // Stop if player dropped the clock
		);
	));
);

__on_tick() -> (
	_stopwatch_tick();
);

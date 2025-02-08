__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

__on_player_respawns(p) -> (
	if(
		// In case first time login counts as "respawning"
		statistic(p, 'custom', 'deaths') == 0
		// Appearing after end-credits counts as "respawning"
		|| statistic(p, 'custom', 'time_since_death') > 0,
		return(),
	);

	schedule(0, _(outer(p)) -> (
		modify(p, 'effect', 'unluck', 6000, 0, false, true, false);
	));
);

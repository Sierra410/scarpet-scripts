__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

global_nonorientable = [
	'deepslate',
];

__on_player_places_block(p, i, h, b) -> (
	if((global_nonorientable ~ b) != null,
		set(b, b, {'axis'->'y'});
	);
);

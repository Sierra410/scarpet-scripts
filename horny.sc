// https://derpibooru.org/images/3619043

__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'player',
};

__on_statistic(player, category, event, value) -> (
	if(category != 'used' || event != 'golden_carrot', return());
	_make_horny();
);

global_horny_tick_size = 10;
global_horny_duration = 100;
global_horny = 0;

_make_horny() -> (
	should_schedule = (global_horny <= 0);
	global_horny = global_horny_duration;
	if(should_schedule, (
		particle('heart', player()~'pos' + [0, 2, 0], 7, 0.5, 0);

		schedule(global_horny_tick_size, '_horny_tick');
	));
);

_horny_tick() -> (
	global_horny = global_horny - global_horny_tick_size;
	if((global_horny > 0), (
		schedule(global_horny_tick_size, '_horny_tick');
	));
);

__on_player_collides_with_entity(p, e) -> (
	if(global_horny <= 0, return());
	if(e~'type' != 'horse', return());
	if(e~'nbt':'InLove' <= 0, return());

	particle('heart', player()~'pos' + [0, 2, 0], 7, 0.5, 0);

	spawn('horse', player()~'pos', {
		'Age'->-24000,
	});

	modify(e, 'nbt_merge', nbt({
		'InLove'->0,
		'Age'->6000,
	}));


	global_horny = 0;
);

__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'global',
};

global_paintings = {
	'1x1' -> [
		'minecraft:kebab',
		'minecraft:aztec',
		'minecraft:alban',
		'minecraft:aztec2',
		'minecraft:bomb',
		'minecraft:plant',
		'minecraft:wasteland',
		'minecraft:meditative',
	],
	'1x2' -> [
		'minecraft:wanderer',
		'minecraft:graham',
		'minecraft:prairie_ride',
	],
	'2x1' -> [
		'minecraft:pool',
		'minecraft:courbet',
		'minecraft:sunset',
		'minecraft:sea',
		'minecraft:creebet',
	],
	'2x2' -> [
		'minecraft:match',
		'minecraft:bust',
		'minecraft:stage',
		'minecraft:void',
		'minecraft:skull_and_roses',
		'minecraft:wither',
		'minecraft:baroque',
		'minecraft:humble',
		'minecraft:earth',
		'minecraft:wind',
		'minecraft:fire',
		'minecraft:water',
	],
	'3x3' -> [
		'minecraft:bouquet',
		'minecraft:cavebird',
		'minecraft:cotan',
		'minecraft:endboss',
		'minecraft:fern',
		'minecraft:owlemons',
		'minecraft:sunflowers',
		'minecraft:tides',
		'minecraft:dennis',
	],
	'3x4' -> [
		'minecraft:backyard',
		'minecraft:pond',
	],
	'4x2' -> [
		'minecraft:fighters',
		'minecraft:changing',
		'minecraft:finding',
		'minecraft:lowmist',
		'minecraft:passage',
	],
	'4x3' -> [
		'minecraft:skeleton',
		'minecraft:donkey_kong',
	],
	'4x4' -> [
		'minecraft:pointer',
		'minecraft:pigscene',
		'minecraft:burning_skull',
		'minecraft:orb',
		'minecraft:unpacked',
	],
};

_cycle_painting(e, bwd) -> (
	t = e~'nbt':'variant';
	ps = first(pairs(global_paintings),
		i = (_:1~t);
		i != null
	);

	if(ps != null, (
		np = ps:1:((1 - bwd*2) + i);
		modify(
			e, 'nbt_merge',
			str('{variant:"%s"}', np),
		);
	));
);

__on_player_interacts_with_entity(p, e, h) -> (
	if(
		query(e, 'type') == 'painting'
		&& query(p, 'holds', h):0 == 'painting',
		_cycle_painting(e, query(p, 'sneaking')),
	);
);

__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

global_ignore_list = [
	// Blocks with GUIs
	'anvil',
	'barrel',
	'beacon',
	'blast_furnace',
	'brewing_stand',
	'cartography_table',
	'chest',
	'chipped_anvil',
	'crafting_table',
	'damaged_anvil',
	'enchanting_table',
	'ender_chest',
	'furnace',
	'grindstone',
	'hanging_sign',
	'lectern',
	'loom',
	'shulker_box',
	'sign',
	'smithing_table',
	'smoker',
	'stonecutter',
	'trapped_chest',
	// Redstone Components
	'bell',
	'chiseled_bookshelf',
	'comparator',
	'daylight_detector',
	'lever',
	'note_block',
	'redstone_ore',
	'repeater',
	// Blocks with variants, like buttons/doors/gates are added in __on_start
];

_should_ignore(block) -> (
	(global_ignore_list ~ str(block)) != null
);

__on_player_right_clicks_block(p, i, hand, block, face, hitvec) -> (
	if(
		i:0 != 'firework_rocket'
			|| has(i:2, 'Fireworks', 'Explosions'),
		return(),
	);

	_should_ignore(block) || 'cancel'
);

__on_player_uses_item(p, i, hand) -> (
	if(
		i:0 == 'firework_rocket'
			&& has(i:2, 'Fireworks', 'Explosions'),
		'cancel'
	)
);

__on_start() -> (
	for(block_list(), (
		if(
			// (Non-iron) doors, trapdoors, gates, etc.
			(! _~'^iron_' &&
				(
					_~'_door$' ||
					_~'_trapdoor$' ||
					_~'_fence_gate$' ||
					_~'_button$' ||
					_~'_sign$'
					// pots are used rarely enough and are already flaky enough
					// to not matter. Fewer comparisons = faster code anyway.
					// _~'^potted_'
				)
			),
			global_ignore_list += _,
		);
	));
);

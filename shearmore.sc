__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
	'scope' -> 'global',
};

_get_enchantment(item, ench) -> (
	item:2:'components':'minecraft:enchantments':'levels':ench
);

__on_player_interacts_with_entity(p, e, h) -> (
	if(query(e, 'player_type') == null, return());

	if(h == 'mainhand',
		tool_slot = 0, // Current slot
		tool_slot = 5, // Offhand slot
	);

	tool = inventory_get('equipment', p, tool_slot);
	if(tool:0 != 'shears', return());

	hat = inventory_get('equipment', e, 4);
	if(hat:0 != 'carved_pumpkin', return());

	hat_has_binding = _get_enchantment(hat, 'minecraft:binding_curse') != null;
	tool_break_chance = 1 / (
		_get_enchantment(tool, 'minecraft:unbreaking'):'lvl' + 1
	);

	// Damage Shears
	dmg = tool:2:'components':'minecraft:damage';
	if(
		hat_has_binding,
		dmg += 24 * tool_break_chance, // Extra damage, if the pumpkin's cursed
		dmg += number(rand(1.0) < tool_break_chance)
	);

	// Break Shears
	if(dmg > 237, (
		sound(
			'entity.item.break', query(p, 'pos'),
			0.8, 0.8 + rand(0.4), 'player',
		);
		tool:1 += -1;
	));

	nbt = tool:2; // Because tool:2 can't be used as value in put()
	put(nbt, 'components.minecraft:damage', dmg);
	tool:2 = nbt;

	// Replace shears with damaged version
	inventory_set('equipment', p, tool_slot, tool:1, tool:0, tool:2);

	// Play sound
	sound('entity.snow_golem.shear', query(e, 'pos'), 1, 1, 'player');

	// Drop pumpkin
	drop_item('equipment', e, 4);
);

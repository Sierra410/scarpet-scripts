__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
};

_log(... args) -> (
    print(format('c ' + join(' ', args)));
);

_get_enchantment(item, ench) -> (
	if(ench == null, return(null));

	if(length(item) != 3, return(null));

	enchs = item:2:'Enchantments';
	if(enchs == null, return(null));

	ench = str('[{id:"%s"}]', ench);

	enchs:ench
);

_has_enchantment(item, ench) -> (
	_get_enchantment(item, ench) != null
);

__on_player_interacts_with_entity(p, e, h) -> (
	if(h != 'mainhand', return(null));
	if(query(e, 'player_type') == null, return(null));

	tool = inventory_get('equipment', p, 0);
	if(tool:0 != 'shears', return(null));

	hat = inventory_get('equipment', e, 4);
	if(hat:0 != 'carved_pumpkin', return(null));

	hat_has_binding = _has_enchantment(hat, 'minecraft:binding_curse');
	tool_break_chance = 1 / (
		_get_enchantment(tool, 'minecraft:unbreaking'):'lvl' + 1
	);

	// Damage Shears
	dmg = tool:2:'Damage';
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

	tool:2:'Damage' = dmg;

	// Replace shears with damaged version
	inventory_set('equipment', p, 0, tool:1, tool:0, tool:2);

	// Play sound
	sound('entity.snow_golem.shear', query(e, 'pos'), 1, 1, 'player');

	// Drop pumpkin
	drop_item('equipment', e, 4);
);

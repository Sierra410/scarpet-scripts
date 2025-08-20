__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

_leash_fix(e, new) -> (
	if(
		// Skip New Entities
		new, return(),

		// Skip unleashed entities
		e~'nbt':'leash' == null,
			return(),

		// Skip entities with NoAI that were _not_ lobotomized by us
		(
			(e~'nbt':'NoAI' != null)
			&& !query(e, 'has_scoreboard_tag', 'LEASHFIX')
		),
			return(),
	);

	uuid = e~'uuid';

	modify(e, 'ai', false); // Disable AI
	modify(e, 'tag', 'LEASHFIX'); // Tag the entity to indicate it was us
	logger('info', str('Lobotomized %s (%s)', e, uuid));

	schedule(100, _(id) -> ( // Re-enable AI 5 seconds later
		e = entity_id(id);
		if(e == null,
			logger('error', str(
				'Lobotomized entity %s disappeared before being re-brained!',
				id,
			));
			return();
		);
		modify(e, 'clear_tag', 'LEASHFIX'); // We're done with this entity
		modify(e, 'ai', true); // Re-Enable AI
		logger('info', str('Rebrained %s (%s)', e, uuid));
	), uuid);
);

entity_load_handler('cat', _(e, new) -> _leash_fix(e, new));
entity_load_handler('wolf', _(e, new) -> _leash_fix(e, new));

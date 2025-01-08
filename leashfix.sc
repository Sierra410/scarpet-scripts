__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

_leash_fix(e, new) -> (
	if(
		new, return(null), // Skip new entities
		e~'nbt':'Leash' == null, return(null), // Skip unleashed entities
		e~'nbt':'NoAI' != null, return(null), // Skip entities w/o AI
	);

	modify(e, 'ai', false); // Disable AI
	schedule(100, _(outer(e)) -> ( // Re-enable AI 5 seconds later
		modify(e, 'ai', true);
	));
);

entity_load_handler('cat', _(e, new) -> _leash_fix(e, new));
entity_load_handler('wolf', _(e, new) -> _leash_fix(e, new));

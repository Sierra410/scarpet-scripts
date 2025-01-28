__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

entity_load_handler('iron_golem', _(e, new) -> (
	if(new, sound('item.axe.scrape', pos(e), 3, 0.2, 'neutral'));
));

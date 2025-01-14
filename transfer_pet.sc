__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'player',
	'commands' -> {
        '' -> _() -> _confirm(),
    }
};

// null or [[pets...], 'new_owners_name', nbt(new_owners_uuid)]
global_staged_transfer = null;

_confirm() -> {
    if(global_staged_transfer == null, (
        print(format(
            'r No pets to transfer!
Right click on a player to stage transfer of leashed pets.'
        ));
        return(null);
    ));

    leashed_pets = global_staged_transfer:0;
    new_owner_name = global_staged_transfer:1;
    new_owner_uuid = global_staged_transfer:2;
    global_staged_transfer = null;

    transferred = [];
    for(leashed_pets, (
        e = entity_id(_);
        if(e == null, (
            continue();
        ));

        transferred += e;
        modify(e, 'nbt_merge', nbt({'Owner'->new_owner_uuid}));
    ));

    if(length(transferred) > 0, (
        print(format(str(
            'l Transferred %s to %s.',
            transferred,
            new_owner_name,
        )));
    ));

    missing = length(leashed_pets) - length(transferred);
    if(missing > 0, (
        print(format(str(
            'y Warning: %d pet(s) could not be transferred due to being unloaded or dead.',
            missing,
        )));
    ));
};

__on_player_interacts_with_entity(p, e, h) -> (
    if(h != 'mainhand', return(null));
	if(query(e, 'player_type') == null, return(null));

    tool = inventory_get('equipment', p, 0);
	if(tool:0 != 'lead', return(null));

    owner_uuid = p~'nbt':'UUID';
    leashed_pets = filter(
        entity_selector(str('@e[nbt={Owner:%s}]', owner_uuid)),
        _~'nbt':'Leash':'UUID' == owner_uuid,
    );

    if(length(leashed_pets) == 0, return('null'));

    recipient_uuid = e~'nbt':'UUID';

    // Storing recipient's name and UUID instead of the player object to allow
    // transfer even if the recipient has disconnected after being picked
    global_staged_transfer = [
        map(leashed_pets, query(_, 'uuid')),
        str(e),
        recipient_uuid,
    ];

    print(format(str(
        'l Staged transfer of %s to %s.\nRun /transfer_pet to confirm.',
        leashed_pets,
        e,
    )));
);

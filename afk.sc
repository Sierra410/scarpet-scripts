__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
    'command_permission' -> 'all',
    'commands' -> {
        '' ->                    _() ->     _action(player(), 'shadow'),
        '<action>' ->            _(a) ->    _action(player(), a),
        '<action> <interval>' -> _(a, i) -> _interval(player(), a, i),
    },
    'arguments' -> {
        'action' -> {
            'type' -> 'term',
            'options' -> ['attack', 'use', 'sneak', 'stop'],
        },
        'interval' -> {
            'type' -> 'int',
            'min' -> 0,
            'max' -> 72000,
            'suggester' -> _(args) -> (
                if(!(_supports_interval(args:'action')), return([]));

                [20]
            ),
        },
    },
};

_error(fmt, ... args) -> (
    print(format('r ' + str(fmt, ... args)));
);

_supports_interval(a) -> a == 'attack' || a == 'use';

// Format and run /player command
_player(p, ... a) -> (
    run(str('player %s %s', p, join(' ', a)));
);

// Action w/o interval. Actions that require interval are 'continuous'
_action(p, a) -> (
    if(
        a == 'shadow', _join_afk(p),
        _supports_interval(a), a += ' continuous',
    );

    _player(p, a);
);

// Action with interval. Interval of <1 = once
_interval(p, a, i) -> (
    if(!_supports_interval(a), (
        _error('Only "use" and "attack" can take interval!');

        return(null);
    ));

    if(i < 1,
        _player(p, a, 'once'),
        _player(p, a, 'interval', i),
    );
);

// Join the AFK team
_join_afk(p) -> (
    team = query(p, 'team');

    if(team != null, (
        nbt = nbt_storage(global_def_team_storage);
        put(nbt, query(p, 'uuid'), team);
        nbt_storage(global_def_team_storage, nbt);
    ));

    team_add(global_afk_team, p);
);

// Leave the AFK team and restore the default one
_leave_afk(p) -> (
    team = query(p, 'team');

    if(
        team != global_afk_team,
        return(null);
    );


    nbt = nbt_storage(global_def_team_storage);
    team = get(nbt, query(p, 'uuid'));

    if(team == null, (
        team_leave(p);
        return(null);
    ));

    team_add(team, p);
);

__on_player_connects(p) -> (
    _leave_afk(p);
);

__on_player_takes_damage(p, amount, source, source_entity) -> (
    type = query(p, 'player_type');
    if(type == 'shadow', (
        _leave_afk(p);

        run(str('player %s kill', p));

        return('cancel');
    ));
);

__on_start() -> (
    global_def_team_storage = 'default_team';
    global_afk_team = 'AFK';

    team = team_add(global_afk_team);
    if(team != null, (
        // Configure freshly created team
        team_property(global_afk_team, 'displayName', 'AFK');
        team_property(global_afk_team, 'prefix', format('g [AFK] '));
    ));
);

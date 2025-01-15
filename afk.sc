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

        return();
    ));

    if(i < 1,
        _player(p, a, 'once'),
        _player(p, a, 'interval', i),
    );
);

// Join the AFK team
_join_afk(p) -> (
    _save_player_team(p, query(p, 'team'));
    team_add(global_afk_team, p);
);

// Leave the AFK team and restore the default one
_leave_afk(p) -> (
    team = query(p, 'team');

    if(
        team != global_afk_team,
        return();
    );

    team = _get_player_team(p);
    if(
        team != null,
        team_add(team, p), // Re-join team, if there's a default team
        team_leave(p), //     or just leave the AFK team
    );
);

__on_player_connects(p) -> (
    _leave_afk(p);
);

__on_player_takes_damage(p, amount, source, source_entity) -> (
    type = query(p, 'player_type');
    // No-op for non-shadowed players
    if(type != 'shadow', return());

    hp = query(p, 'health');
    // Ignore damage, as long as it's not too much
    if((hp - amount) > 10, return());

    // Bail out the player
    _leave_afk(p);
    run(str('player %s kill', p));
    return('cancel');
);

_load_player_teams() -> (
    global_player_teams = read_file('player_teams', 'json') || {};
);

_save_player_team(p, t) -> (
	put(global_player_teams, query(p, 'uuid'), t);
	write_file('player_teams', 'json', global_player_teams);
);

_get_player_team(p) -> (
	get(global_player_teams, query(p, 'uuid'))
);

__on_start() -> (
   _load_player_teams();

    global_afk_team = 'AFK';
    team = team_add(global_afk_team);
    if(team != null, (
        // Configure freshly created team
        team_property(global_afk_team, 'displayName', 'AFK');
        team_property(global_afk_team, 'prefix', format('g [AFK] '));
    ));
);

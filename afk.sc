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
            'options' -> ['attack', 'use', 'sneak', 'stop', 'shadow'],
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

_player(p, ... a) -> (
    run(str('player %s %s', p, join(' ', a)));
);

_action(p, a) -> (
    if(
        a == 'shadow', _join_afk(p),
        _supports_interval(a), a += ' continuous',
    );

    _player(p, a);
);

_interval(p, a, i) -> (
    if(!_supports_interval(a), (
        _error('Only "use" and "attack" can take interval!');

        return(null);
    ));

    _player(p, a, 'interval', i);
);

_join_afk(p) -> (
    if(
        query(p, 'team') == null,
        team_add(global_afk_team, p),
    );
);

_leave_afk(p) -> (
    if(
        query(p, 'team') == global_afk_team,
        team_leave(p),
    );
);

__on_player_connects(p) -> (
    _leave_afk(p);
);

__on_player_takes_damage(p, amount, source, source_entity) -> (
    type = query(p, 'player_type');
    if(type == 'shadow', (
        _leave_afk(p);

        run(str('player %s kill', p));

        return('cancel')
    ));
);

__on_start() -> (
    global_afk_team = 'AFK';

    team = team_add(global_afk_team);
    if(team != null, (
        // Configure freshly created team
        team_property(global_afk_team, 'displayName', 'AFK');
        team_property(global_afk_team, 'prefix', format('g [AFK] '));
    ));
);

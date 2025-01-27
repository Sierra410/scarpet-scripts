__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

// Write Player Activity
_wpa(...args) -> (
    write_file('player_activity', 'text', join(' ', args));
);

__on_player_message(p, m) -> (
    _wpa('C', p, m); // C for Chat
);

__on_player_connects(p) -> (
    _wpa('J', p); // J for Join
);

__on_player_disconnects(p, r) -> (
    // Shorten well-known reasons
    if(
        r~'multiplayer.disconnect.generic',
            r = '', // Normal Disconnect, no extra reason
        r~'multiplayer.disconnect.duplicate_login',
            r = 'dupe',
        r~'disconnect.timeout',
            r = 'timeout',
    );

    _wpa('D', p, r); // D for Disconnect
);

__on_player_dies(p) -> (
    _wpa('K', p); // K for Killed
);

__on_player_command(p, c) -> (
    _wpa('E', c); // E for Executes Command
);

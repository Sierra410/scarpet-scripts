__config() -> {
    'strict' -> true,
    'stay_loaded' -> true,
    'scope' -> 'global',
};

// i - italic
// b - bold
// s - strikethrough
// u - underline
// o - obfuscated
//
// w - White (default)
// y - Yellow
// m - Magenta (light purple)
// r - Red
// c - Cyan (aqua)
// l - Lime
// t - lighT blue
// f - dark grayF (weird Flex, but ok)
// g - Gray
// d - golD
// p - PurPle
// n - browN (dark red)
// q - turQuoise (dark aqua)
// e - grEEn
// v - naVy blue
// k - blaK
// #XXXXXX - arbitrary RGB color
//
// '^<format> <text>' - tooltip
// '?<suggestion> - command suggestion
// '!<message>' - click to run
// '@<url>'
// '&<text>' - click to copy

_fmt_arg(arg, def_fmt) -> (
	t = type(arg);
	if(
		t == 'text', arg,
		t == 'bool', arg && format('bl True') || format('br False'),
		t == 'number', format('bc '+str(arg)),
		t == 'list', format('b '+str(arg)),
		format(def_fmt+str(arg)),
	)
);

// Formats a list of aguments. Passing an argument like '%%[format]' will set
// that format as the defauly for all following arugments
_fmt(args) -> (
	def_fmt = 'w ';

	sum(...map(args, (
		if(_ ~ '^%%',
			def_fmt = slice(_, 2)+' ';
			continue();
		);

		_fmt_arg(_, def_fmt)
	)))
);

echo(...msg) -> (
	chat_msg(player('all'), ...msg)
);

chat_msg(p, ...msg) -> (
	print(p, _fmt(msg));
);

action_msg(p, ...msg) -> (
	display_title(p, 'actionbar', _fmt(msg), 0, 10, 0);
);

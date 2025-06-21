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

_fmt_list(l, conf) -> (
	def_fmt = 'b#EEEEEE ';

	if(conf:'indent' == true, (
		comma = format(conf:'def_fmt'+',\n  ');
		bropen = format(conf:'def_fmt'+'[\n  ');
		brclose = format(conf:'def_fmt'+',\n]');
	), (
		comma = format(conf:'def_fmt'+', ');
		bropen = format(conf:'def_fmt'+'[');
		brclose = format(conf:'def_fmt'+']');
	));

	fmtd = [bropen];
	for(l, (
		fmtd += _fmt_arg(_, conf);
		fmtd += comma;
	));

	put(fmtd, -1, brclose);

	sum(...fmtd)
);

_fmt_arg(arg, conf) -> (
	t = type(arg);
	if(
		t == 'text', arg,
		t == 'null', format('br null'),
		t == 'number', format('bc '+str(arg)),
		t == 'bool', arg && format('bl True') || format('br False'),
		t == 'list', _fmt_list(arg, conf),
		if(conf:'quote' && (arg ~ '^\\s+$') == null,
			format(conf:'def_fmt'+str('\'%s\'', arg)),
			format(conf:'def_fmt'+str(arg)),
		)
	)
);

// Formats a list of aguments. Passing an argument like '%%<target>[value]' will
// set that variable as the defauly for all following arugments
//
// Available targets:
//   %%%[format]     - default format
//   %%I - indent lists
//   %%i - don't indent lists
//   %%Q - quote strings (except whitespace-only strings)
//   %%q - don't quote strings
_fmt(...args) -> (
	conf = {
		'def_fmt' -> 'w ',
		'indent' -> false,
		'quote' -> false,
	};

	sum(...map(args, (
		if(_ ~ '^%%',
			if(
				_ == '%%I', conf:'indent' = true,
				_ == '%%i', conf:'indent' = false,
				_ == '%%Q', conf:'quote' = true,
				_ == '%%q', conf:'quote' = false,
				conf:'def_fmt' = slice(_, 2)+' ';
			);
			continue();
		);

		_fmt_arg(_, conf)
	)))
);

echo(...msg) -> (
	chat_msg(player('all'), ...msg)
);

chat_msg(p, ...msg) -> (
	print(p, _fmt('%%I', ...msg));
);

action_msg(p, ...msg) -> (
	display_title(p, 'actionbar', _fmt(...msg), 0, 10, 0);
);

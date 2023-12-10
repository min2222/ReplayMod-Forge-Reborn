package com.replaymod.core.utils;

import java.util.regex.Pattern;

public class Patterns {
	public static final Pattern ALPHANUMERIC_UNDERSCORE = Pattern.compile("^[a-z0-9_]*$", 2);
	public static final Pattern ALPHANUMERIC_COMMA = Pattern.compile("^[a-z0-9,]*$", 2);
	public static final Pattern ALPHANUMERIC_SPACE_HYPHEN_UNDERSCORE = Pattern.compile("^[a-z0-9 \\-_]*$", 2);
}

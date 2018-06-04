package cdt.headless.proj.parser;

import java.io.InputStream;

public class IncludeToLowerInputStream extends AbstractIncludeFilterStream {

	public IncludeToLowerInputStream(InputStream in) {
		super(in);
	}

	@Override
	protected int convertChar(int c) {
		return Character.toLowerCase(c);
	}

}

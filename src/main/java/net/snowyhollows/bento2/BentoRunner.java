/*
 * Copyright (c) 2009-2018 Ericsson AB, Sweden. All rights reserved.
 *
 * The Copyright to the computer program(s) herein is the property of Ericsson AB, Sweden.
 * The program(s) may be used  and/or copied with the written permission from Ericsson AB
 * or in accordance with the terms and conditions stipulated in the agreement/contract under
 * which the program(s) have been supplied.
 *
 */
package net.snowyhollows.bento2;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author efildre
 */
public class BentoRunner {

	public static Bento runWithClasspathProperties(BentoFactory<?> factory, String classpathProperties)
			throws IOException {
		Properties properties = new Properties();
		properties.load(BentoRunner.class.getResourceAsStream(classpathProperties));
		Bento root = Bento.createRoot();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			root.register(entry.getKey(), entry.getValue());
		}
		root.get(factory);
		return root;
	}
}

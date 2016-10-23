package com.github.mshin.sosa;

import org.osgi.framework.InvalidSyntaxException;

public class OsgiService {

	public static <S> S getService(Class<S> clazz, CachingOsgiServiceGetter cachingOsgiServiceGetter) {
		return cachingOsgiServiceGetter.getService(clazz);
	}

	public static <S> S getService(Class<S> clazz, String filterString, CachingOsgiServiceGetter cachingOsgiServiceGetter)
			throws InvalidSyntaxException {
		return cachingOsgiServiceGetter.getService(clazz, filterString);
	}
}

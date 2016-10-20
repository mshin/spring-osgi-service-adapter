package com.github.mshin.sosa.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.ServiceReference;

/**
 * Collection container based on the Type Safe Heterogeneous Container pattern.
 * Specifically designed for use with the <code>CachingOsgiServiceGetter</code>.
 * 
 * @author MunChul Shin
 *
 */
public class TypeSafeHeterogeneousContainer {

	private Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();
	private Map<Class<?>, ServiceReference<?>> serviceReferenceMap = new HashMap<Class<?>, ServiceReference<?>>();

	public <T> void putInstance(Class<T> type, T instance) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null.");
		}

		this.instanceMap.put(type, type.cast(instance));
	}

	public <T> T getInstance(Class<T> type) {
		return type.cast(this.instanceMap.get(type));
	}

	public <S> void putServiceReference(Class<S> type, ServiceReference<S> serviceReference) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null.");
		}

		this.serviceReferenceMap.put(type, serviceReference);
	}

	@SuppressWarnings("unchecked")
	public <S> ServiceReference<S> getServiceReference(Class<S> type) {
		return (ServiceReference<S>) this.serviceReferenceMap.get(type);
	}

	public void clearInstanceMap() {
		this.instanceMap.clear();
	}

	public void clearServiceReferenceMap() {
		this.instanceMap.clear();
	}

	public boolean instanceMapContainsKey(Class<?> key) {
		return this.instanceMap.containsKey(key);
	}

	public boolean serviceReferenceMapContainsKey(Class<?> key) {
		return this.serviceReferenceMap.containsKey(key);
	}

	public Collection<Object> instanceMapValues() {
		return this.instanceMap.values();
	}

	public Collection<ServiceReference<?>> serviceReferenceMapValues() {
		return this.serviceReferenceMap.values();
	}
}

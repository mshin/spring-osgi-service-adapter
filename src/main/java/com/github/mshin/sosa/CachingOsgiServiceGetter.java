package com.github.mshin.sosa;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mshin.sosa.exception.OsgiServiceRuntimeException;
import com.github.mshin.sosa.util.TypeSafeHeterogeneousContainer;

/**
 * Spring Bean that manages access to OSGi services. Use this class if your OSGi
 * environment and services are basically static and caching the service
 * instances is not going to cause a problem.
 * <p>
 * The services are cached with the <b>clazz</b> value associated with each
 * service used as the key. Only one service per <b>clazz</b> key can be cached
 * at a time. If a filter is declared, the filter will be used to get the
 * service but the service will be stored under the given clazz value.
 * <p>
 * If the OSGi service instance is not castable to <b>clazz</b> it will not be
 * cached. The <code>ServiceReference</code> associated with that OSGi service
 * will still be cached and used to retrieve the service for subsequent calls to
 * getService() <i>(not sure if this would ever happen)</i>.
 * 
 * @author MunChul Shin
 */
public class CachingOsgiServiceGetter {

	/**
	 * Default timeout for attempting to find an OSGI service, in milliseconds.
	 */
	private static final int DEFAULT_TIMEOUT = 30000;
	private static final Logger LOGGER = LoggerFactory.getLogger(CachingOsgiServiceGetter.class);

	private TypeSafeHeterogeneousContainer cache;

	private Integer findOsgiServiceTimeout = DEFAULT_TIMEOUT;

	private Class<?> bundleClass;

	private BundleContext context;

	/**
	 * Initialization method to be called from Spring context.
	 */
	@PostConstruct
	public void init() {
		LOGGER.info("initializing {}...", this.getClass().getName());

		Bundle bundle = null;

		if (null == this.bundleClass) {

			LOGGER.info("Class used to find bundle: {}", String.valueOf(CachingOsgiServiceGetter.class));

			bundle = FrameworkUtil.getBundle(CachingOsgiServiceGetter.class);
		} else {

			LOGGER.info("Class used to find bundle: {}", String.valueOf(this.bundleClass));

			bundle = FrameworkUtil.getBundle(this.bundleClass);
		}

		LOGGER.info("bundle: {}", String.valueOf(bundle));

		this.context = bundle.getBundleContext();

		LOGGER.info("context: {}", String.valueOf(this.context));

		this.cache = new TypeSafeHeterogeneousContainer();

		LOGGER.info("Finished initializing {}.", this.getClass().getName());
	}

	/**
	 * Necessary to dereference services when no longer used, as per the OSGi
	 * specification.
	 */
	@PreDestroy
	public void destroy() {
		LOGGER.info("destroying {}...", this.getClass().getName());

		this.cache.clearInstanceMap();

		for (ServiceReference<?> reference : this.cache.serviceReferenceMapValues()) {
			context.ungetService(reference);
		}

		LOGGER.info("Finished destroying {}.", this.getClass().getName());
	}

	/**
	 * Equivalent to <code>getService(Class<?> clazz, null)</code>.
	 * 
	 * @param clazz
	 * @return The retrieved OSGi service.
	 */
	public <S> S getService(Class<S> clazz) {
		S service = null;
		try {
			service = getService(clazz, null);
		} catch (InvalidSyntaxException e) {
			LOGGER.error("This error should never be reached.", e);
		}

		return service;
	}

	/**
	 * Returns the OSGi service registered under <b>clazz</b> as per
	 * <code>org.osgi.util.tracker.ServiceTracker.getServiceReference()</code>
	 * documentation. Will throw runtime exceptions if none is found after
	 * <b>DEFAULT_TIMEOUT</b>. If <b>filterString</b> is specified, will use
	 * that instead of <b>clazz</b>.
	 * 
	 * @throws InvalidSyntaxException
	 */
	public <S> S getService(Class<S> clazz, String filterString) throws InvalidSyntaxException {
		// If we have cached the service, return it
		if (this.cache.instanceMapContainsKey(clazz)) {
			return this.cache.getInstance(clazz);
		}
		// If we have cached the reference, cache and return its service
		if (this.cache.serviceReferenceMapContainsKey(clazz)) {
			ServiceReference<S> sr = this.cache.getServiceReference(clazz);
			S service = context.getService(sr);

			this.cache.putInstance(clazz, service);

			return service;
		}
		// If we have neither, cache and return the reference and the service
		return cacheReferenceAndService(clazz, filterString);

	}

	public <S, T> T cacheReferenceAndService(Class<S> clazz, String filterString) throws InvalidSyntaxException {
		ServiceTracker<S, T> track = null;
		if (null == filterString) {
			track = new ServiceTracker<S, T>(this.context, clazz, null);
		} else {
			Filter filter = context.createFilter(filterString);
			track = new ServiceTracker<S, T>(this.context, filter, null);
		}

		track.open();
		try {
			track.waitForService(findOsgiServiceTimeout);
			ServiceReference<S> ref = track.getServiceReference();
			if (ref == null) {
				throw new NullPointerException("Could not find a service reference for class: " + clazz);
			}

			T service = track.getService(ref);

			if (service == null) {
				throw new NullPointerException("Could not find a service registered for class: " + clazz);
			}

			if (clazz.isInstance(service)) {
				S castService = clazz.cast(service);
				this.cache.putInstance(clazz, castService);
			} else {
				LOGGER.warn(
						"Could not cache service because it was not castable to its expected type. | service={} | type={}",
						service, clazz);
			}
			this.cache.putServiceReference(clazz, ref);

			return service;
		} catch (Exception e) {
			LOGGER.error("Could not retrieve service: " + e);
			// We are throwing an exception here because we are assuming the
			// OSGi environment is not dynamic for this implementation.
			throw new OsgiServiceRuntimeException(e);
		} finally {
			track.close();
		}
	}

	public BundleContext getBundleContext() {
		return context;
	}

	public void setBundleClass(Class<?> bundleClass) {
		this.bundleClass = bundleClass;
	}

	public Integer getFindOsgiServiceTimeout() {
		return findOsgiServiceTimeout;
	}

	public void setFindOsgiServiceTimeout(Integer findOsgiServiceTimeout) {
		if (null != findOsgiServiceTimeout) {
			this.findOsgiServiceTimeout = findOsgiServiceTimeout;
		}
	}

}

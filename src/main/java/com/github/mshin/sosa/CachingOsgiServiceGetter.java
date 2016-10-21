package com.github.mshin.sosa;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mshin.sosa.exception.OsgiServiceRuntimeException;
import com.github.mshin.sosa.util.TypeSafeHeterogeneousContainer;

/**
 * Spring Bean that manages access to OSGi services. Use this class if your OSGi
 * environment and services are basically static and caching the service
 * instances is not going to create a problem.
 * 
 * @author MunChul Shin
 */
public class CachingOsgiServiceGetter {

	/**
	 * Default timeout for attempting to find an OSGI service
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
	 * Returns the first OSGi service registered under clazz. Will throw runtime
	 * exceptions if none is found after DEFAULT_TIMEOUT
	 * 
	 */
	public <S> S getService(Class<S> clazz) {
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
		return cacheReferenceAndService(clazz);

	}

	public <S, T> T cacheReferenceAndService(Class<S> clazz) {
		ServiceTracker<S, T> track = new ServiceTracker<S, T>(this.context, clazz, null);
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

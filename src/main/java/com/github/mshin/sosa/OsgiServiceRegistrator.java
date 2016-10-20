package com.github.mshin.sosa;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for registering and unregistering class instances as OSGi
 * services.
 * 
 * @author MunChul Shin
 */
public class OsgiServiceRegistrator {

	private Class<?> bundleClass;
	private BundleContext context;
	private List<ServiceRegistration<?>> registrationList;

	private List<OsgiServiceDefinition> osgiServiceDefinitionList;
	private Object serviceInstance;

	private static final Logger LOGGER = LoggerFactory.getLogger(OsgiServiceRegistrator.class);

	/**
	 * Initialization method to be called from the Spring context. Accesses the
	 * Bundle containing <b>bunleClass</b>. Registers as OSGi services
	 * <code>Object</code> instances defined in <b>serviceInstance</b> and
	 * <b>osgiServiceDefinitionList</b>.
	 */
	@PostConstruct
	public void init() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("initializing {}...", this.getClass().getName());
			LOGGER.debug("Class used to find bundle: {}", String.valueOf(this.bundleClass));
		}

		// Get the Bundle specified by bundleClass
		Bundle bundle = FrameworkUtil.getBundle(bundleClass);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("bundle: {}", String.valueOf(bundle));
		}

		this.context = bundle.getBundleContext();
		this.registrationList = new ArrayList<ServiceRegistration<?>>();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("context: {}", String.valueOf(this.context));
		}

		if (null != this.serviceInstance) {
			registerService(this.serviceInstance.getClass(), this.serviceInstance, new Hashtable<String, String>());
		}
		if (null != this.osgiServiceDefinitionList) {
			for (OsgiServiceDefinition osd : this.osgiServiceDefinitionList) {
				registerService(osd.getClassList(), osd.getServiceInstance(), osd.getProperties());
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Finished initializing {}.", this.getClass().getName());
		}
	}

	/**
	 * Registers <b>service</b> as an OSGi service under the name <b>clazz</b>,
	 * with the specified <b>dictionary</b>.
	 */
	public void registerService(Class<?> clazz, Object service, Dictionary<String, String> dictionary) {

		this.registrationList.add(this.context.registerService(clazz.getName(), service, dictionary));
	}

	/**
	 * Registers <b>service</b> as an OSGi service under the class names
	 * <b>list</b>, with the specified <b>dictionary</b>.
	 */
	public void registerService(List<String> list, Object service, Dictionary<String, String> dictionary) {

		this.registrationList.add(this.context.registerService(list.toArray(new String[0]), service, dictionary));
	}

	/**
	 * Shutdown method to be called from Spring context. Will unregister
	 * services.
	 */
	@PreDestroy
	public void destroy() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("destroying {}...", this.getClass().getName());
		}

		for (ServiceRegistration<?> registration : this.registrationList) {
			registration.unregister();
		}
		this.registrationList.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Finished destroying {}.", this.getClass().getName());
		}
	}

	/**
	 * This is provided as a convenience method. This property however should be
	 * accessed with caution as it is advised that OSGi code and your
	 * application code should not be mixed.
	 * 
	 * @return
	 */
	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Set this property to control the bundleContext used. It is advised that
	 * only a class one is certain will be used by this bundle be used for this
	 * property, such as a class that implements
	 * <code>org.osgi.framework.BundleActivator</code> for this bundle.
	 * 
	 * @param bundleClass
	 */
	public void setBundleClass(Class<?> bundleClass) {
		this.bundleClass = bundleClass;
	}

	/**
	 * Set this property to register the defined OSGi services under the given
	 * list of class names and the given properties.
	 * 
	 * @param osgiServiceDefinitionList
	 */
	public void setOsgiServiceDefinitionList(List<OsgiServiceDefinition> osgiServiceDefinitionList) {
		this.osgiServiceDefinitionList = osgiServiceDefinitionList;
	}

	/**
	 * Set this property to register the <b>serviceInstance</b> as an OSGi
	 * service under it's class name with no associated properties.
	 * 
	 * @param serviceInstance
	 */
	public void setServiceInstance(Object serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

}

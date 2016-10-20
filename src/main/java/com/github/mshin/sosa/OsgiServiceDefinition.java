package com.github.mshin.sosa;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class is used to define an OSGi service to be registered with the
 * <code>OsgiServiceRegistrator</code>.
 * 
 * @author MunChul Shin
 *
 */
public class OsgiServiceDefinition {

	private List<String> classList;
	private Object serviceInstance;
	private Map<String, String> properties;

	public OsgiServiceDefinition() {

	}

	public OsgiServiceDefinition(List<String> classList, Object serviceInstance, Map<String, String> properties) {
		super();
		this.classList = classList;
		this.serviceInstance = serviceInstance;
		this.properties = properties;
	}

	public List<String> getClassList() {
		return classList;
	}

	public void setClassList(List<String> classList) {
		this.classList = classList;
	}

	public Object getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(Object serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	public Dictionary<String, String> getProperties() {
		Dictionary<String, String> output = new Hashtable<String, String>();
		for (String key : properties.keySet()) {
			output.put(key, properties.get(key));
		}
		return output;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}

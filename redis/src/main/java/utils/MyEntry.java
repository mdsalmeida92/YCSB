package utils;



import java.io.Serializable;
import java.util.Map;

public class MyEntry implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, String> attributes;
	private String key;
	
	public MyEntry() {
	}

	public MyEntry(Map<String, String> attributes) {

		this.attributes = attributes;
	}

	public MyEntry(String key, Map<String, String> attributes) {
		this.key = key;
		this.attributes = attributes;
	}
	
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}


	public Map<String, String> getAttributes() {
		return attributes;
	}



}
package application.dal;

import com.fasterxml.jackson.databind.ObjectMapper;

import application.util.ConfigValues;

public class ConfigureManager {
	private final ObjectMapper jackson;
	private final String fileName;

	public ConfigureManager(String fileName) {
		jackson = new ObjectMapper();
		this.fileName = fileName;
	}

	public ConfigValues getProperties() throws Exception {
		var json = this.getClass().getClassLoader().getResourceAsStream(fileName);
		return jackson.readValue(json, ConfigValues.class);
	}
}

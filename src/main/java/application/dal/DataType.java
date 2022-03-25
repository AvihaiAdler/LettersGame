package application.dal;

public enum DataType {
	Title("title"),
	Data("data");
	
	private final String type;
	
	DataType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return type;
	}
}

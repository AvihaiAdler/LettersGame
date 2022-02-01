package application.util;

public enum ScreenType {
  Welcome("welcome"),
	Cross("cross"),
	Letters("letters"),
	Blank("blank");
	
	public final String type;
	private ScreenType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type;
	}
}

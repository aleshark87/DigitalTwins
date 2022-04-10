package lamp;

public class LampModel {
	
	private boolean lamp_status;
	
	public LampModel(final boolean lamp_status) {
		this.lamp_status = lamp_status;
	}
	
	public boolean getLampStatus() {
		return lamp_status;
	}

	public void setLampStatus(final boolean lamp_status) {
		this.lamp_status = lamp_status;
	}
	
	
}

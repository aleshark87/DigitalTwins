package application;

public class LampClient {

	public static void main(String[] args) {
		HttpThingRequester requester = new HttpThingRequester();
		
		System.out.println(requester.getLampStatus());
		requester.invokeLampSwitchAction(true);
	}

}
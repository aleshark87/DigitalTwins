package application;

public class Main {

	public static void main(String[] args) {
		HttpThingRequester requester = new HttpThingRequester();
		
		System.out.println(requester.getLampStatus());
	}

}
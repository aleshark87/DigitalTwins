package application;

public class Main {

	public static void main(String[] args) {
		HttpRequester requester = new HttpRequester();
		int responseCreate = requester.createThing();
		System.out.println(responseCreate);
		int responseTD = requester.getAndSetThingDescription();
		System.out.println(responseTD);
		//System.out.println(requester);
		System.out.println(requester.getLampStatus());
	}

}

package application;

public class Main {

	public static void main(String[] args) {
		HttpRequester requester = new HttpRequester();
		//int responseCreate = requester.createThing();
		int responseTD = requester.getAndSetThingDescription();
		//System.out.println(responseCreate);
		//System.out.println(responseTD);
		//System.out.println(requester);
		System.out.println(requester.getLampStatus());
	}

}

package crypto.respawned.bazaartracker;

public class User {

	/*
	portalsBought: [Portal!]!
	gotchisOwned: [Aavegotchi!]!
	portalsOwned: [Portal!]!
	parcelsOwned: [Parcel!]!
	*/

	private String id = null;

	public User() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}

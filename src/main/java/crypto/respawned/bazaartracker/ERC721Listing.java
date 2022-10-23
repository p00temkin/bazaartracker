package crypto.respawned.bazaartracker;

import crypto.forestfish.utils.NumUtils;

public class ERC721Listing {

	private String id = null;
	private String erc721TokenAddress = null;
	private String priceInWei = null;
	private Double priceInGHST = 0.0d;
	private String priceInGHSTSTR = null;
	private String seller = "";
	private String timeCreated = "";
	private String timePurchased = "";
	private Gotchi gotchi = null;
	
	public ERC721Listing() {
		super();
	}
	
	public void update() {
		this.priceInGHST = Double.parseDouble("" + priceInWei)/1000000000000000000d;
		this.priceInGHSTSTR = "" + NumUtils.round(this.priceInGHST, 0);
	}

	public String getPriceInWei() {
		return priceInWei;
	}

	public void setPriceInWei(String priceInWei) {
		this.priceInWei = priceInWei;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public Double getPriceInGHST() {
		return priceInGHST;
	}

	public void setPriceInGHST(Double priceInGHST) {
		this.priceInGHST = priceInGHST;
	}

	public String getPriceInGHSTSTR() {
		return priceInGHSTSTR;
	}

	public void setPriceInGHSTSTR(String priceInGHSTSTR) {
		this.priceInGHSTSTR = priceInGHSTSTR;
	}

	public String getErc721TokenAddress() {
		return erc721TokenAddress;
	}

	public void setErc721TokenAddress(String erc721TokenAddress) {
		this.erc721TokenAddress = erc721TokenAddress;
	}

	public Gotchi getGotchi() {
		return gotchi;
	}

	public void setGotchi(Gotchi gotchi) {
		this.gotchi = gotchi;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getTimePurchased() {
		return timePurchased;
	}

	public void setTimePurchased(String timePurchased) {
		this.timePurchased = timePurchased;
	}

	@Override
	public String toString() {
		return "id=" + this.id +  ", priceInGHST=" + this.getPriceInGHSTSTR() + " erc721TokenAddress=" + erc721TokenAddress + ", seller=" + seller;
	}

}

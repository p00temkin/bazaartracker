package crypto.respawned.bazaartracker;

import crypto.forestfish.utils.NumUtils;

public class ERC1155Listing {

	private String id = "";
	private String priceInWei = "";
	private Double priceInGHST = 0.0d;
	private String priceInGHSTSTR = "";
	private String seller = "";
	private String quantity = "";
	private String erc1155TypeId = "";
	private String erc1155TokenAddress = "";
	private boolean sold = true;
	private boolean cancelled = true;
	
	public ERC1155Listing() {
		super();
	}
	
	public void update() {
		this.priceInGHST = Double.parseDouble("" + priceInWei)/1000000000000000000d;
		this.priceInGHSTSTR = "" + NumUtils.round(this.priceInGHST, 0);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getErc1155TypeId() {
		return erc1155TypeId;
	}

	public void setErc1155TypeId(String erc1155TypeId) {
		this.erc1155TypeId = erc1155TypeId;
	}

	public String getErc1155TokenAddress() {
		return erc1155TokenAddress;
	}

	public void setErc1155TokenAddress(String erc1155TokenAddress) {
		this.erc1155TokenAddress = erc1155TokenAddress;
	}

	public boolean isSold() {
		return sold;
	}

	public void setSold(boolean sold) {
		this.sold = sold;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
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

	@Override
	public String toString() {
		return "id=" + id +  ", priceInGHST=" + this.getPriceInGHSTSTR() + ", erc1155TypeId=" + erc1155TypeId + ", erc1155TokenAddress=" + erc1155TokenAddress + ", quantity=" + quantity + ", seller=" + seller + " sold=" + sold;
	}

}

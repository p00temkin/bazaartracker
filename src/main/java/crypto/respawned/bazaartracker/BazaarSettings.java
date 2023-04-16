package crypto.respawned.bazaartracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.utils.NumUtils;
import crypto.forestfish.utils.SystemUtils;
import crypto.respawned.bazaartracker.enums.BazaarItemType;

public class BazaarSettings {

	private static final Logger LOGGER = LoggerFactory.getLogger(BazaarSettings.class);

	// App specific
	//private String theGraphQueryEndpointURI = "https://api.thegraph.com/subgraphs/name/aavegotchi/aavegotchi-core-matic";
	private String theGraphQueryEndpointURI = "https://subgraph.satsuma-prod.com/tWYl5n5y04oz/aavegotchi/aavegotchi-core-matic/api";
	private int theGraphPollFrequencyInSeconds = 60;
	private String erc1155ListingMethodID = "0x575ae876";
	private boolean autoBuy = false;

	// Generic
	private String aavegotchiContractAddress = "0x86935f11c86623dec8a25696e1c19a8659cbf95d";
	private String gasLimit = "200000";

	// Wallet
	private String providerURL = "";
	private String walletAddress = "";
	private String walletMnemonic = "N/A"; // only needed once to create the wallet if it does not exist
	private String walletPrivKey = "N/A"; // only needed once to create the wallet if it does not exist

	// Wearable/consumable specific
	private String matchString = "";

	// Gotchi specific
	private Double maxBRS = 100000.0d; // set to ~ 350 to find normies
	private Double minBRS = 2000.0d;
	private Double minKINSHIP = 400.0d;
	private Double minGHSTBalance = 0.0d;
	private Integer maxHAUNT = 100;

	// Price in GHST threshold
	private String ghstThresholdSTR = "";
	private Double ghstThreshold = 0.0d;

	// API keys
	private String apiTokenApp = "";
	private String apiTokenUser = "";
	private String polygonscanAPIKEY = "";

	private BazaarItemType itemType = BazaarItemType.CONSUMABLE;

	public BazaarSettings() {
		super();
	}

	public String getTheGraphQueryEndpointURI() {
		return theGraphQueryEndpointURI;
	}

	public void setTheGraphQueryEndpointURI(String theGraphQueryEndpointURI) {
		this.theGraphQueryEndpointURI = theGraphQueryEndpointURI;
	}

	public int getTheGraphPollFrequencyInSeconds() {
		return theGraphPollFrequencyInSeconds;
	}

	public void setTheGraphPollFrequencyInSeconds(int theGraphPollFrequencyInSeconds) {
		this.theGraphPollFrequencyInSeconds = theGraphPollFrequencyInSeconds;
	}

	public String getApiTokenApp() {
		return apiTokenApp;
	}

	public void setApiTokenApp(String apiTokenApp) {
		this.apiTokenApp = apiTokenApp;
	}

	public String getApiTokenUser() {
		return apiTokenUser;
	}

	public void setApiTokenUser(String apiTokenUser) {
		this.apiTokenUser = apiTokenUser;
	}

	public String getGhstThresholdSTR() {
		return ghstThresholdSTR;
	}

	public void setGhstThresholdSTR(String ghstThresholdSTR) {
		this.ghstThresholdSTR = ghstThresholdSTR;
		this.ghstThreshold = Double.parseDouble(ghstThresholdSTR);
	}

	public Double getGhstThreshold() {
		return ghstThreshold;
	}

	public void setGhstThreshold(Double ghstThreshold) {
		this.ghstThreshold = ghstThreshold;
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		this.matchString = matchString;
	}

	public String getPolygonscanAPIKEY() {
		return polygonscanAPIKEY;
	}

	public void setPolygonscanAPIKEY(String polygonscanAPIKEY) {
		this.polygonscanAPIKEY = polygonscanAPIKEY;
	}

	public BazaarItemType getItemType() {
		return itemType;
	}

	public void setItemType(BazaarItemType itemType) {
		this.itemType = itemType;
	}

	public Double getMinBRS() {
		return minBRS;
	}

	public void setMinBRS(Double minBRS) {
		this.minBRS = minBRS;
	}

	public Double getMinKINSHIP() {
		return minKINSHIP;
	}

	public void setMinKINSHIP(Double minKINSHIP) {
		this.minKINSHIP = minKINSHIP;
	}

	public Integer getMaxHAUNT() {
		return maxHAUNT;
	}

	public void setMaxHAUNT(Integer maxHAUNT) {
		this.maxHAUNT = maxHAUNT;
	}

	public Double getMaxBRS() {
		return maxBRS;
	}

	public void setMaxBRS(Double maxBRS) {
		this.maxBRS = maxBRS;
	}

	public Double getMinGHSTBalance() {
		return minGHSTBalance;
	}

	public void setMinGHSTBalance(Double minGHSTBalance) {
		this.minGHSTBalance = minGHSTBalance;
	}

	public String getErc1155ListingMethodID() {
		return erc1155ListingMethodID;
	}

	public void setErc1155ListingMethodID(String erc1155ListingMethodID) {
		this.erc1155ListingMethodID = erc1155ListingMethodID;
	}

	public boolean isAutoBuy() {
		return autoBuy;
	}

	public void setAutoBuy(boolean autoBuy) {
		this.autoBuy = autoBuy;
	}

	public String getAavegotchiContractAddress() {
		return aavegotchiContractAddress;
	}

	public void setAavegotchiContractAddress(String aavegotchiContractAddress) {
		this.aavegotchiContractAddress = aavegotchiContractAddress;
	}

	public String getGasLimit() {
		return gasLimit;
	}

	public void setGasLimit(String gasLimit) {
		this.gasLimit = gasLimit;
	}

	public String getProviderURL() {
		return providerURL;
	}

	public void setProviderURL(String providerURL) {
		this.providerURL = providerURL;
	}

	public String getWalletAddress() {
		return walletAddress;
	}

	public void setWalletAddress(String walletAddress) {
		this.walletAddress = walletAddress;
	}

	public String getWalletMnemonic() {
		return walletMnemonic;
	}

	public void setWalletMnemonic(String walletMnemonic) {
		this.walletMnemonic = walletMnemonic;
	}

	public String getWalletPrivKey() {
		return walletPrivKey;
	}

	public void setWalletPrivKey(String walletPrivKey) {
		this.walletPrivKey = walletPrivKey;
	}

	public void print() {
		System.out.println("Settings:");
		System.out.println(" - erc1155ListingMethodID: " + this.getErc1155ListingMethodID());
		System.out.println(" - autobuy: " + this.isAutoBuy());
		System.out.println(" - apiTokenApp: " + this.getApiTokenApp());
		System.out.println(" - apiTokenUser: " + this.getApiTokenUser());
		System.out.println(" - providerURL: " + this.getProviderURL());
		System.out.println(" - polygonscanAPIKEY: " + this.getPolygonscanAPIKEY());
		System.out.println(" - theGraphPollFrequencyInSeconds: " + this.getTheGraphPollFrequencyInSeconds());
		System.out.println(" - itemType: " + itemType);
		System.out.println(" - matchString: " + this.getMatchString());
		System.out.println(" - maxBRS: " + this.getMaxBRS());
		System.out.println(" - minBRS: " + this.getMinBRS());
		System.out.println(" - minKINSHIP: " + this.getMinKINSHIP());
		System.out.println(" - maxHAUNT: " + this.getMaxHAUNT());
		System.out.println(" - minGHSTBalance: " + this.getMinGHSTBalance());
		System.out.println(" - ghstThreshold: " + NumUtils.round(this.getGhstThreshold(), 0));
	}

	public void sanityCheck() {

		if (true &&
				((this.getItemType() == BazaarItemType.WEARABLE) || (this.getItemType() == BazaarItemType.CONSUMABLE)) &&
				"".equals(this.getMatchString()) &&
				true) {
			LOGGER.error("You need to specify a matchstring when searching for Bazaaar WEARABLE/CONSUMABLE items");
			SystemUtils.halt();
		}

		return;
	}

}

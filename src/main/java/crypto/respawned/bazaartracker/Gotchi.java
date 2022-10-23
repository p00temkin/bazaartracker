package crypto.respawned.bazaartracker;

public class Gotchi {

	private String id = null;
	private String name = null;
	private User owner = null;
	private String hauntId = null;
	private String kinship = null;
	private String baseRarityScore = null;
	private String modifiedRarityScore = null;
	private String withSetsRarityScore = null;
	private String collateral = null;
	private String escrow = null;
	private boolean locked = false;
	
	private Double ghostBalance = 0.0d; // populated using call to polygonscan
	
	public Gotchi() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHauntId() {
		return hauntId;
	}

	public void setHauntId(String hauntId) {
		this.hauntId = hauntId;
	}

	public String getKinship() {
		return kinship;
	}

	public void setKinship(String kinship) {
		this.kinship = kinship;
	}

	public String getBaseRarityScore() {
		return baseRarityScore;
	}

	public void setBaseRarityScore(String baseRarityScore) {
		this.baseRarityScore = baseRarityScore;
	}

	public String getModifiedRarityScore() {
		return modifiedRarityScore;
	}

	public void setModifiedRarityScore(String modifiedRarityScore) {
		this.modifiedRarityScore = modifiedRarityScore;
	}

	public String getWithSetsRarityScore() {
		return withSetsRarityScore;
	}

	public void setWithSetsRarityScore(String withSetsRarityScore) {
		this.withSetsRarityScore = withSetsRarityScore;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getCollateral() {
		return collateral;
	}

	public void setCollateral(String collateral) {
		this.collateral = collateral;
	}

	public String getEscrow() {
		return escrow;
	}

	public void setEscrow(String escrow) {
		this.escrow = escrow;
	}

	public Double getGhostBalance() {
		return ghostBalance;
	}

	public void setGhostBalance(Double ghostBalance) {
		this.ghostBalance = ghostBalance;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

}

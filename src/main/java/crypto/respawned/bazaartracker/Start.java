package crypto.respawned.bazaartracker;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.enums.AccountOrigin;
import crypto.forestfish.enums.evm.EVMChain;
import crypto.forestfish.objects.evm.EVMLocalWallet;
import crypto.forestfish.objects.evm.EVMAccountBalance;
import crypto.forestfish.objects.evm.connector.EVMBlockChainConnector;
import crypto.forestfish.utils.EVMUtils;
import crypto.forestfish.utils.FormatUtils;
import crypto.forestfish.utils.NotificationUtils;
import crypto.forestfish.utils.NumUtils;
import crypto.forestfish.utils.SystemUtils;
import crypto.respawned.bazaartracker.enums.BazaarItemType;
import crypto.respawned.bazaartracker.utils.GotchiGraphQLUtils;
import net.pushover.client.MessagePriority;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.info("bazaarTracker init()");

		String baseURL = "https://app.aavegotchi.com/baazaar";
		boolean haltOnUnconfirmedTX = true;
		BazaarSettings settings = null;
		settings = parseCliArgs(args);
		settings.sanityCheck();

		/**
		 *  Initialize connection to MATIC network
		 */
		EVMBlockChainConnector connector = new EVMBlockChainConnector(EVMChain.POLYGON, settings.isHalt_on_rpc_errors());

		/**
		 * Initialize wallet if we are in autobuy mood
		 */
		EVMLocalWallet maticWallet = null;
		if (settings.isAutoBuy()) {

			// Wallet setup + make sure MATIC balance is above 0
			if (!"N/A".equals(settings.getWalletMnemonic())) maticWallet = new EVMLocalWallet("maticwallet", AccountOrigin.RECOVERY_MNEMONIC, "nopassword", settings.getWalletMnemonic());
			if (!"N/A".equals(settings.getWalletPrivKey())) maticWallet = new EVMLocalWallet("maticwallet", AccountOrigin.PRIVATEKEY, "nopassword", settings.getWalletPrivKey());
			if (null == maticWallet) maticWallet = new EVMLocalWallet("maticwallet", AccountOrigin.EXISTING_LOCALWALLETFILE, "nopassword", settings.getWalletMnemonic());

			EVMAccountBalance walletBalance = EVMUtils.getWalletBalanceNativeCurrency(connector, maticWallet);
			BigInteger walletBalanceBI = new BigInteger(walletBalance.getBalanceInWEI());
			if (walletBalanceBI.intValue() == 0) {
				LOGGER.error("wallet " + maticWallet.getCredentials().getAddress() + " has no funds! We gotta spend to pet.");
				SystemUtils.halt();
			}
			LOGGER.info("Ready to move with bazaar wallet " + maticWallet.getCredentials().getAddress());
		}

		/**
		 * ERC721 search (gotchis, land, ..)
		 */
		if (settings.getItemType() == BazaarItemType.GOTCHI) {
			while (true) {
				ArrayList<ERC721Listing> matchingBazaarItems721 = GotchiGraphQLUtils.getBazaarERC721sWithString(settings, connector);
				if (matchingBazaarItems721.isEmpty()) {
					// Nothing available, keep going
				} else {
					for (ERC721Listing lst: matchingBazaarItems721) {
						lst.update();

						/**
						 *  autobuy if configured
						 */
						if (settings.isAutoBuy()) {
							LOGGER.warn("TODO");
							SystemUtils.halt();
						} else {
							String logMessage =  "We have a matching gotchi: " +  "[brs=" + lst.getGotchi().getBaseRarityScore() + ",kinship=" + lst.getGotchi().getKinship() + ",haunt=" + lst.getGotchi().getHauntId() + ",ghst=" + NumUtils.round(lst.getGotchi().getGhostBalance(), 0) + "] and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " for price " + lst.getPriceInGHSTSTR() + " GHST. Name: " + lst.getGotchi().getName() + " URL: " + baseURL + "/erc721/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, baseURL + "/erc721/" + lst.getId(), "Bazaar URL", "siren");
							LOGGER.info(logMessage);
						}
					}
					LOGGER.info("Halting.");
					SystemUtils.halt();
				}

				LOGGER.info("Wating " + settings.getTheGraphPollFrequencyInSeconds() + " seconds before next check");
				SystemUtils.sleepInSeconds(settings.getTheGraphPollFrequencyInSeconds());
			}
		}

		/**
		 * ERC1155 search (wearables, consumables ..)
		 */
		if ((settings.getItemType() == BazaarItemType.WEARABLE) || (settings.getItemType() == BazaarItemType.CONSUMABLE)) {
			while (true) {
				ArrayList<ERC1155Listing> matchingBazaarItems1155 = GotchiGraphQLUtils.getBazaarERC1155sWithString(settings, settings.getMatchString(), settings.getGhstThreshold());
				if (matchingBazaarItems1155.isEmpty()) {
					// Nothing available, keep going
				} else {
					for (ERC1155Listing lst: matchingBazaarItems1155) {
						lst.update();

						/**
						 *  autobuy if configured
						 */
						if (settings.isAutoBuy()) {
							String logMessage =  "We have a matching item match for string " + settings.getMatchString() + " and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " Buying it for " + NumUtils.round(lst.getPriceInGHST(), 0) + ". URL: " + baseURL + "/erc1155/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, baseURL + "/erc1155/" + lst.getId(), "Bazaar URL", "siren");
							LOGGER.info(logMessage);

							BigInteger priceinWei = new BigInteger(lst.getPriceInWei());
							LOGGER.info("priceInWei: " + lst.getPriceInWei());
							int txRetryThreshold = 3;
							int confirmTimeInSecondsBeforeRetry = 20;

							// Prepare the request hex data
							String buyRequest_hexData = settings.getErc1155ListingMethodID()                    // methodID for PET action (default 0x575ae876)
									+ FormatUtils.makeUINT256WithDec2Hex(Integer.parseInt(lst.getId()))     	// uint256 param1, itemID (last param of the Bazaar URL) in hex
									+ FormatUtils.makeUINT256WithDec2Hex(1)    							    	// uint256 param2, amount
									+ FormatUtils.makeUINT256WithDec2Hex(priceinWei);                           // uint256 param3, priceInWei

							String txHASH = EVMUtils.makeSignedRequest(buyRequest_hexData, txRetryThreshold, confirmTimeInSecondsBeforeRetry, connector, maticWallet.getCredentials(), settings.getAavegotchiContractAddress(), haltOnUnconfirmedTX);
							System.out.println("txHASH: " + txHASH);
						} else {
							String logMessage =  "We have a matching item match for string " + settings.getMatchString() + " and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " Available for " + NumUtils.round(lst.getPriceInGHST(), 0) + ". URL: " + baseURL + "/erc1155/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, baseURL + "/erc1155/" + lst.getId(), "Bazaar URL", "siren");
							LOGGER.info(logMessage);
						}

					}
					LOGGER.info("Halting.");
					SystemUtils.halt();
				}

				LOGGER.info("Wating " + settings.getTheGraphPollFrequencyInSeconds() + " seconds before next check");
				SystemUtils.sleepInSeconds(settings.getTheGraphPollFrequencyInSeconds());
			}
		}

	}


	private static BazaarSettings parseCliArgs(String[] args) {

		BazaarSettings settings = new BazaarSettings();
		Options options = new Options();

		// Type of Bazaar search
		Option itemType = new Option("t", "itemtype", true, "Bazaar item type, WEARABLE, CONSUMABLE, PARCEL or GOTCHI");
		itemType.setRequired(true);
		options.addOption(itemType);

		// API token app ID
		Option apiTokenApp = new Option("a", "apitokenappid", true, "API token app ID");
		options.addOption(apiTokenApp);

		// API token user ID
		Option apiTokenUser = new Option("u", "apitokenuserid", true, "API token user ID");
		options.addOption(apiTokenUser);

		// API polygonscan
		Option apiPolygonscan = new Option("o", "apikeypolygonscan", true, "The Polygonscan API key (https://polygonscan.com/myapikey)");
		options.addOption(apiPolygonscan);

		// item match string
		Option matchSTR = new Option("q", "matchstring", true, "Bazaar item name match string");
		options.addOption(matchSTR);

		// item price threshold (in GHST)
		Option ghstthreshold = new Option("g", "ghstthreshold", true, "Bazaar item threshold in GHST");
		ghstthreshold.setRequired(true);
		options.addOption(ghstthreshold);

		// max haunt (GOTCHI)
		Option maxHaunt = new Option("h", "maxhaunt", true, "Max haunt of the gotchi");
		options.addOption(maxHaunt);

		// min BRS (GOTCHI)
		Option minBRS = new Option("b", "minbrs", true, "Min BRS for your Gotchi");
		options.addOption(minBRS);

		// graph poll frequency
		Option graphPollFrequencyInSeconds = new Option("s", "graphpollfrequency", true, "The Graph poll frequency");
		graphPollFrequencyInSeconds.setRequired(true);
		options.addOption(graphPollFrequencyInSeconds);

		// MATIC/Polygon provider URL
		Option providerURL = new Option("p", "providerurl", true, "MATIC/Polygon Provider URL (infura etc)");
		options.addOption(providerURL);

		// wallet address
		Option walletAdress = new Option("w", "wallet", true, "Wallet address");
		options.addOption(walletAdress);

		// wallet mnemonic
		Option walletMnemonic = new Option("m", "walletmnemonic", true, "Wallet mnemonic");
		options.addOption(walletMnemonic);

		// wallet private key
		Option walletPrivatekey = new Option("k", "walletprivkey", true, "Wallet private key");
		options.addOption(walletPrivatekey);

		// min kinship
		Option minKinship = new Option("i", "minkinship", true, "Min kinship of the gotchi");
		options.addOption(minKinship);

		// attempt autobuy
		Option autoBuy = new Option("x", "autobuy", false, "Attempt to autobuy with GHST in your wallet");
		options.addOption(autoBuy);

		// min GHST balance of gotchi wallet
		Option minghstthreshold = new Option("y", "minghst", true, "Minimum GHST in target gotchi pocket");
		options.addOption(minghstthreshold);

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("t")) {
				if (cmd.getOptionValue("itemtype").equalsIgnoreCase("WEARABLE")) settings.setItemType(BazaarItemType.WEARABLE);
				if (cmd.getOptionValue("itemtype").equalsIgnoreCase("CONSUMABLE")) settings.setItemType(BazaarItemType.CONSUMABLE);
				if (cmd.getOptionValue("itemtype").equalsIgnoreCase("PARCEL")) settings.setItemType(BazaarItemType.PARCEL);
				if (cmd.getOptionValue("itemtype").equalsIgnoreCase("GOTCHI")) settings.setItemType(BazaarItemType.GOTCHI);
			}
			if (cmd.hasOption("x")) settings.setAutoBuy(true);
			if (cmd.hasOption("a")) settings.setApiTokenApp(cmd.getOptionValue("apitokenappid"));
			if (cmd.hasOption("u")) settings.setApiTokenUser(cmd.getOptionValue("apitokenuserid"));
			if (cmd.hasOption("q")) settings.setMatchString(cmd.getOptionValue("matchstring"));
			if (cmd.hasOption("g")) settings.setGhstThresholdSTR(cmd.getOptionValue("ghstthreshold"));
			if (cmd.hasOption("o")) settings.setPolygonscanAPIKEY(cmd.getOptionValue("apikeypolygonscan"));
			if (cmd.hasOption("p")) settings.setProviderURL(cmd.getOptionValue("providerurl"));
			if (cmd.hasOption("m")) settings.setWalletMnemonic(cmd.getOptionValue("walletmnemonic"));
			if (cmd.hasOption("k")) settings.setWalletPrivKey(cmd.getOptionValue("walletprivkey"));
			if (cmd.hasOption("w")) settings.setWalletAddress(cmd.getOptionValue("wallet"));
			if (cmd.hasOption("g")) settings.setGasLimit(cmd.getOptionValue("gaslimit"));
			if (cmd.hasOption("h")) settings.setMaxHAUNT(Integer.parseInt(cmd.getOptionValue("maxhaunt")));
			if (cmd.hasOption("b")) settings.setMinBRS(Double.parseDouble(cmd.getOptionValue("minbrs")));
			if (cmd.hasOption("i")) settings.setMinKINSHIP(Double.parseDouble(cmd.getOptionValue("minkinship")));
			if (cmd.hasOption("s")) settings.setTheGraphPollFrequencyInSeconds(Integer.parseInt(cmd.getOptionValue("graphpollfrequency")));
			if (cmd.hasOption("y")) settings.setMinGHSTBalance(Double.parseDouble(cmd.getOptionValue("minghst")));

			settings.sanityCheck();

			settings.print();

		} catch (ParseException e) {
			LOGGER.error("ParseException: " + e.getMessage());
			formatter.printHelp(" ", options);
			SystemUtils.halt();
		}

		return settings;
	}

}

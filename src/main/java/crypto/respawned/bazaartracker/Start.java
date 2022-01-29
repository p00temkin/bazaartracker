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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import crypto.forestfish.enums.WalletOrigin;
import crypto.forestfish.objects.evm.EVMBlockChain;
import crypto.forestfish.objects.evm.EVMLocalWallet;
import crypto.forestfish.objects.evm.EVMWalletBalance;
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

		boolean debug = false;
		BazaarSettings settings = null;
		if (debug) {
			settings = new BazaarSettings();

			// DEBUG
			settings.setGhstThresholdSTR("39");
			settings.setItemType(BazaarItemType.WEARABLE);
			settings.setMatchString("Guy Fawkes");
			//settings.setProviderURL("https://polygon-mainnet.infura.io/v3/xxxxx");
			settings.setAutoBuy(true);
			settings.sanityCheck();

		} else {
			settings = parseCliArgs(args);
			settings.sanityCheck();
		}

		/**
		 *  Initialize connection to MATIC network
		 */
		EVMBlockChain maticBlockChain = null;
		Web3j maticWeb3j = null;
		EVMLocalWallet maticWallet = null;
		if (settings.isAutoBuy()) {
			maticBlockChain = new EVMBlockChain("Matic/Polygon", "MATIC", 137, settings.getProviderURL(), "https://polygonscan.com/");
			maticWeb3j = Web3j.build(new HttpService(maticBlockChain.getNodeURL()));

			// Wallet setup + make sure MATIC balance is above 0
			if (!"N/A".equals(settings.getWalletMnemonic())) maticWallet = new EVMLocalWallet("maticwallet", WalletOrigin.RECOVERY_MNEMONIC, "nopassword", settings.getWalletMnemonic());
			if (!"N/A".equals(settings.getWalletPrivKey())) maticWallet = new EVMLocalWallet("maticwallet", WalletOrigin.PRIVATEKEY, "nopassword", settings.getWalletPrivKey());
			if (null == maticWallet) maticWallet = new EVMLocalWallet("maticwallet", WalletOrigin.EXISTING_LOCALWALLETFILE, "nopassword", settings.getWalletMnemonic());

			EVMWalletBalance walletBalance = EVMUtils.getWalletBalanceMain(maticWeb3j, maticBlockChain, maticWallet);
			if (walletBalance.getBalanceInWEI().intValue() == 0) {
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
				ArrayList<ERC721Listing> matchingBazaarItems721 = GotchiGraphQLUtils.getBazaarERC721sWithString(settings);
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
							String logMessage =  "We have a matching gotchi: " +  "[brs=" + lst.getGotchi().getBaseRarityScore() + ",kinship=" + lst.getGotchi().getKinship() + ",haunt=" + lst.getGotchi().getHauntId() + ",ghst=" + NumUtils.round(lst.getGotchi().getGhostBalance(), 0) + "] and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " for price " + lst.getPriceInGHSTSTR() + " GHST. Name: " + lst.getGotchi().getName() + " URL: " + "https://aavegotchi.com/baazaar/erc721/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, "https://aavegotchi.com/baazaar/erc721/" + lst.getId(), "Bazaar URL", "siren");
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
							String logMessage =  "We have a matching item match for string " + settings.getMatchString() + " and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " Buying it for " + NumUtils.round(lst.getPriceInGHST(), 0) + ". URL: " + "https://aavegotchi.com/baazaar/erc1155/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, "https://aavegotchi.com/baazaar/erc1155/" + lst.getId(), "Bazaar URL", "siren");
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

							boolean txAttemptsCompleted = EVMUtils.makeRequest(buyRequest_hexData, txRetryThreshold, confirmTimeInSecondsBeforeRetry, maticWeb3j, maticBlockChain, maticWallet, settings.getAavegotchiContractAddress(), settings.getGasLimit());
							System.out.println("txAttemptsCompleted: " + txAttemptsCompleted);
						} else {
							String logMessage =  "We have a matching item match for string " + settings.getMatchString() + " and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " Available for " + NumUtils.round(lst.getPriceInGHST(), 0) + ". URL: " + "https://aavegotchi.com/baazaar/erc1155/" + lst.getId();
							NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, "https://aavegotchi.com/baazaar/erc1155/" + lst.getId(), "Bazaar URL", "siren");
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
		Option apiPolygonscan = new Option("k", "apikeypolygonscan", true, "The Polygonscan API key (https://polygonscan.com/myapikey)");
		options.addOption(apiPolygonscan);

		// item match string
		Option matchSTR = new Option("y", "matchstring", true, "Bazaar item match string");
		options.addOption(matchSTR);

		// item price threshold (in GHST)
		Option ghstthreshold = new Option("g", "ghstthreshold", true, "Bazaar item threshold in GHST");
		ghstthreshold.setRequired(true);
		options.addOption(ghstthreshold);

		// max haunt (GOTCHI)
		Option maxHaunt = new Option("h", "maxhaunt", true, "Max haunt of the gotchi you are looking for");
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

		// wallet mnemonic
		Option walletPrivatekey = new Option("k", "walletprivkey", true, "Wallet private key");
		options.addOption(walletPrivatekey);

		// min kinship
		Option minKinship = new Option("i", "minkinship", true, "Min kinship of the gotchi you are looking for");
		options.addOption(minKinship);

		// attempt autobuy
		Option autoBuy = new Option("x", "autobuy", false, "Attempt to autobuy with GHST in your wallet");
		options.addOption(autoBuy);

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
			if (cmd.hasOption("y")) settings.setMatchString(cmd.getOptionValue("matchstring"));
			if (cmd.hasOption("g")) settings.setGhstThresholdSTR(cmd.getOptionValue("ghstthreshold"));
			if (cmd.hasOption("k")) settings.setPolygonscanAPIKEY(cmd.getOptionValue("apikeypolygonscan"));
			if (cmd.hasOption("p")) settings.setProviderURL(cmd.getOptionValue("providerurl"));
			if (cmd.hasOption("m")) settings.setWalletMnemonic(cmd.getOptionValue("walletmnemonic"));
			if (cmd.hasOption("k")) settings.setWalletPrivKey(cmd.getOptionValue("walletprivkey"));
			if (cmd.hasOption("w")) settings.setWalletAddress(cmd.getOptionValue("wallet"));
			if (cmd.hasOption("g")) settings.setGasLimit(cmd.getOptionValue("gaslimit"));
			if (cmd.hasOption("h")) settings.setMaxHAUNT(Integer.parseInt(cmd.getOptionValue("maxhaunt")));
			if (cmd.hasOption("b")) settings.setMinBRS(Double.parseDouble(cmd.getOptionValue("minbrs")));
			if (cmd.hasOption("i")) settings.setMinKINSHIP(Double.parseDouble(cmd.getOptionValue("minkinship")));
			if (cmd.hasOption("s")) settings.setTheGraphPollFrequencyInSeconds(Integer.parseInt(cmd.getOptionValue("graphpollfrequency")));

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

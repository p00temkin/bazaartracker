package crypto.respawned.bazaartracker;

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
			
			// GOTCHI KINSHIP DEBUG
			settings.setMaxHAUNT(1);
			settings.setMinBRS(400.0d);
			settings.setMinKINSHIP(450.0d);
			settings.setGhstThresholdSTR("1200");
			settings.setItemType(BazaarItemType.GOTCHI);
			settings.setPolygonscanAPIKEY("xxxx");
			
		} else {
			settings = parseCliArgs(args);
			settings.sanityCheck();
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
						String logMessage =  "We have a matching gotchi: " +  "[brs=" + lst.getGotchi().getBaseRarityScore() + ",kinship=" + lst.getGotchi().getKinship() + ",haunt=" + lst.getGotchi().getHauntId() + ",ghst=" + NumUtils.round(lst.getGotchi().getGhostBalance(), 0) + "] and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + " for price " + lst.getPriceInGHSTSTR() + " GHST. Name: " + lst.getGotchi().getName() + " URL: " + "https://aavegotchi.com/baazaar/erc721/" + lst.getId();
						NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, "https://aavegotchi.com/baazaar/erc721/" + lst.getId(), "Bazaar URL", "siren");
						LOGGER.info(logMessage);
					}
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
						String logMessage =  "We have a matching item match for string " + settings.getMatchString() + " and GHST threshold " + NumUtils.round(settings.getGhstThreshold(), 0) + ". URL: " + "https://aavegotchi.com/baazaar/erc1155/" + lst.getId();
						NotificationUtils.pushover(settings.getApiTokenUser(), settings.getApiTokenApp(), "Gotchi BAZAAR time!", logMessage, MessagePriority.HIGH, "https://aavegotchi.com/baazaar/erc1155/" + lst.getId(), "Bazaar URL", "siren");
						LOGGER.info(logMessage);
					}
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
		Option matchSTR = new Option("m", "matchstring", true, "Bazaar item match string");
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
		Option graphPollFrequencyInSeconds = new Option("p", "graphpollfrequency", true, "The Graph poll frequency");
		graphPollFrequencyInSeconds.setRequired(true);
		options.addOption(graphPollFrequencyInSeconds);
		
		// min kinship
		Option minKinship = new Option("i", "minkinship", true, "Min kinship of the gotchi you are looking for");
		options.addOption(minKinship);

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
			if (cmd.hasOption("a")) settings.setApiTokenApp(cmd.getOptionValue("apitokenappid"));
			if (cmd.hasOption("u")) settings.setApiTokenUser(cmd.getOptionValue("apitokenuserid"));
			if (cmd.hasOption("m")) settings.setMatchString(cmd.getOptionValue("matchstring"));
			if (cmd.hasOption("g")) settings.setGhstThresholdSTR(cmd.getOptionValue("ghstthreshold"));
			if (cmd.hasOption("k")) settings.setPolygonscanAPIKEY(cmd.getOptionValue("apikeypolygonscan"));
			if (cmd.hasOption("h")) settings.setMaxHAUNT(Integer.parseInt(cmd.getOptionValue("maxhaunt")));
			if (cmd.hasOption("b")) settings.setMinBRS(Double.parseDouble(cmd.getOptionValue("minbrs")));
			if (cmd.hasOption("i")) settings.setMinKINSHIP(Double.parseDouble(cmd.getOptionValue("minkinship")));
			if (cmd.hasOption("p")) settings.setTheGraphPollFrequencyInSeconds(Integer.parseInt(cmd.getOptionValue("graphpollfrequency")));
			
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

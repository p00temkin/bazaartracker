package crypto.respawned.bazaartracker.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import com.netflix.graphql.dgs.client.GraphQLResponse;

import crypto.forestfish.enums.evm.PolygonERC20Token;
import crypto.forestfish.objects.evm.EVMAccountBalance;
import crypto.forestfish.objects.evm.connector.EVMBlockChainConnector;
import crypto.forestfish.objects.evm.model.erc20.EVMERC20TokenInfo;
import crypto.forestfish.utils.ContractMapper;
import crypto.forestfish.utils.EVMUtils;
import crypto.forestfish.utils.GraphQLUtils;
import crypto.forestfish.utils.NumUtils;
import crypto.forestfish.utils.PolygonscanUtils;
import crypto.forestfish.utils.SystemUtils;
import crypto.respawned.bazaartracker.BazaarSettings;
import crypto.respawned.bazaartracker.ERC1155Listing;
import crypto.respawned.bazaartracker.ERC721Listing;
import crypto.respawned.bazaartracker.Gotchi;
import crypto.respawned.bazaartracker.IdNameTuple;
import reactor.core.publisher.Mono;

public class GotchiGraphQLUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(GotchiGraphQLUtils.class);

	public static Integer getBazaarItemTypeIdForString(BazaarSettings settings, String itemTypeMatchStr) {

		// https://thegraph.com/hosted-service/subgraph/aavegotchi/aavegotchi-core-matic
		String graphqlQuery = "{"
				+ "itemTypes(where:{name_contains: \"" + itemTypeMatchStr + "\"}) {name, id}"
				+ "}";
		HashMap<String, Object> queryArgs = new HashMap<>();

		Mono<GraphQLResponse> graphQLResponse = GraphQLUtils.executeQuery(settings.getTheGraphQueryEndpointURI(), graphqlQuery, queryArgs, 180, 10); // retry for 30 minutes
		if (null == graphQLResponse) {
			LOGGER.error("Unable to get a valid response from the GraphQL query towards " + settings.getTheGraphQueryEndpointURI());
			SystemUtils.halt();
		}

		LOGGER.debug("graphQLResponse: " + graphQLResponse.block().toString());

		IdNameTuple[] itemList = graphQLResponse.map(r -> r.extractValueAsObject("itemTypes", IdNameTuple[].class)).block();	
		if (itemList.length == 0) {
			LOGGER.error("No Bazaar item exists which matches your string");
			SystemUtils.halt();
		} else if (itemList.length == 1) {
			IdNameTuple t = itemList[0];
			LOGGER.debug("itemTypeId is " +  t.getId() + " and name \"" + t.getName() + " for string " + itemTypeMatchStr);
			return Integer.parseInt(t.getId());
		} else {
			LOGGER.error("Your item string is not deterministic, we got multiple item types:");
			for (IdNameTuple t: itemList) {
				LOGGER.error(" - id: " + t.getId() + " name: \"" + t.getName() + "\"");
			}
			SystemUtils.halt();
		}
		return null;
	}

	public static ArrayList<ERC1155Listing> getBazaarERC1155sWithString(BazaarSettings settings, String itemTypeMatchStr, Double priceLimitInGHST) {
		Integer itemTypeId = GotchiGraphQLUtils.getBazaarItemTypeIdForString(settings, itemTypeMatchStr);
		ArrayList<ERC1155Listing> matchingBazaarItems = GotchiGraphQLUtils.getBazaarERC1155sWithItemTypeId(settings, itemTypeId, priceLimitInGHST);
		return matchingBazaarItems;
	}

	public static ArrayList<ERC1155Listing> getBazaarERC1155sWithItemTypeId(BazaarSettings settings, Integer itemTypeId, Double priceLimitInGHST) {

		ArrayList<ERC1155Listing> matches = new ArrayList<ERC1155Listing>();
		int candidateCount = 0;

		// https://thegraph.com/hosted-service/subgraph/aavegotchi/aavegotchi-core-matic
		String graphqlQuery = "{"
				+ "erc1155Listings(where: {erc1155TypeId_in: [\"" + itemTypeId + "\"], sold: false, cancelled: false}) {id, sold, cancelled, priceInWei, seller, quantity, erc1155TypeId, erc1155TokenAddress}"
				+ "}";
		HashMap<String, Object> queryArgs = new HashMap<>();

		Mono<GraphQLResponse> graphQLResponse = GraphQLUtils.executeQuery(settings.getTheGraphQueryEndpointURI(), graphqlQuery, queryArgs, 180, 10); // retry for 30 minutes
		if (null == graphQLResponse) {
			LOGGER.error("Unable to get a valid response from the GraphQL query towards " + settings.getTheGraphQueryEndpointURI());
			SystemUtils.halt();
		}

		LOGGER.debug("graphQLResponse: " + graphQLResponse.block().toString());

		ERC1155Listing[] itemList = graphQLResponse.map(r -> r.extractValueAsObject("erc1155Listings", ERC1155Listing[].class)).block();	
		if (itemList.length == 0) {
			LOGGER.error("No Bazaar items for sale with id " + itemTypeId);
			SystemUtils.halt();
		} else if (itemList.length == 1) {
			ERC1155Listing erc1155item = itemList[0];
			erc1155item.update();
			candidateCount = 1;
			if (erc1155item.getPriceInGHST() <= priceLimitInGHST) {
				LOGGER.info("We found a match for what you want .. id is " + erc1155item.getId() + " and price is \"" + erc1155item.getPriceInWei() + "\"");
				LOGGER.info("Lets check if its within your price target");
				matches.add(erc1155item);
			}
		} else {
			LOGGER.debug("We found multiple matches for what you want ..");
			LOGGER.debug("Lets check if any items are within your price target");
			for (ERC1155Listing erc1155item: itemList) {
				erc1155item.update();
				candidateCount++;
				LOGGER.debug("erc1155item.getPriceInGHST() : " + erc1155item.getPriceInGHSTSTR());
				if (erc1155item.getPriceInGHST() <= priceLimitInGHST) {
					matches.add(erc1155item);
					LOGGER.debug(" - id: " + erc1155item.getId() + " price: \"" + erc1155item.getPriceInWei() + "\"");
				}
			}
		}
		if (matches.isEmpty()) {
			LOGGER.info("Seems all item candidates (" + candidateCount + ") are out of your price target of " + priceLimitInGHST + " GHST, will keep checking");
		}

		return matches;
	}

	public static ArrayList<ERC721Listing> getBazaarERC721sWithString(BazaarSettings settings, EVMBlockChainConnector polygon_connector) {

		ArrayList<ERC721Listing> candidates = new ArrayList<ERC721Listing>();
		ArrayList<ERC721Listing> matches = new ArrayList<ERC721Listing>();

		boolean got1kResults = true;

		int first = 1000;
		int skip = 0;
		while (got1kResults) {

			int gotchiCandidateCount = 0;

			// https://thegraph.com/hosted-service/subgraph/aavegotchi/aavegotchi-core-matic
			String graphqlQuery = "{"
					+ "erc721Listings(first: " + first + ", skip: " + skip + ", where: {timePurchased: \"0\", cancelled: false, portal: null, parcel: null}) {"
					+ "id, priceInWei, seller, timeCreated, timePurchased, erc721TokenAddress,"

					+ "gotchi {"
					+ "id, name, hauntId, locked, owner{id}, kinship, baseRarityScore, collateral, escrow, modifiedRarityScore, withSetsRarityScore"
					+ "}"

					+ "}"

					+ "}";
			HashMap<String, Object> queryArgs = new HashMap<>();
			Mono<GraphQLResponse> graphQLResponse = GraphQLUtils.executeQuery(settings.getTheGraphQueryEndpointURI(), graphqlQuery, queryArgs, 180, 10); // retry for 30 minutes
			if (null == graphQLResponse) {
				LOGGER.error("Unable to get a valid response from the GraphQL query towards " + settings.getTheGraphQueryEndpointURI());
				SystemUtils.halt();
			}

			ERC721Listing[] itemList = graphQLResponse.map(r -> r.extractValueAsObject("erc721Listings", ERC721Listing[].class)).block();	
			if (itemList.length == 0) {
				LOGGER.error("No Gotchi items for sale??");
				SystemUtils.halt();
			} else {
				for (ERC721Listing erc721item: itemList) {
					erc721item.update();
					gotchiCandidateCount++;
					candidates.add(erc721item);
				}
			}

			if (gotchiCandidateCount != 1000) {
				LOGGER.debug("gotchiCandidateCount: " + gotchiCandidateCount );
				got1kResults = false;
			}

			LOGGER.debug("Performed query with first=" + first + " skip=" + skip + ", got " + gotchiCandidateCount + " results back");
			skip = skip + 1000;
		}

		//System.out.println("graphQLResponse: " + graphQLResponse.block().toString());
		/*
		System.out.println("settings.getMinKINSHIP(): " + settings.getMinKINSHIP());
		System.out.println("settings.getMinBRS(): " + settings.getMinBRS());
		System.out.println("settings.getGhstThreshold(): " + settings.getGhstThreshold());
		 */

		if (candidates.isEmpty()) {
			LOGGER.error("No Gotchi items for sale?? Think this is wrong somehow.");
			SystemUtils.halt();
		} else {
			for (ERC721Listing erc721item: candidates) {
				erc721item.update();
				Double brsScore = Double.parseDouble("" + erc721item.getGotchi().getBaseRarityScore());
				Double kinship = Double.parseDouble("" + erc721item.getGotchi().getKinship());
				Integer haunt = Integer.parseInt(erc721item.getGotchi().getHauntId());

				Boolean debug = false;
				String debugName = "455 kinship";
				if (debug && erc721item.getGotchi().getName().toLowerCase().contains(debugName)) {
					System.out.println(" - name: " + erc721item.getGotchi().getName());
					System.out.println(" - brsScore: " + brsScore);
					System.out.println(" - kinship: " + kinship);
					System.out.println(" - haunt: " + haunt);
					System.out.println(" - timepurchased: " + erc721item.getTimePurchased());
					System.out.println(" - price: " + erc721item.getPriceInGHST());
					System.out.println(" - locked: " + erc721item.getGotchi().isLocked());
				}

				// Not sure why still there (sold but not cancelled)
				if (false ||
						(brsScore <= 100.0d) || // burned gotchis get brsScore of 0.0 
						//(!erc721item.getGotchi().isLocked()) || // gotchis for sale should always be locked, but seems not the case?
						!"0".equals(erc721item.getTimePurchased()) || // already sold unless 0
						false) {
					// disregard
				} else {

					boolean ghstBalanceRequirementFulfilled = true;
					Double ghstBalance = 0.0d;

					// Grab the wallet GHST (if required as part of the query)
					if (settings.getMinGHSTBalance() > 0.1d) { 

						Double ghstBalance1 = 0.0d;
						if (settings.getPolygonscanAPIKEY().length() >= 4) {
							ghstBalance1 = PolygonscanUtils.getERC20WalletBalance10kTx(erc721item.getGotchi().getEscrow(), settings.getPolygonscanAPIKEY(), PolygonERC20Token.GHST);
							LOGGER.debug("gotchi GHST balance (using polygonscan): " + NumUtils.round(ghstBalance1, 2));
						} 

						EVMAccountBalance bal = EVMUtils.getAccountBalanceForERC20Token(polygon_connector, erc721item.getGotchi().getEscrow(), PolygonERC20Token.GHST.toString());
						Double ghstBalance2 = Double.parseDouble(bal.getBalanceInETH());

						LOGGER.debug("gotchi GHST balance (using rpc node): " + NumUtils.round(ghstBalance2, 2));
						
						if (ghstBalance1 > 0.0d) ghstBalance = ghstBalance1;
						if (ghstBalance2 > 0.0d) ghstBalance = ghstBalance2;

					}

					Gotchi g = erc721item.getGotchi();
					g.setGhostBalance(ghstBalance);
					erc721item.setGotchi(g);

					if (erc721item.getGotchi().getGhostBalance() < settings.getMinGHSTBalance()) {
						ghstBalanceRequirementFulfilled = false; 
					}

					// interesting discounts to print ..
					if ((erc721item.getGotchi().getGhostBalance() >= 50.0d) && (erc721item.getPriceInGHST() < 2000)) {
						LOGGER.info(" nice wallet stash discount - " + "haunt: " + haunt + " brs: " + NumUtils.round(brsScore, 2) + ", kinship=" + erc721item.getGotchi().getKinship() + " priceInGHST: " + NumUtils.round(erc721item.getPriceInGHST(), 0) + " name: \"" + erc721item.getGotchi().getName() + "\"" + " listing id: " + erc721item.getId() + " ghstBalance: " + NumUtils.round(erc721item.getGotchi().getGhostBalance(), 2));
					}
					
					if (true &&
							ghstBalanceRequirementFulfilled &&
							(haunt <= settings.getMaxHAUNT()) &&
							(erc721item.getPriceInGHST() <= settings.getGhstThreshold()) &&
							(brsScore >= settings.getMinBRS()) &&
							(kinship >= settings.getMinKINSHIP()) &&
							true) {

						LOGGER.debug(" - qualified candidate: " + "haunt: " + haunt + " brs: " + NumUtils.round(brsScore, 2) + ", kinship=" + erc721item.getGotchi().getKinship() + " priceInGHST: " + NumUtils.round(erc721item.getPriceInGHST(), 0) + " name: \"" + erc721item.getGotchi().getName() + "\"" + " listing id: " + erc721item.getId() + " ghstBalance: " + NumUtils.round(erc721item.getGotchi().getGhostBalance(), 2));
						matches.add(erc721item);
					}

				}
			}
		}

		if (matches.isEmpty()) {
			LOGGER.info("Seems all item candidates (" + candidates.size() + ") are out of your price target of " + settings.getGhstThreshold() + " GHST, will keep checking");
		}

		return matches;	
	}


}

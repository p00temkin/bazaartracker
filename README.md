## Bazaartracker

Get mobile notifications (Pushover, <https://pushover.net/>) when items are for sale within your GHST threshold.

![alt text](https://github.com/p00temkin/bazaartracker/blob/master/img/bazaartracker.png?raw=true)

### Prerequisites

[Java 17+, Maven 3.x]

   ```
git clone https://github.com/p00temkin/forestfish
mvn clean package install
   ```

### Building the application

   ```
   mvn clean package
   mv target/bazaartracker-0.0.1-SNAPSHOT-jar-with-dependencies.jar bazaartracker.jar
   ```

### Usage

Match Bazaar "Guy Fawkes" ERC1155 wearable with GHST threshold of 40, pushover UserID A and AppID B, and check every 2 minutes:

   ```
   java -jar ./bazaartracker.jar -t WEARABLE -q "Guy Fawkes" -g 40.0 -u A -a B -s 120
   ```

Match a gotchi with BRS 537, disregard kinship, GHST threshold of 1200, pushover UserID A and AppID B, and check every 2 minutes:

   ```
   java -jar ./bazaartracker.jar -t GOTCHI -g 1200.0 -b 537.0 -i 0.0 -u A -a B -s 120
   ```

 Match a Haunt1 gotchi with kinship 497, disregard brs, GHST threshold of 1500, pushover UserID A and AppID B, and check every 2 minutes:

   ```
   java -jar ./bazaartracker.jar -t GOTCHI -g 1500.0 -i 497.0 -b 0.0 -u A -a B -s 120
   ```

Options:

   ```
usage:
 -a,--apitokenappid <arg>        API token app ID
 -b,--minbrs <arg>               Min BRS for your Gotchi
 -g,--ghstthreshold <arg>        Bazaar item threshold in GHST
 -h,--maxhaunt <arg>             Max haunt of the gotchi you are looking for
 -i,--minkinship <arg>           Min kinship of the gotchi you are looking for
 -k,--walletprivkey <arg>        Wallet private key
 -m,--walletmnemonic <arg>       Wallet mnemonic
 -o,--apikeypolygonscan <arg>    The Polygonscan API key (https://polygonscan.com/myapikey)
 -p,--providerurl <arg>          MATIC/Polygon Provider URL (infura etc)
 -q,--matchstring <arg>          Bazaar item name match string
 -s,--graphpollfrequency <arg>   The Graph poll frequency
 -t,--itemtype <arg>             Bazaar item type, WEARABLE, CONSUMABLE, PARCEL or GOTCHI
 -u,--apitokenuserid <arg>       API token user ID
 -w,--wallet <arg>               Wallet address
 -x,--autobuy                    Attempt to autobuy with GHST in your wallet
 -y,--minghst <arg>              Minimum GHST in target gotchi pocket

   ```

### Support/Donate

To support this project directly:

   ```
   Ethereum/EVM: forestfish.x / 0x207d907768Df538F32f0F642a281416657692743
   Algorand: forestfish.algo / HDWK77MR2O7BLSNFIYG3OPBKTPLNB5PHK4XLGGUYEK4LKTMK7WOCLHE57I
   ```

Or please consider donating to EFF:
[Electronic Frontier Foundation](https://supporters.eff.org/donate)

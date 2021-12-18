## Bazaartracker

Get mobile notifications (Pushover, https://pushover.net/) when items are for sale within your GHST threshold. 

![alt text](https://github.com/p00temkin/bazaartracker/blob/master/img/bazaartracker.png?raw=true)

### Prerequisites

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
   java -jar ./bazaartracker.jar -t WEARABLE -m "Guy Fawkes" -g 40.0 -u A -a B -p 120
   ```
   
Match a gotchi with BRS 537, disregard kinship, GHST threshold of 1200, pushover UserID A and AppID B, and check every 2 minutes: 
   
   ```
   java -jar ./bazaartracker.jar -t GOTCHI -g 1200.0 -b 537.0 -i 0.0 -u A -a B -p 120
   ```
 
 Match a Haunt1 gotchi with kinship 497, disregard brs, GHST threshold of 1500, pushover UserID A and AppID B, and check every 2 minutes: 
   
   ```
   java -jar ./bazaartracker.jar -t GOTCHI -g 1500.0 -i 497.0 -b 0.0 -u A -a B -p 120
   ```
 
 
Options:
   ```
 -a,--apitokenappid <arg>        API token app ID
 -b,--minbrs <arg>               Min BRS for your Gotchi
 -g,--ghstthreshold <arg>        Bazaar item threshold in GHST
 -h,--maxhaunt <arg>             Max haunt of the gotchi you are looking for
 -i,--minkinship <arg>           Min kinship of the gotchi you are looking for
 -k,--apikeypolygonscan <arg>    The Polygonscan API key (https://polygonscan.com/myapikey)
 -m,--matchstring <arg>          Bazaar item match string
 -p,--graphpollfrequency <arg>   The Graph poll frequency
 -t,--itemtype <arg>             Bazaar item type, WEARABLE, CONSUMABLE, PARCEL or GOTCHI
 -u,--apitokenuserid <arg>       API token user ID

   ```
   
### Support/Donate

forestfish.x / 0x207d907768Df538F32f0F642a281416657692743
   
// Script to import twitter friends csv into JanusGraph
// Data is from https://www.kaggle.com/hwassner/TwitterFriends
// The dataset is provided under the CC BY-NC-SA 4.0 license https://creativecommons.org/licenses/by-nc-sa/4.0/ 
// direct download link https://www.kaggle.com/hwassner/TwitterFriends/downloads/data.csv/4, requires login
// Author: Chris Hupman (chupman@us.ibm.com) 07/16/2018
// usage: ./bin/gremlin.sh -e $PWD/loadTwitterFriends.groovy $PWD/data.csv $PWD/janusgraph.properties
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

FILENAME = args[0];


PROPERTIES = args[1];
// Open or create graph database
graph = JanusGraphFactory.open(PROPERTIES)
// load the data
g = graph.traversal();
g.tx().rollback();
batchSize = 100000;
lastBatch = 0;

println 'Reading in file ' + FILENAME;
    
field_name = ["tag", "screenName", "avatar", "followersCount", "friendsCount", "lang", "lastSeen", "tweetId"]
//edges_fields = ["id","...followers"]

// Open file, iterate through each line, and set a the line number to count
new File(FILENAME).eachLine { line, count ->
        String[] field = line.split(",");

        user = g.V().has('id', field[0]);
        for (id in field) {
            if (id == field[0]) continue;
            friend = g.V().has('id', id);
            user.addE("follows").to(friend);
        }

        if (count % batchSize == 0 || (count - lastBatch) > batchSize) {
            graph.tx().commit();
            lastBatch = count;
            println "Commit complete. Vertex added count is at " + (count);
        }

}

// Commit any remaining entries and close the graph
graph.tx().commit();
graph.close();

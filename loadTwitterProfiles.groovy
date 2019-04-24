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
// With the twitter friends example some preprocessing will be necessary.
// delete the header and remove all quotes with something like sed -e s/\"//g data.csv > clean_data.csv

PROPERTIES = args[1];
// Open or create graph database
graph = JanusGraphFactory.open(PROPERTIES)
// load the data
g = graph.traversal();
g.tx().rollback();
batchSize = 100000;

println 'Reading in file ' + FILENAME;
    
field_name = ["id", "screenName", "avatar", "followersCount", "friendsCount", "lang", "lastSeen", "tweetId"]
//edges_fields = ["id","...followers"]

// Open file, iterate through each line, and set a the line number to count
new File(FILENAME).eachLine { line, count ->
        String[] field = line.split(",");
        Date date= new Date(Long.parseLong(field[6]));
        // Get or Create user Vertex
        //t_v1 = g.V().has('id', field[0]); v1 = t_v1.hasNext() ? t_v1.next() : graph.addVertex('id', field[0]);
        g.addV().property('id', field[0]).property('screenName', field[1]).property('avatar', field[2]).
                 property('followersCount', field[3]).property('friendsCount', field[4]).property('lang', field[5]).
                 property('lastSeen', date).property('tweetId', field[7]).iterate();
        if (count % batchSize == 0) {
            graph.tx().commit();
            println "Commit complete. Vertex added count is at " + (count);
        }

}

// Commit any remaining entries and close the graph
graph.tx().commit();
graph.close();

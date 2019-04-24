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
batchSize = 1000000;
lastBatch = 0;
edgeCount = 0;

println 'Reading in file ' + FILENAME;

// Open file, iterate through each line, and set a the line number to count
new File(FILENAME).eachLine { line, count ->
    // println "sup";
    LinkedList fields = line.split(",")
    // userId = Long.valueOf(fields.pop())
    userId = fields.get(0)
    fields.removeFirst()
    user = g.V().has('id', userId).next()
    for (id in fields) {
        friend = g.V().has('id', id).next()
        edge = g.V(user).addE("follows").to(friend).next()
        edgeCount++
    }

    // Check if we exceeded our batchSize and commit if so.
    if ((edgeCount - lastBatch) >= batchSize) {
        graph.tx().commit();
        lastBatch = count + edgeCount;
        println "Commit complete. Vertex added count is at " + (count) + " edgeCount is at " + edgeCount;
    }
}
// Commit any remaining entries and close the graph
graph.tx().commit();
graph.close();

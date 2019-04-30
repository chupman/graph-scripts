// Script to import twitter friends csv into JanusGraph
// Data is from https://www.kaggle.com/hwassner/TwitterFriends
// The dataset is provided under the CC BY-NC-SA 4.0 license https://creativecommons.org/licenses/by-nc-sa/4.0/ 
// direct download link https://www.kaggle.com/hwassner/TwitterFriends/downloads/data.csv/4, requires login
// Author: Chris Hupman (chupman@us.ibm.com) 07/16/2018
// usage: ./bin/gremlin.sh -e $PWD/loadTwitterFriends.groovy $PWD/data.csv $PWD/janusgraph.properties

FILENAME = args[0];

PROPERTIES = args[1];
// Open or create graph database
graph = JanusGraphFactory.open(PROPERTIES)
// load the data
g = graph.traversal();
g.tx().rollback();
batchSize = 100000;

println 'Reading in file ' + FILENAME;

// Open file, iterate through each line, and set a the line number to count
new File(FILENAME).eachLine { line, count ->
        // the id is the only thing on each line
        // fields = line.split(",")
        // println fields[0]
        // v = graph.addVertex("friend")
        if (line) {
            // id = Long.valueOf(line)
            v = graph.addVertex()
            v.property('id', line)
        } else {
            println line
        }

        if (count % batchSize == 0) {
            graph.tx().commit()
            println "Commit complete. Vertex added count is at " + (count)
        }

}

// Commit any remaining entries and close the graph
graph.tx().commit();
graph.close();

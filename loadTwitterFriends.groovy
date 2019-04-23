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
eCount = 0;
vCount = 0;
println 'Reading in file ' + FILENAME;
    
def batchCommit(vCount, eCount, batchSize, graph) {
    if ((vCount + eCount) % batchSize == 0) {
        graph.tx().commit();
        println "Vertex Count as " + (vCount) + ". Edge Count at " + eCount;
    }
}

// Open file, iterate through each line, and set a the line number to count
new File(FILENAME).eachLine { line, count ->

    if (line != null && line.trim().length() > 0) {
        String regex = "\\[(\\s\"#*[a-zA-Z0-9]*\",*)+\\s\\],";
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        while(m.find()) {
            match = m.group();
            // By prefixing with the quote character we won't match the comma after the bracket.
            replace = match.replaceFirst("\",", "\".");
            m.appendReplacement(sb, replace);
        }
        m.appendTail(sb);
        line = sb.toString();
        // By specifying that we should split into 10 fields we can avoid having to do an expensive regex parse on the firends list.
        String[] field = line.split(",", 10);
        Long user = field[0] as Long;

        // Get or Create user Vertex
        t_v1 = g.V().has('id', user); v1 = t_v1.hasNext() ? t_v1.next() : graph.addVertex('id', user);

        if (field[9].length() < 3) return;
        
        Long[] friendList = Arrays.stream(field[9].replaceAll("[\\[\\]\\s\"]", "").split(","))*.asType(Long);
        
        for (Long friend_id : friendList) {
            t_v2 = g.V().has('id', friend_id);
            if (t_v2.hasNext()) {
                v2 = t_v2.next();
            } else {
                v2 = graph.addVertex('id', friend_id);
                vCount++;
                batchCommit(vCount, eCount, batchSize, graph);
            }
            // Create an edge between variables v1, aliased to x, and v2, aliased to y, which were created (or retrieved) above.
            g.V(v1).as('x').V(v2).as('y').addE('follows').from('x').to('y').iterate();
            eCount++;
            batchCommit(vCount, eCount, batchSize, graph); 
        }
    } else {
        println "Empty line at row " + count + ", Skipping";
        return;
    }
}

// Commit any remaining entries and close the graph
graph.tx().commit();
graph.close();

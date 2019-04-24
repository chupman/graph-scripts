// Script to create schema for twitter friends csv import into JanusGraph
// Data is from https://www.kaggle.com/hwassner/TwitterFriends
// The dataset is provided under the CC BY-NC-SA 4.0 license https://creativecommons.org/licenses/by-nc-sa/4.0/ 
// direct download link https://www.kaggle.com/hwassner/TwitterFriends/downloads/data.csv/4, requires login
// Author: Chris Hupman (chupman@us.ibm.com) 07/16/2018
// usage: ./bin/gremlin.sh -e $PWD/createTwitterFriendsSchema.groovy $PWD/janusgraph.properties
PROPERTIES = args[0];
// Open or create graph database
graph = JanusGraphFactory.open(PROPERTIES)

// Create graph schema and indexes, if they haven't already been created
mgmt = graph.openManagement();
// If the id property key is not set then create the schema. This lets us reuse this import script for additonal data files.
if (mgmt.getPropertyKey('id').equals(null)) {
    // Add these to the property file
    // println 'Enabling bulk loading configuration options';
    // mgmt.set('schema.default', 'none');
    // mgmt.set('storage.batch-loading', true);
    // mgmt.set('ids.block-size', 500000);
    // println 'Bulk loading configuration options have been set, creating schema.';

    userId = mgmt.makePropertyKey('id').dataType(Long.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    userName = mgmt.makePropertyKey('screenName').dataType(String.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    tag = mgmt.makePropertyKey('tag').dataType(String.class).cardinality(org.janusgraph.core.Cardinality.SET).make();
    mgmt.makePropertyKey('avatar').dataType(String.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makePropertyKey('followersCount').dataType(Integer.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makePropertyKey('friendsCount').dataType(Integer.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makePropertyKey('lang').dataType(String.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makePropertyKey('lastSeen').dataType(Date.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makePropertyKey('tweetId').dataType(Long.class).cardinality(org.janusgraph.core.Cardinality.SINGLE).make();
    mgmt.makeEdgeLabel('follows').multiplicity(MULTI).make();
    mgmt.makeEdgeLabel('tagged').multiplicity(MULTI).make();
    mgmt.makeVertexLabel('friend').make();
    mgmt.makeVertexLabel('profile').make();

    mgmt.buildIndex('byScreenName', Vertex.class).addKey(userName).buildCompositeIndex();
    mgmt.buildIndex('byId', Vertex.class).addKey(userId).buildCompositeIndex();
    mgmt.buildIndex('byTag', Vertex.class).addKey(tag).buildCompositeIndex();
    println 'created schema';
    
    mgmt.commit();
    println 'Waiting for byScreenName Index to initialize';
    ManagementSystem.awaitGraphIndexStatus(graph, 'byScreenName').call()
    println 'Waiting for byTag Index to initialize';
    ManagementSystem.awaitGraphIndexStatus(graph, 'byTag').call()
    println 'Waiting for byId Index to initialize';
    ManagementSystem.awaitGraphIndexStatus(graph, 'byId').call()
    println 'Indexes have been initialized';

}  else { 
    println 'Schema already exists';
    mgmt.close();
}
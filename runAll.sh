#!/bin/bash
# run all scripts should be run from the root of the janusgraph folder
GRAPH_SCRIPTS=~/github/graph-scripts
PROPERTIES=conf/janusgraph-cassandra-es.properties

echo "Creating Schema"
./bin/gremlin.sh -e ${GRAPH_SCRIPTS}/createTwitterFriendsSchema.groovy ${PROPERTIES}
echo "Starting preprocessing"
python ${GRAPH_SCRIPTS}/preprocessing.py
echo "preprocessing complete"
echo "loading users with profile information"
./bin/gremlin.sh -e ${GRAPH_SCRIPTS}/loadTwitterProfiles.groovy profiles.csv ${PROPERTIES}
echo "loading friends without profile information"
./bin/gremlin.sh -e ${GRAPH_SCRIPTS}/loadTwitterFriend.groovy friends.csv ${PROPERTIES}
echo "loading hashtags and user edges"
./bin/gremlin.sh -e ${GRAPH_SCRIPTS}/loadTwitterHashTags.groovy hashtags.csv ${PROPERTIES}
echo "creating follower edges"
./bin/gremlin.sh -e ${GRAPH_SCRIPTS}/loadTwitterEdges.groovy edges.csv ${PROPERTIES}
echo "Complete"
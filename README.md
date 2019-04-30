# graph-scripts


## Run time statistics

### macbook running docker containers for Cassandra and Elasticsearch
```
$ time ~/github/graph-scripts/runAll.sh | tee output.txt
...
Commit complete. Vertex added count is at 39879 edgeCount is at 32691801
Complete

real    1502m5.117s
user    91m5.748s
sys     29m23.055s
```

### Single gremlin server against 3 node Cassandra and Elasticsearch clusters

Seven hours is a lot better than twenty five, but still far from good. 

```
$ time ../graph-scripts/runAll.sh | tee output.txt
...
Commit complete. Vertex added count is at 39879 edgeCount is at 32691801
Complete

real    425m55.032s
user    206m23.368s
sys     81m51.180s
```

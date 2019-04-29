#!/bin/bash
# Clean out quotes, brackets, spaces and the header line and pipes into a new files. Takes about 30 seconds
#sed -e '1,2{/id,screenName,tags/d;}' -e 's/\"//g' -e 's/\"//g' -e 's/\[ //g' -e 's/ \]//g' -e 's/\[\]//g' -e 's/ //g' data.csv  > data-clean.csv
# Same changes, but made in place changes takes about 36 seconds, but saves 333MB of disk space in exchange for the 6 seconds of run time.
sed -ir 's/\"//g' data.csv 
sed -ir 's/\[ //g' data.csv 
sed -ir 's/ \]//g' data.csv 
sed -ir 's/\[\]//g' data.csv 
sed -ir 's/ //g' data.csv 
sed '1,2{/id,screenName,tags/d;}' data.csv > /dev/null

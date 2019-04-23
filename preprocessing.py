#!/usr/bin/python

import re
from six import iteritems

hashtags = {}
profile_ids = set([])
friend_ids = set([])

def extractTags(line):
    line = line.split(']')[0]
    line = line.split('[')[1]
    return line.split(',')
    # Clean up whitespace and split on , then return list

with open('data.csv', 'r') as input_file, open('profiles.csv', 'w') as profiles, open('edges.csv', 'w') as edges:
    # Matches quotes
    remove_quotes_regex = re.compile(r"\"")
    hashtag_cleanup_regex = re.compile(r"( *\[ *| *\],)")
    # Matches the hashtags bracket, brackets with whitespace, empty brackets, and whitespace 
    cleanup_regex = re.compile(r"(\[.*\]\,|\[ *| *\]|\[ *\]| )")

    for i, line in enumerate(input_file):
        if i != 0:
            line = re.sub(remove_quotes_regex, "", line)
            id = line[:line.index(",")]
            profile_ids.add(id)

            # Add hashtags to dict for later writing
            tags = extractTags(line)
            if tags:
                for tag in tags:
                    if not tag.isspace():
                        hashtags.setdefault(tag.strip(), []).append(id)
            line = re.sub(cleanup_regex, "", line).split(",")

            # Don't write header line
            profiles.write(",".join(line[:8]) + "\n")

            # Write edges files line from remaining data, already includes newline
            edges.write(line[0] + "," + ",".join(line[7:]))

            for friend in line[8:]:
                if friend and (not friend.isspace()):
                    friend_ids.add(friend)

with open('friends.csv', 'w') as friends_f:
    friends_f.write("\n".join(list(friend_ids.difference(profile_ids))) + "\n")

with open('hashtags.csv', 'w') as hashtags_f:
    for hashtag, users in iteritems(hashtags):
        hashtags_f.write(hashtag + "," + ",".join(users) + "\n")

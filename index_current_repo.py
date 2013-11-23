#!/usr/bin/env python

import json, urllib, urllib2, pycurl

def submit_request(data):
    
    c = pycurl.Curl()
    c.setopt(c.URL, "http://localhost:7474/db/data/ext/IndexServices/graphdb/indexSingleNexson")
    c.setopt(c.HTTPHEADER, ["Content-type:Application/json"])
    c.setopt(c.POSTFIELDS, data)
    c.setopt(c.VERBOSE, True)
    
    c.perform()

files_base_url = "https://raw.github.com/OpenTreeOfLife/treenexus/master"
studies_url = "https://api.github.com/repos/OpenTreeOfLife/treenexus/contents/study"
studies = urllib2.urlopen(studies_url)

for study in json.loads(studies.read()):
    
    print indexing
    data = {"url" : "/".join([files_base_url, "study", study["name"], study["name"] + ".json"])}
    submit_request(json.dumps(data))


# additional code that is not currently used but may come in handy below here

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/treenexus/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/treenexus/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

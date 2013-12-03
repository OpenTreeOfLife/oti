#!/usr/bin/env python

import json, urllib2, pycurl, sys

def submit_request(data):
    '''Send a request to oti to index a single study. URL is set to default neo4j location'''
    c = pycurl.Curl()
    c.setopt(c.URL, oti_url + "ext/IndexServices/graphdb/indexSingleNexson")
    c.setopt(c.HTTPHEADER, ["Content-type:Application/json"])
    c.setopt(c.POSTFIELDS, data)
    c.setopt(c.VERBOSE, True)
    c.perform()

files_base_url = "https://raw.github.com/OpenTreeOfLife/treenexus/master"
studies_url = "https://api.github.com/repos/OpenTreeOfLife/treenexus/contents/study"
studies = urllib2.urlopen(studies_url)

oti_url = "http://localhost:7474/db/data/"
if len(sys.argv) > 1:
    oti_url = sys.argv[1].rstrip("/") + "/"

for study in json.loads(studies.read()):
    
    study_id = study["name"]
    print "indexing study " + study_id
    data = {"url" : "/".join([files_base_url, "study", study_id, study_id + ".json"])}
    submit_request(json.dumps(data))


# additional code that is not currently used but may come in handy below here

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/treenexus/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/treenexus/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

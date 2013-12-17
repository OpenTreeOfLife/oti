#!/usr/bin/env python

import json, urllib2, pycurl, sys

def submit_request(data):
    '''Send a request to oti to index a single study. URL is set to default neo4j location'''
    c = pycurl.Curl()
    url = oti_url + "ext/IndexServices/graphdb/indexSingleNexson"
    c.setopt(c.URL, url)
    c.setopt(c.HTTPHEADER, ["Content-type:Application/json"])
    c.setopt(c.POSTFIELDS, data)
    c.setopt(c.VERBOSE, True)
    # Operates asynchronously I think... we need to wait for it to finish.
    # Also it exits without waiting for all transfers to complete.
    # These pile up, and we get 403 responses!
    c.perform()

if len(sys.argv) > 1:
    oti_url = sys.argv[1].rstrip("/") + "/"
else:
    oti_url = "http://localhost:7474/db/data/"

if len(sys.argv) > 2:
    oti_repo = sys.argv[2]
else:
    oti_repo = 'treenexus'

files_base_url = "https://raw.github.com/OpenTreeOfLife/%s/master"%(oti_repo)
studies_url = "https://api.github.com/repos/OpenTreeOfLife/%s/contents/study"%(oti_repo)
studies = urllib2.urlopen(studies_url)

study_list = studies.read()
print " Indexing %s studies from %s"%(len(study_list), oti_repo)

for study in json.loads(study_list):
    study_id = study["name"]
    print "Indexing %s study %s"%(oti_repo, study_id)
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

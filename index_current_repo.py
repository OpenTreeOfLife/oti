#!/usr/bin/env python

import json, requests, sys

def submit_request(data):
	'''Send a request to oti to index a single study. URL is set to default neo4j location'''
	url = oti_url + "ext/IndexServices/graphdb/indexSingleNexson"
	r = requests.post(url,
					  data=json.dumps(data),
					  headers={'Content-type': 'application/json'});
	# We don't really need any data out of r.
	# But do raise an error if something went wrong.
	r.raise_for_status()

if len(sys.argv) > 1:
	oti_url = sys.argv[1].rstrip("/") + "/"
else:
	oti_url = "http://localhost:7474/db/data/"

if len(sys.argv) > 2:
	oti_repo = sys.argv[2]	# maybe 'treenexus_test'
else:
	oti_repo = 'treenexus'
files_base_url = "https://raw.github.com/OpenTreeOfLife/%s/master"%(oti_repo)

if len(sys.argv) > 3:
	oti_mode = sys.argv[3]
else:
	oti_mode = 'github'
if oti_mode == 'github':
	make_url = lambda study_id: "/".join([files_base_url, "study", study_id, study_id + ".json"])
elif oti_mode == 'local':
	make_url = lambda study_id: "http://localhost/api/default/v1/study/%s.json"%(study_id)
else:
    print "Unrecognized mode %s"%(oti_mode)

# or get studies from the API: ...
# files_base_url = "http://localhost/api/default/v1/study/9.json"
# PROBLEM: URL formation are rules in the two cases.

studylist_url = "https://api.github.com/repos/OpenTreeOfLife/%s/contents/study"%(oti_repo)
r = requests.get(studylist_url)
r.raise_for_status()

study_list = r.json()
print " Indexing %s studies from %s"%(len(study_list), oti_repo)

for study in study_list:
	study_id = study["name"]
	url = make_url(study_id)
	print "Indexing %s study %s from %s"%(oti_repo, study_id, url)
	submit_request({"url" : url})


# additional code that is not currently used but may come in handy below here

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/treenexus/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/treenexus/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

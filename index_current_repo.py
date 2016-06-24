#!/usr/bin/env python

# Command line arguments:
#  - URL prefix for communicating with the OTI neo4j server, just
#      before the neo4j plugin method name (which is 'index_study').
#      E.g. http://127.0.0.1:7478/db/data/ext/studies_v3/graphdb
#  - URL prefix for phylesystem API, the part just before '/study/...'
#      or '/study_list'.
#      E.g. https://devapi.opentreeoflife.org

import json, os, requests, sys, tarfile, urllib2

def index_repo(oti_url, phylesystem_api_url):

    # retrieve the study list
    r = requests.get(phylesystem_api_url + "study_list")
    r.raise_for_status()

    study_list = r.json()
    print(" Indexing {} studies from {}".format(len(study_list), phylesystem_api_url))

    for study_id in study_list:
        url = "{}study/{}.json".format(phylesystem_api_url, study_id)
        print("Indexing study {} from {}".format(study_id, url))
        try:
            r = submit_indexing_request(oti_url, {"url" : url })
            r.raise_for_status()
        except requests.exceptions.HTTPError as e:
            print("\nIndexing failed for " + url + "\n\n" + (e.message if hasattr(e, "message") else "(unknown error)") + "\n")
	        # j = r.json()
            print(r.text + '\n')
        else:
            print('OK\n')

def submit_indexing_request(oti_url, data):
	'''Send a request to OTI to index a single study. URL is set to default neo4j location'''
	url = oti_url + "index_study"
	print 'Calling "{}" with data="{}"'.format(url, repr(data))
	r = requests.post(url,
					  data=json.dumps(data),
					  headers={'Content-type': 'application/json'});
	# We don't really need any data out of r.
	# But do raise an error if something went wrong.
	return r

# default args
if __name__ == '__main__':
    if len(sys.argv) > 1:
        oti_url = sys.argv[1].rstrip("/") + "/"
    else:
        # default could be 'http://127.0.0.1:7478/db/data/'
        print("usage (with defaults): index_current_repo.py <oti_url> [<target_phylesystem_api_name>](==http://localhost/phylesystem/v1/)")
        sys.exit(0)
    print("Using the oti instance at: " + oti_url) 

    if len(sys.argv) > 2:
        phylesystem_api_url = sys.argv[2].rstrip("/") + "/"
    else:
        phylesystem_api_url = "http://localhost/phylesystem/v1/"
    print("Accessing studies via: " + phylesystem_api_url) 

    index_repo(oti_url, phylesystem_api_url)


# additional code that is not currently used but may come in handy below here
# note that the github api does request throttling if you're not authenticated

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/phylesystem/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/phylesystem/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

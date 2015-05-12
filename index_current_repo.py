#!/usr/bin/env python

# Command line arguments:
#  - URL prefix for communicating with the OTI neo4j server
#  - URL prefix for phylesystem API methods (not including 'v1/')

import json, os, requests, sys, tarfile, urllib2

def main():

    # default args
    if len(sys.argv) > 1:
        oti_url = sys.argv[1].strip("/") + "/"
    else:
        # default could be 'http://127.0.0.1:7478/db/data/'
        print("usage (with defaults): index_current_repo.py <oti_url> [<target_phylesystem_repo_name>](==http://localhost/api/)")
        sys.exit(0)
    print("Using the oti instance at: " + oti_url) 

    if len(sys.argv) > 2:
        api_url = sys.argv[2].strip("/") + "/"
    else:
        api_url = "http://localhost/phylesystem/"
    #files_base_url = "https://raw.github.com/OpenTreeOfLife/%s/master"%(oti_repo)
    print("Using the studies from: " + api_url) 

    # Ignoring oti_mode for now, since we're using the local API to retrieve study
    # ids and NexSON in the required format.
    #
    ##if len(sys.argv) > 3:
    ##	oti_mode = sys.argv[3]
    ##else:
    ##	oti_mode = 'github'
    ##if oti_mode == 'github':
    ##	make_url = lambda study_id: "/".join([files_base_url, "study", study_id, study_id + ".json"])
    ##elif oti_mode == 'local':
    ##    # this is used during initial docstore indexing
    ##	make_url = lambda study_id: "http://localhost/api/default/v1/study/{}.json".format(study_id)
    ##else:
    ##    print("Unrecognized mode {}".format(oti_mode))

    make_study_url = lambda study_id: api_url + "default/v1/study/{}.json".format(study_id)

    # right now we are not loading the taxonomy, any taxonomy must be provided as a pre-built database

    # retrieve the study list
    r = requests.get(api_url + "study_list")
    r.raise_for_status()

    study_list = r.json()
    print(" Indexing {} studies from {}".format(len(study_list), api_url))

    for study_id in study_list:
        url = make_study_url(study_id)
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
	url = oti_url + "ext/studies/graphdb/index_study"
	print 'Calling "{}" with data="{}"'.format(url, repr(data))
	r = requests.post(url,
					  data=json.dumps(data),
					  headers={'Content-type': 'application/json'});
	# We don't really need any data out of r.
	# But do raise an error if something went wrong.
	return r

main()





# additional code that is not currently used but may come in handy below here

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/phylesystem/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/phylesystem/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

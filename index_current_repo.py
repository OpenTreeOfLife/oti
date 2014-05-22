#!/usr/bin/env python

import json, os, requests, sys, tarfile, urllib2

def get_download_dir():
	d = os.path.abspath(os.path.expanduser("~/Downloads"))
	if not os.path.exists(d):
		os.mkdir(d)
	return d

def submit_indexing_request(data):
	'''Send a request to oti to index a single study. URL is set to default neo4j location'''
	url = oti_url + "ext/IndexServices/graphdb/indexNexsons"
	print 'Calling "{}" with data="{}"'.format(url, repr(data))
	r = requests.post(url,
					  data=json.dumps(data),
					  headers={'Content-type': 'application/json'});
	# We don't really need any data out of r.
	# But do raise an error if something went wrong.
	r.raise_for_status()
	return r.json()

def load_taxonomy():
	'''Install ott... Currently not used, we just supply a pre-built taxonomy database.'''
	
	# setup stuff for the download 
	download_dir = get_download_dir()
	ott_filename = ott_file_url.rsplit("/",1)[1]
	ott_save_path = os.path.join(download_dir,ott_filename)

	# download the taxonomy if necessary
	if not os.path.exists(ott_save_path):
		print("Downloading ott from " + ott_file_url + ".\nThis could take a while...")
		req = urllib2.Request(ott_file_url)
		f = urllib2.urlopen(req)

		# write the taxonomy into the file
		ottfile = file(ott_save_path, 'wb')
		while True:
			chunk = f.read(1000000)
			if not chunk: break
			ottfile.write (chunk)
		ottfile.close()

	# extract the taxonomy if necessary
	tax_file_path = os.path.join(download_dir,"ott","taxonomy.tsv")
	syn_file_path = os.path.join(download_dir,"ott","synonyms.tsv")
	if not os.path.exists(tax_file_path) or not os.path.exists(syn_file_path):
		os.chdir(download_dir)
		tar = tarfile.open(ott_save_path)
		tar.extractall()
		tar.close()

	# call the taxonomy loading routines
	url = oti_url + "ext/ConfigurationServices/graphdb/installOTT"
	data='{"taxonomyFile":"'+tax_file_path+'","synonymFile":"'+syn_file_path+'"}'
	print('Calling "{}" with data="{}"'.format(url, repr(data)))
	r = requests.post(url, data, headers={'Content-type': 'application/json'});

	# We don't really need any data out of r.
	# But do raise an error if something went wrong.
	r.raise_for_status()

# default args
if len(sys.argv) > 1:
	oti_url = sys.argv[1].rstrip("/") + "/"
	# local oti url (example): http://localhost:7474/db/data/
else:
	print("usage (with defaults): index_current_repo.py <oti_url> [<target_phylesystem_repo_name>](==phylesystem) [<ott_file_url>](==http://files.opentreeoflife.org/ott/ott2.8draft3.tgz)")
	sys.exit(0)
print("Using the oti instance at: " + oti_url) 

if len(sys.argv) > 2:
	oti_repo = sys.argv[2]	# maybe 'phylesystem_test'
else:
	oti_repo = 'phylesystem'
files_base_url = "https://raw.github.com/OpenTreeOfLife/%s/master"%(oti_repo)
print("Using the phylesystem repo at: " + files_base_url) 

if len(sys.argv) > 3:
	ott_file_url = sys.argv[3]
else:
	ott_file_url = "http://files.opentreeoflife.org/ott/ott2.8draft3.tgz"
print("Using the ott taxonomy at: " + ott_file_url) 

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
make_url = lambda study_id: "http://localhost/api/default/v1/study/{}.json".format(study_id)

# or get studies from the API: ...
# files_base_url = "http://localhost/api/default/v1/study/9.json"
# PROBLEM: URL formation are rules in the two cases.

# right now we are not loading the taxonomy, any taxonomy must be provided as a pre-built database
load_taxonomy = False
if load_taxonomy:
	load_taxonomy()

# retrieve the study list
studylist_url = "http://localhost/api/study_list"
r = requests.get(studylist_url)
r.raise_for_status()

study_list = r.json()
print(" Indexing {} studies from {}".format(len(study_list), oti_repo))

for study_id in study_list:
	url = make_url(study_id)
	print("Indexing {} study {} from {}".format(oti_repo, study_id, url))
	try:
		r = submit_request({"urls" : [url] })
	except requests.exceptions.HTTPError as e:
		print("\nIndexing failed for " + url + "\n\n" + (e.message if hasattr(e, "message") else "(unknown error)") + "\n")
	else:
		for k, v in r['errors'].items():
			print('\nIndexing failed for URL "{u}", study {s}. Message = "{v}"'.format(u=url, s=k, v=v))


# additional code that is not currently used but may come in handy below here

# access the commits
#commits_url = "https://api.github.com/repos/OpenTreeOfLife/phylesystem/commits"
#commits = urllib2.urlopen(commits_url)
#commits_json = json.loads(commits.read())
#most_recent_commit_sha = commits_json[0]["sha"]

# example url for accessing study from a single commit
# https://raw.github.com/OpenTreeOfLife/phylesystem/5762c8194c718e22bcf17d53818477320be3658d/study/10/10.json

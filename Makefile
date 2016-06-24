# 1. Set up neo4j with 'make neo4j'
# 2. Optional: install a taxomachine graph db in data/graph.db (this
#    is only needed for taxonomy-aware queries)
# 3. make index-repo
# 4. make running
# 5. make test
# 6. make stop

all: neo4j compile restart test

NEO=neo4j-community-1.9.5
PLUGIN=target/oti-neo4j-plugins-0.0.1-SNAPSHOT.jar
SETTINGS=host:apihost=http://localhost:7478 host:translate=true

$(NEO):
	curl http://files.opentreeoflife.org/neo4j/$(NEO).tar.gz >$(NEO).tar.gz
	tar xzvf $(NEO).tar.gz
	sed -i ".bak" -e s+7474+7478+ -e s+7473+7477+ $(NEO)/conf/neo4j-server.properties

# Convenience target for initial setup
neo4j: $(NEO)

compile: $(PLUGIN)

SOURCES=$(shell echo `find src -name "*.java"`)

$(PLUGIN): $(SOURCES)
	./mvn_serverplugins.sh

running: .running

.running: $(PLUGIN)
	rm -f .running
	$(NEO)/bin/neo4j stop
	cp -p $(PLUGIN) $(NEO)/plugins/
	$(NEO)/bin/neo4j start
	touch .running

stop:
	$(NEO)/bin/neo4j stop
	rm -f .running

# use dev phylesystem for testing... may not work
index-repo: $(NEO)
	python index_current_repo.py \
	   http://127.0.0.1:7478/db/data/ext/studies_v3/graphdb \
	   https://devapi.opentreeoflife.org/phylesystem/v1

test-v3: .running
	cd ws-tests; \
	for test in test_v3*.py; do \
	  echo $$test; \
	  PYTHONPATH=../../germinator/ws-tests python $$test $(SETTINGS); \
	done 

test-v2: .running
	cd ws-tests; \
	for test in test_v2*.py; do \
	  echo $$test; \
	  PYTHONPATH=../../germinator/ws-tests python $$test $(SETTINGS); \
	done

test: test-v2 test-v3

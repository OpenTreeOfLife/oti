
## Work in progress: automated web service tests

To run them:

    cd ws-tests
    export PYTHONPATH={path to phylesystem-api repo}/ws-tests:$PYTHONPATH
    {path to phylesystem-api repo}/ws-tests/run_tests.sh devapi.opentreeoflife.org

(It might be more convenient to copy opentreetesting.py and
run_tests.sh locally, rather than refer to the phylesystem-api repo
all the time.)

Substitute whatever API host you're testing for devapi.opentreeoflife.org.

Any test can be run individually:

    cd ws-tests
    export PYTHONPATH={path to phylesystem-api repo}/ws-tests:$PYTHONPATH
    python test_basic.py host:apihost=http://devapi.opentreeoflife.org

You can test against a locally running neo4j instance:

    cd ws-tests
    export PYTHONPATH={path to phylesystem-api repo}/ws-tests:$PYTHONPATH
    python test_basic.py host:apihost=http://localhost:7474 host:translate=true

When writing tests, the utilities in the opentreetesting.py library are available.

For more information on the test harness, see [here](https://github.com/OpenTreeOfLife/phylesystem-api/blob/master/ws-tests/README.md).


## Manual tests

Currently, there few automated tests for OTI. You can, however, fairly thoroughly test the performance manually. Run the following command to index the current phylesystem repo on devapi.opentreeoflife.org:

```bash
./index_current_repo.py http://devapi.opentreeoflife.org/oti http://devapi.opentreeoflife.org/api
```

(For a less time consuming test, try the asterales endpoint.)

And then (assuming indexing worked), query the database to test it. Here are some example queries using cURL, and their expected results.

### Return a list of all studies:

```bash
curl -X POST http://devapi.opentreeoflife.org/oti/ext/studies/graphdb/find_studies
``` 

```json
{
  "matched_studies" : [ {
    "ot:studyId" : "1354"
  }, {
    "ot:studyId" : "2189"
  }, # snipped
  {
    "ot:studyId" : "2326"
  }, {
    "ot:studyId" : "1827"
  } ]
}
```

### Search for studies with trees spanning the taxon with name "Annona glabra":

```bash
curl -X POST http://devapi.opentreeoflife.org/oti/ext/studies/graphdb/find_trees -H "content-type: application/json" -d '{"property":"ot:ottTaxonName","value":"Annona glabra"}'
```

```json
{
  "matched_studies" : [ {
    "matched_trees" : [ {
      "nexson_id" : "tree3008",
      "oti_tree_id" : "1495_tree3008"
    }, {
      "nexson_id" : "tree3009",
      "oti_tree_id" : "1495_tree3009"
    }, {
      "nexson_id" : "tree3010",
      "oti_tree_id" : "1495_tree3010"
    } ],
    "ot:studyId" : "1495"
  } ]
}
```

### Get a list of properties available for searching studies and trees:

```bash
curl -X POST http://devapi.opentreeoflife.org/oti/ext/studies/graphdb/properties
```

```json
{
  "tree_properties" : [ "ot:studyPublicationReference", "is_deprecated", "ot:focalCladeOTTTaxonName", "ot:studyLastEditor", "ot:studyModified", "ot:studyLabel", "ot:comment", "ot:studyId", "ot:dataDeposit", "ot:authorContributed", "ot:studyUploaded", "ot:studyYear", "ot:focalCladeTaxonName", "ot:tag", "ot:curatorName", "ot:studyPublication", "ot:focalCladeOTTId", "ot:focalClade" ],
  "study_properties" : [ "ot:studyPublicationReference", "is_deprecated", "ot:focalCladeOTTTaxonName", "ot:studyLastEditor", "ot:studyModified", "ot:studyLabel", "ot:comment", "ot:studyId", "ot:dataDeposit", "ot:authorContributed", "ot:studyUploaded", "ot:studyYear", "ot:focalCladeTaxonName", "ot:tag", "ot:curatorName", "ot:studyPublication", "ot:focalCladeOTTId", "ot:focalClade" ]
}
```

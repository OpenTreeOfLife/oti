oti
===

An indexing tool for nexsons.

Install
-----

####Clone the repo

First, clone a copy of the oti repo on your local machine.

```
git clone https://github.com/OpenTreeOfLife/oti.git
```

####Clone the repo

Set JAVA_HOME.  On OS X 10.9.5 with Java 8, you have to do

    export JAVA_HOME=`/usr/libexec/java_home`

The incantation will be different on other kinds of system.  Finding
the right method may involve consulting stackoverflow and the involve
parent of the target of the /usr/bin/java symbolic link.

####Install dependencies

OTI requires the jade, ot-base, and taxomachine repositories to be installed into the local maven directory. Run the following commands after you clone to download the dependencies from git and install them into your local maven repository cache. Of course, this requires maven (version 3).

Running this script will clone the ot-base and jade repos into the parent directory of the oti repo. To place them elsewhere, install them manually. See directions at https://github.com/FePhyFoFum/jade and https://github.com/OpenTreeOfLife/ot-base.

```
cd oti
sh mvn_install_dependencies.sh
```

OTI uses the jade, ot-base, and taxomachine classes for many things. The source code for these dependencies is reasonably well-documented. You can refer to the class files in the jade and ot-base directories for more information. If you use Eclipse (with the m2eclipse plugin), just import the ot-base and jade repos as maven projects to browse their packages and classes.

If you have old versions of these repositoties installed, you will need to update them (```git pull && ./mvn_install.sh```).

Setup
-----

You can get OTI set up with the included shell script using the command 
```sh setup_oti.sh```. The script can also be used for reference if you want to do a
more custom installation. OTI runs inside a neo4j installation as a server
plugin, which exposes indexing and searching features via the standard neo4j
HTTP interface for plugin extensions. 

If plugins have changed, you may need to recompile them and restart neo4j for them
to show up:

        ./setup_oti.sh --recompile-plugin
        ./setup_oti.sh --restart-neo4j

Once you have a neo4j installation with the OTI plugin running, you can check
the available services using curl, for instance:

```
curl -v http://localhost:7474/db/data/
```

...will report a list of available plugin extensions and their services. OTI's plugins are "IndexServices" and "QueryServices".

Using OTI
-----

####Indexing

For OTI to be useful, you will need to index some nexsons. The "indexNexsons"
service provides a method to do that. It accepts a single parameter ```urls```,
which is a list of urls of nexson files to be indexed. A single nexson study is
assumed to be contained within a single nexson file. For more information:

```
curl -v http://localhost:7474/db/data/ext/IndexServices/graphdb/indexNexsons
```

There is a deprecated indexing service that only indexes a single NexSON URL at
a time:

```
curl -v http://localhost:7474/db/data/ext/IndexServices/graphdb/indexSingleNexson
```

It should no longer be used and the ```indexNexsons``` service should be used instead.

To remove existing nexsons, call ```unindexNexsons``` with a list of ids:

```
curl -v http://localhost:7474/db/data/ext/IndexServices/graphdb/unindexNexsons
```

A python script is provided to facilitate indexing all studies in the most recent commit to master in the [phylesystem](https://github.com/OpenTreeOfLife/phylesystem) repo. It takes no arguments:

```
python index_current_repo.py
```

The following example will index all the nexsons in the current public phylesystem:

```bash
OTI=http://localhost:7474/db/data
PHYLESYSTEM=http://api.opentreeoflife.org/phylesystem/
./index_current_repo.py $OTI $PHYLESYSTEM
```

####Querying

Querying is accomplished via the QueryServices plugin. It is currently possible to query for studies, trees, or tree tip nodes. You may search for any of these elements based on a variety of indexed properties. Currently, only simple, single-property queries are available.

For lists of queryable properties for each element type:

```
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/getSearchablePropertiesForStudies
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/getSearchablePropertiesForTrees
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/getSearchablePropertiesForTreeNodes
```

For more information on queries:

```
curl -v http://localhost:7474/db/data/ext/QueryServices/
```

Some example queries follow. Note, these will return empty results if no matching studies have been indexed:

```
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/singlePropertySearchForTrees/ -H "Content-type:Application/json" -d '{"property":"ot:ottTaxonName","value":"Carex"}'
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/singlePropertySearchForStudies/ -H "Content-type:Application/json" -d '{"property":"ot:studyPublicationReference","value":"vorontsova"}'
curl -X POST http://localhost:7474/db/data/ext/QueryServices/graphdb/singlePropertySearchForTreeNodes/ -H "Content-type:Application/json" -d '{"property":"ot:ottId","value":"1000455"}'
```

JAVAFLAGS="-Xmx30G"
HELPTEXT="usage:\nsetup_oti.sh <options>\\n\\t[--clean-db]\n\t[--force]\n\t[--update-oti]\n\t[--recompile-plugin]\n\t[--restart-neo4j]\n\t[-prefix <path>]\n\n"

while [ $# -gt 0 ]; do
	case "$1" in
		--clean-db) CLEANDB=true;;
		--force) FORCE=true;;
		--update-oti) UPDATE=true;;
		--recompile-plugin) RECOMPILE=true;;
		--restart-neo4j) RESTART_NEO4J=true;;
		-prefix) shift; PREFIX="$1";;
		--help) printf "$HELPTEXT"; exit 0;;
		*) printf "\nunrecognized argument: $1.\n"; printf "$HELPTEXT"; exit 1;
	esac
	shift
done

if [ ! $PREFIX ]; then
	PREFIX="../" # should fix the taxomachine script to put slashes in when dirs are appended
	if [ ! $FORCE ]; then
		printf "\nprefix is not set. the default prefix $PREFIX will be used. continue? y/n:"
		while [ true ]; do
			read RESP
			case "$RESP" in
				n) exit;;
				y) break;;
				*) printf "unrecognized input. uze ^C to exit script";;
			esac
		done
	fi
fi

#if [ ! -d $PREFIX ]; then
#	mkdir $PREFIX
#fi
cd $PREFIX
PREFIX=$(pwd)
printf "\nworking at prefix $PREFIX\n"

OSTYPE=$(uname -msr)

if echo $OSTYPE | grep "Linux" ; then
    LINUX=true
elif echo $OSTYPE | grep "darwin" ; then
    MAC=true
fi

OTI_NEO4J_HOME="$PREFIX/neo4j-oti"
OTI_NEO4J_DAEMON="$OTI_NEO4J_HOME/bin/neo4j"

# download neo4j if necessary
if [ ! -d $OTI_NEO4J_HOME ]; then
    cd "$HOME/Downloads"
    wget "http://download.neo4j.org/artifact?edition=community&version=1.9.5&distribution=tarball&dlid=2600508"
    tar -xvf "artifact?edition=community&version=1.9.5&distribution=tarball&dlid=2600508"
    printf "\ninstalling neo4j instance for oti at: $OTI_NEO4J_HOME\n"
    mv neo4j-community-1.9.5 $OTI_NEO4J_HOME
fi
printf "\nusing neo4j instance for oti at: $OTI_NEO4J_HOME\n"

### need to install git if not already present
### need to install maven if not already present

cd $PREFIX
OTI_HOME="$PREFIX/oti"

# clone the otu repo if necessary
if [ ! -d $OTI_HOME ]; then
    printf "\ninstalling oti at: $OTI_HOME\n"
    git clone git@github.com:OpenTreeOfLife/oti.git
fi

if [ $UPDATE ]; then
    printf "\ngetting latest updates from github master\n"
    cd $OTI_HOME
    git pull origin master
fi
printf "\nusing oti at: $OTI_HOME\n"

cd $OTI_HOME
OTI_PLUGIN_INSTALL_LOC="$OTI_NEO4J_HOME/plugins/oti-0.0.1-SNAPSHOT.jar"

# remove previous plugin if requested
if [ $RECOMPILE ] || [ $UPDATE ]; then
    rm -Rf $OTI_PLUGIN_INSTALL_LOC
fi

# recompile plugin if necessary
if [ ! -f $OTI_PLUGIN_INSTALL_LOC ]; then
    ./mvn_serverplugins.sh
    mv $OTI_HOME/target/oti-neo4j-plugins-0.0.1-SNAPSHOT.jar $OTI_PLUGIN_INSTALL_LOC
fi

OTI_DB="$OTI_NEO4J_HOME/data/graph.db"
if [ $CLEANDB ]; then
	printf "\nremoving the existing database at: $OTI_DB\n"
	rm -Rf $OTI_DB
fi

if [ $RESTART_NEO4J ]; then
    # start the neo4j. cannot have other running neo4j instances or this will fail!
    $OTI_NEO4J_DAEMON restart
fi

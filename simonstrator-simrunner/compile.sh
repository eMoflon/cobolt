#!/bin/bash
#
# This script builds a fat jar for simulations, either using repository versions 
# of the simonstrator platform or by pulling in dependencies via maven.
#
# It is also used for CI builds on the server.
#
# Bjoern Richerzhagen
#
# Copied from master on 2016-12-12
#

# $1: repo (api, overlays, peerfact)
# $2: branch
function compileMvnProject()
{
	# Do we need to build the API?
	if [[ $2 = "use-maven" ]]; then
		# no - ensure, that local version is deleted!
		rm -rf mvn-repo/maki/simonstrator-$1
		echo -e "\e[92mResolving API via maven.\e[39m"
	else
		echo -e "\e[92mBuilding $1 via git, using branch $2.\e[39m"
		git clone git@dev.kom.e-technik.tu-darmstadt.de:maki/simonstrator-$1.git --branch $2 --single-branch stage
		STATUS=$?
		if [ $STATUS -eq 0 ]; then
			echo -e "\e[92mGit: OK\e[39m"
		else
			echo -e "\e[31mCloning the $1 via git failed. Exiting...\e[39m"
			exit 1
		fi
		mv mvn-repo stage/
		cd stage
		mvn package -Dmaven.repo.local=mvn-repo -q
		STATUS=$?
		if [ $STATUS -eq 0 ]; then
			echo -e "\e[92mMaven Package: OK\e[39m"
		else
			echo -e "\e[31mPacking the $1 via Maven failed. Exiting...\e[39m"
			exit 1
		fi
		mvn install -Dmaven.repo.local=mvn-repo -q
		mv mvn-repo ../
		cd ../
		rm -rf stage
	fi
}

function buildProject() 
{
	mvn clean -q

	mkdir mvn-repo

	rm -rf stage
	
	# Do we need to build the API?
	compileMvnProject "api" $api

	# Do we need to build Peerfact?
	compileMvnProject "peerfact" $peerfact

	# Build Overlays
	compileMvnProject "overlays" $overlays

	# And finally - build the runner!
	mvn package -Dmaven.repo.local=mvn-repo -q
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		echo -e "\e[92mMaven Package: OK\e[39m"
	else
		echo -e "\e[31mPacking the SimRunner via Maven failed. Exiting...\e[39m"
		exit 1
	fi
	mv target/multirunner.jar ./

	echo " -- "
	echo -e "\e[92m Build completed. \e[39mThe resulting jar (with dependencies): multirunner.jar"
	echo -e " To use your build of peerfact, execute \e[92mjava -jar multirunner.jar\e[39m and pass your configuration."
	echo " -- "
}


function usage() 
{
cat << EOF
Usage: $0 [options] <overlays>

Options:
    -h          You're looking at it.
    -p <branch> Use the provided branch for the compilation of peerfactsim.
                If no branch is specified, maven resolves the dependency.
    -a <branch> Use the provided branch for the compilation of the API.
                If no branch is specified, maven resolves the dependency.

Parameters:
    <overlays>   Branch of the overlay-project to be used during compilation.
EOF
}


# defaults:
peerfact=use-maven
api=use-maven

while getopts “hp:a:” OPTION; do
	case $OPTION in
		h)
			usage
			exit 0
			;;
		p)
			peerfact=$OPTARG
			;;
		a)
			api=$OPTARG
			;;
		?)
			usage
			exit 1
			;;
		*)
			echo 'WHAT HAPPEN?'
			exit 2
			;;
	 esac
done

shift $(($OPTIND-1))
overlays=$*

if [[ -z $overlays ]]; then
	echo "ERROR: No branch for simonstrator-overlays given!"
	echo
	usage
	exit 3
fi

buildProject

exit 0


#!/bin/bash

function runSim()
{
	configfile=$1
	name=`basename $configfile`
	seed=$2
	outputFolder=$5
	jarfile=$3
	dryrun=$4
	variationFile=$6_$7
	variation=`echo $6 | sed 's/#/\ /g'`
	variationTwo=`echo $7 | sed 's/#/\ /g'`

	outputfile=outputs/${outputFolder}/$(echo {$configfile} | grep -o "[a-zA-Z0-9 -_;,.]*\.xml")___seed=${seed}__`date +%Y-%m-%dT%H:%M:%S`.output
	#echo "Output filename is: $outputfile" 

	if [[ $jarfile = "_no_jar_" ]]; then
		jararg="de.tud.kom.p2psim.SimulatorRunner"
	else
		jararg="-jar $jarfile"
	fi

	#export MAVEN_OPTS=-Xms12G
	#export MAVEN_OPTS=-Xmx16G
	
	echo "Starting $name with seed $seed and variation: $variation $variationTwo"
	mkdir -p `dirname $outputfile`
	date >> $outputfile
	
	if [[ $variationTwo = "" ]]; then
		echo  "java -Xms2G -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC ${jararg} ${configfile} variation:vanilla seed=${seed} ${variation} >> $outputfile" 
		if [ $dryrun = false ]; then
			#mvn exec:java -Dexec.mainClass="de.tud.kom.p2psim.SimulatorRunner" -Dexec.args="${configfile} seed=${seed} ${variation}" >> $outputfile
			java -Xms2G -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -classpath ../simonstrator-peerfact_lib/* ${jararg} ${configfile} variation:vanilla seed=${seed} ${variation} >> $outputfile
		fi
	else
		echo  "java -Xms2G -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC ${jararg} ${configfile} variation:vanilla seed=${seed} ${variation} ${variationTwo} >> $outputfile"
		if [ $dryrun = false ]; then 
			#mvn exec:java -Dexec.mainClass="de.tud.kom.p2psim.SimulatorRunner" -Dexec.args="${configfile} seed=${seed} ${variation} ${variationTwo}" >> $outputfile
			java -Xms2G -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -classpath ../simonstrator-peerfact_lib/* ${jararg} ${configfile} variation:vanilla seed=${seed} ${variation} ${variationTwo} >> $outputfile
			
		fi
	fi
		
	if [ $? != 0 ]; then
		echo -n -e '\033[38;5;196m'
		echo -n -e " ERROR: The execution of $name with seed $seed failed!"
		echo -e '\033[0m'
	else
		echo "Finished $name with seed $seed"
	fi

	date >> $outputfile
}

function usage() {
cat << EOF
Usage: $0 [options] <configs>

Options:
    -h          You're looking at it.
    -n          Dry run: print the generated commands, but don't execute them.
    -p <num>    Start at most <num> simulator instances in parallel.
                Defaults to "3".
    -j <jar>    Start simulator by running the given jar file, otherwise the
                simulator is run directly.
 	-o <folder> Write output into folder with given name.
                Defaults is batchoutput.
    -s <seeds>  <seeds> is a comma separated list of RNG seeds for each
                configuration. Defaults to "123".
    -v <vars>   A variations .txt file that includes multiple variable assignments,
                each line will be treated as a new simulation run. This allows easy
                batch processing of configs where only a limited number of params
                is varied.
    -w <workloads> Another .txt file with variable assignments. The variants in this
                file will be permutated with the variations passed with -v.

Parameters:
    <configs>   A list of .xml files containing configurations to run.
                If a directory is given, all contained .xml files will be used,
                but no recursion is applied and files starting with a
                underscore are ignored.
EOF
}


# defaults:
maxParallel=3
seeds=123
hasVariations=false
hasVariationsTwo=false
variations=none
variationsTwo=none
outputFolder=batchoutput
jarfile=_no_jar_
outputDir=
dryrun=false

while getopts “hnp:j:o:s:v:w:” OPTION; do
	case $OPTION in
		h)
			usage
			exit 0
			;;
		n)
			dryrun=true
			;;
		p)
			maxParallel=$OPTARG
			;;
		j)
			jarfile=$OPTARG
			;;
		s)
			seeds=`echo $OPTARG | tr ',' '\n'`
			;;
		o)
			outputFolder=$OPTARG
			;;
		v)
			variations=$OPTARG
			hasVariations=true
			;;
		w)
			variationsTwo=$OPTARG
			hasVariationsTwo=true
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
configargs=$*

#echo maxParallel $maxParallel
#echo seeds $seeds
#echo jarfile $jarfile
#echo configargs $configargs
#echo dryrun $dryrun

if [[ -z $configargs ]]; then
	echo ERROR: No configurations given.
	echo
	usage
	exit 3
fi

declare -a files
declare -a commands
shopt -s extglob

for arg in $configargs; do
	if [[ -d $arg ]]; then
		files=(${files[@]} ${arg}/!(_*).xml)
	else
		files=(${files[@]} $arg)
	fi
done


if $hasVariationsTwo; then
	for seed in $seeds; do
		while read variationTwo
		do
			variationTwo=`echo $variationTwo | sed 's/\ /#/g' | sed 's/^[ \t]*//;s/[ \t]*$//'`
			echo ${variationTwo}
			while read variation
			do
				variation=`echo $variation | sed 's/\ /#/g' | sed 's/^[ \t]*//;s/[ \t]*$//'`
				for config in ${files[@]}; do
					param="'$config' '$seed' '$jarfile' '$dryrun' '$outputFolder' '$variation' '$variationTwo'"
					echo ${param}
					commands[${#commands[*]}]="$param"
				done
			done < $variations
		done < $variationsTwo
	done
elif $hasVariations; then
	for seed in $seeds; do
		while read variation
		do
			variation=`echo $variation | sed 's/\ /#/g' | sed 's/^[ \t]*//;s/[ \t]*$//'`
			echo $variation
			for config in ${files[@]}; do
				param="'$config' '$seed' '$jarfile' '$dryrun' '$outputFolder' '$variation' ''"
				commands[${#commands[*]}]="$param"
			done
		done < $variations
	done
else
	for seed in $seeds; do
		for config in ${files[@]}; do
			param="'$config' '$seed' '$jarfile' '$dryrun' '$outputFolder' '' ''"
			commands[${#commands[*]}]="$param"
		done
	done
fi

#export function runSim to execute from xargs
export -f runSim

for command in "${commands[@]}"; do echo $command; done | xargs -P $maxParallel -I{} bash -c runSim\ {\}

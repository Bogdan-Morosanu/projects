#!/bin/bash

let winner=0
let min=1000
declare -a execTime 
# array which will hold execution 
# time for different thead numbers

TIMEFORMAT="%U"

GCC_FLAGS="-std=gnu99 -Wall -Werror -O3"
GCC_LIBS="-lpthread -lm"

# first compile test version 
# this test version will accept as the first
# command line option the number of threads
gcc $GCC_FLAGS test_sieve.c $GCC_LIBS -o sieve


for runs in {1..200}
do
    for threads in {1..20}
    do
	cntTime="$(time  (sieve $threads 100000) 2>&1 1>/dev/null )"
	addition="${execTime[$threads]}+$cntTime"
	execTime[$threads]=`awk "BEGIN { print($addition);}"`
    done
    echo "done with $runs profiling runs out of 200..."
done

for threads in {1..20}
do
    division="${execTime[$threads]}/20.0"
    execTime[$threads]=`awk "BEGIN { print($division); }"`
    awk "BEGIN {if(${execTime[$threads]}<$min) exit 0;  else exit 1; }" \
	&& min=${execTime[$threads]} && winner=$threads

done

echo "final winner $winner threads with $min sec average execution time"
echo "now compiling optimised version of code..."

rm sieve
gcc -DTHREADS=$winner $GCC_FLAGS sieve.c $GCC_LIBS -o sieve

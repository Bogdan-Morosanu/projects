test_sieve: test_sieve.c
	gcc -std=gnu99 -Wall -Werror test_sieve.c -lpthread -lm -o sieve

sieve: sieve.c
	gcc -DTHREADS=$(THREADS) -std=gnu99 -Wall -Werror sieve.c -lpthread -lm -o sieve

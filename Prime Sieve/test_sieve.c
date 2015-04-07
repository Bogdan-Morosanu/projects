#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <stdint.h>
#include <math.h>

#include <unistd.h>
#include <sys/wait.h>
#include <pthread.h>

#define LOG_NAME "primes.txt"
#define COL_NUM 10

struct sub_array {
  char *markers;
  int start;
  int stop;
  int size;
};

struct int_array {
  char *markers;
  int size;
};

struct int_array numbers;


void *mark_multiples( void *args );
struct sub_array *create_subarr( struct int_array *all, int cnt_th, int th_num );
void log_contents( struct int_array *all );

int main(int argc, char **argv) {
  if(argc != 3) {
    printf("usage %s <thread_number> <upper_bound>\n", argv[0]);
    exit(0);
  }
  
  int th_num = atoi( argv[1] );
  pthread_t *threads = malloc( sizeof(pthread_t) * th_num);
  assert( threads && "not enough memory!");
  
  // Setup global buffer
  int upper_bound = atoi( argv[2] );
  char *buff = calloc( sizeof(char), upper_bound ); 
  assert( buff && "not enough memory!");
  numbers.markers = buff;
  numbers.size = upper_bound;
  
  // Delegate work to threads
  for(int i = 0; i < th_num; i++ ) {
    struct sub_array *context = create_subarr( &numbers, i, th_num );
    pthread_create( threads + i, NULL, mark_multiples, context );
  }
  
  // Now wait for threads to finish working
  for( int i = 0; i < th_num; i++ ){
    pthread_join(threads[i], NULL);
  }

  log_contents( &numbers );
  return 0;
}

// Sieve approach. each thread marks as non-prime multiples of args that
// are within range allocated to it.
void *mark_multiples(void *args) {
  struct sub_array *sub_arr = (struct sub_array*) args;
  int last_factor = sqrt(sub_arr->size) + 1; //stopping point of sieve
  
  for(int i = 2; i < last_factor; i++) {
    // compute starting index into sieve
    int rem = sub_arr->start % i; 
    int marked = sub_arr->start + ((rem==0) ? 0 : (i-rem)); // adjust for remainder 
    marked = (marked == 0 || marked == i) ? 2*i : marked; 
    // don't mark zero or yourself! 
    //(zero edge case happens for sub_arr->start == 0)

    // marked is now the first elem of sub_arr divisible by i different from itself
    while( marked < sub_arr->stop ) { 
      sub_arr->markers[marked] = 1; // mark off numbers divisible
      marked += i; // step onward throgh the array.
    }
  }
  return NULL; // no real return value
}

struct sub_array *create_subarr(struct int_array *all, int cnt_thread, int th_num) {
  struct sub_array *this = malloc(sizeof(struct sub_array));
  assert( this && "not enough memory!" );
  
  int block_size = all->size / th_num + 1; 
  // notice we don't want to leave our last thread with much more work 
  // to do than the rest, so we distribute the workload evenly via the +1
  // and will have our last thread have less work to do than the rest.
  // probablly will not signifficantly affect runtime anyways.
  
  this->start = cnt_thread * block_size;
  this->stop = this->start + block_size;
  this->markers = all->markers;
  this->size = all->size;
  
  // watchout for the last thread, it has less work to do
  if( cnt_thread + 1 == th_num ) {
    this->stop = this->size;
  }
  
  return this;
}

void log_contents( struct int_array *all ) {
  FILE *log = fopen(LOG_NAME, "w");
  assert(log && "unable to open/create log file!");
  
  // find out relative column width
  int last = all->size;
  int digits = 1;
  int accum = 1;
  while( (accum *= 10) < last ) digits++;
  
  // create format string
  char format[100];
  snprintf(format, 100, "%%%dd ", digits);
  
  int logged = 0;
  for(int i = 2; i < all->size; i++) {
    if( !all->markers[i] ) {
      // i unmarked!, is prime
      fprintf(log, format, i);
      logged++;
      if( logged % COL_NUM == 0 ) {
	fprintf(log, "\n");
      }
    }
  }
  fclose(log);
}


/** 
    File hasher and authentifier utility

    Notes for further modifying:
        sizeof(block_t) must be a multiple of sizeof(hash_t)

*/

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <limits.h>
#include <stdint.h>


#define IV 0
#define DEBUG 0
#define FILE_LEN_MAX 512

typedef uint32_t hash_t;
typedef uint64_t block_t;


hash_t md_hash(void*, size_t);
void test_typedef_validity(void);

int main (int argc, char** argv) {
  test_typedef_validity(); 
  // test the typedefs are good for the implementation
  
  if( argc == 1 ) {
    printf("usage %s <files_to_authentify>\n", argv[0]);
    exit(0);
  }
  
  argv++; //skip program name
  FILE *hash_out = fopen("hash_log.md","r");
  assert( hash_out );
  
  // get all contents of hashes
  fseek(hash_out, 0, SEEK_END);
  int hashes_len = ftell(hash_out);
  fseek(hash_out, 0, SEEK_SET);
  char *hashes = malloc( hashes_len+1 ); // don't forget the null terminator
  assert(hashes);
  hashes[hashes_len] = '\0';

  fread( hashes, 1, hashes_len, hash_out );
  fclose(hash_out);
  

  while( --argc > 0 ) {
    char *nxt_file = *argv++;
    FILE *file_in = fopen(nxt_file, "r");
    
    if( file_in == NULL ) {
      printf("file %s not found!\n", nxt_file);
      exit(1);
    }
    
    char *pos_found = strstr(hashes, nxt_file);
    if( pos_found == NULL ) {
      printf("[FAIL] for %s : no valid identifier found\n", nxt_file);
      
      fclose(file_in); // cleanup and continue processing files
      continue;
    }
    
    // now compute hash of file contents
    fseek( file_in, 0, SEEK_END);
    int fsize = (int)ftell(file_in);
    void *buff = malloc( fsize );
    // obviously works only for small text files
    // must take a sequential approach for huge files
    assert(buff);
    
    // read file
    fseek( file_in, 0, SEEK_SET );
    fread( buff, 1, fsize, file_in );
    
    hash_t hash = md_hash( buff, fsize );
    hash_t comp;
    char format[FILE_LEN_MAX];
    sprintf(format, "%s : %%x", nxt_file); 
    // setup format to dump filename and capture hex hash
    sscanf(pos_found, format, &comp);
   
    if( comp == hash ) {
      printf("[SUCCESS] %s has a good signature\n", nxt_file);
    } else {
      printf("[FAIL] %s was found but signature is invalid\n", nxt_file);
    }
    
    fprintf( hash_out, "%s : %x\n", nxt_file, hash);
    
    free(buff);
    fclose( file_in );
  }
  

  return 0;
}

/**
 * xors block with rand() function to obtain a uniform output distribution
 * of bits. implementation relies on outside seeding of the rand function
 * and this is done in the compression function -the only user of xor_with_rand.
 * implementation probably would have been clearer using char* rather than
 * unsinged* but we choose a middle ground between clarity and speed.
 */
static inline void xor_with_rand (block_t *block, unsigned int *seed) {
  int repeat = sizeof(block_t) / sizeof(unsigned); // actual times we must xor
  unsigned *bptr = (unsigned*) block;  // iterator
 
  while( repeat-- > 0 ) {
    *bptr++ ^= (unsigned)rand_r(seed);
  }
}

/**
 * our compression function takes a message block
 * and hashes it using the already existing chaining
 * variable h	
 */
static void compression_fn(block_t block, hash_t *hash) {	
  unsigned int seed = (unsigned)*hash;
  xor_with_rand(&block, &seed); // scramble the block before chaining	

  int repeat = sizeof(block_t) / sizeof(hash_t); // actual times we must xor
  hash_t *iter = (hash_t*)&block; // iterator over the block

  while( repeat-- > 0 ) {
    *hash ^= *iter++; // cumulate into the existing hash
  }
}

hash_t md_hash(void *addr, size_t sz) {
  hash_t hash = IV;   //Initialising hash with IV
	
  /* first we shall deal with the prefix of our
   * message that is a multiple of the block size
   */
  unsigned long long repeat = sz/sizeof(block_t);
  block_t *block_iter = (block_t*)addr;
  
  // we now chain the compression function over all
  // complete blocks of the message, reducing them in the hash
  while( repeat-- > 0 ) {
    compression_fn( *block_iter++, &hash ); 
  }
  
  // we must now padd the remaining bits of the message
  // to create a valid block
  int left_out = sz % sizeof(block_t);

  if(left_out > 0) {
    // notice our block iterator points to the last,
    // incomplete block of our message, no need to recompute the value

    block_t pad = 0;
    memcpy( &pad, block_iter, left_out );
    
    // now we must set the end marker pad of 10...00.
    // since left_out points to the first char of this pad
    // we need only memset the MSB
    memset( &pad + left_out, 0x80, 1 );
    compression_fn( pad, &hash );
  }

	
  //If our message length is a multiple of the blocksize and
  //it ends with a "100...00" we have a collision for this message
  //and the one containing the same bitpattern but without the
  //"100...00" at the end. It is still probably faster to rehash
  //these special cases rather than append a control pad to each
  //hashed value. 
  return hash;
}

void test_typedef_validity(void) {
  // enforce algorithm division requirements
  int rem =  sizeof(block_t) % sizeof( hash_t );
  rem += sizeof(block_t) % sizeof( unsigned );
  if( rem != 0 ) {
    perror("it seems the redeffinition of the block_t and hash_t \n"
           "data types does not conform to the md and our implementation\n"
	   "requirements : block_t's size must me a multiple of both\n"
	   "hash_t's size and unsigned's size. suggested deffinitions are:\n"
	   "uint64_t for block_t and uint32_t for hash_t\n");
    exit(-1);
  }
}

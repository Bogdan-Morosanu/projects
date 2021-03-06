#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

/** Header Declaration Will be !Immutable! 
 *  but compiled source will use a mutable version
 *  such that it can actually construct and manipulate
 *  the list objects without calling memcpy or some other   	
 *  arcane methods
 * 
 *  Client declaration follows : 
 *		struct list {
 *			const void* const elem;
 *			const struct list* const next;
 *		};
 *
 *		typedef struct list* List;
 *
 *		List cons( void* elem, List ls );
 *		void* head( List ls );
 *		List tail( List ls );
 *
 *		List map( List ls, void* (*fn)(const void* elem) );
 *		List filter( List ls, int (*predicate)(const void* elem));
 *		void* reduce( List ls, void* (*reducer)(void* zero,const void* elem));
 *		void* foldLeft( List ls, void* zero,
 *					void* (*reducer)(void* zr,const void* elem) );
 *
 *		void iterate( List ls, void (*to_do)(const void* elem) );			
 *			
 *		void destroy_list( List ls, void (*free_fn)(void*)  );
 *
 */

 
struct list {
	void* elem;
	struct list* next;
};

typedef struct list* List;

List cons( void* elem, List ls );
List merge_lists( List first, List second );

/** Very important, elems is a null terminated pointer array */
List toList( void** elems );


void iterate( List ls, void (*to_do)(void*) );			
int  count_list( List ls );


inline void* head( List ls );
inline List tail( List ls );

List map( List ls, void* (*fn)(void* elem) );
List filter( List ls, int (*predicate)(void* elem));

void* reduce( List ls, void* (*reducer)(void*,void*));
void* foldLeft( List ls, void* zero,
			void* (*reducer)(void*,void*) );
			
void destroy_list( List ls, void (*free_fn)(void*)  );

/** Helper functions 
 * written with accumulator value to be tail recursive.
 */
static int count_tailrec( int zero, List ls ) ;
	

/** This file is now a Module, no need for a main
int main( int argc, char** argv ) {
	List ls = cons("one", cons( "two", cons( "three", NULL)));
	List filtered = filter(ls, len_filter);

	puts("initial list : ");
	iterate( ls, print_str_and_len );
	
	int total_len = 0;
	foldLeft( ls, &total_len, accumulate_len );
		
	printf("of total length : %d\n", total_len);
	
	puts("filtered by length < 4 : " );
	iterate( filtered, print_str_and_len );
	
	total_len = 0;
	foldLeft( filtered, &total_len, accumulate_len );
	printf("of total length : %d\n", total_len);
	
	
	destroy_list( ls, NULL );
	destroy_list( filtered, NULL );
	
	return 0;
}

*/

List cons( void* elem, List ls ) {
	List this = malloc( sizeof(struct list) );
	assert( this );
	
	this->elem = elem;
	this->next = ls;
		
	return this;
}

List toList( void** elems ) {
	List ls = NULL;
	while( *elems ) {
		ls = cons( *elems++, ls );
	}
	return ls;
}

List merge_lists( List first, List second ) {
	List ls = NULL;
	while( head(second) ) {
		ls = cons( head(second), ls);
		second = tail(second);
	}
	
	while( head(first) ) {
		ls = cons( head(first), ls);
		first = tail(first);
	}
	
	return ls;
}

void iterate( List ls, void (*to_do)(void*) ) {
	if( ls == NULL ) {
		return;
	} else {
		to_do( head(ls) );
		iterate( tail(ls), to_do );
	}
}


int count_list( List ls ) {
	return count_tailrec( 0, ls );
}

static int count_tailrec( int zero, List ls ) {
	if( ls == NULL ) {
		return zero;
	} else {
		return count_tailrec( zero+1, tail(ls) );
	}
}

inline void* head( List ls ) {
	return (ls) ? ls->elem : NULL;
}

inline List tail( List ls ) {
	if(ls) {
		return ls->next;
	} else {
		perror("tail( NULL )");
		return NULL;
	}
}


List map( List ls, void* (*fn)(void* elem) ) {
	if( ls == NULL ) {
		return NULL; //base case
	} else { 
		//recursive step
		return cons( fn(head(ls)), map( tail(ls), fn) );	
	}	
}


List filter( List ls, int (*predicate)(void* elem)) {
	if( ls == NULL ) {
		return NULL;
	
	} else if( predicate(head(ls)) ) {
		return cons( head(ls), filter(tail(ls), predicate) );
	
	} else {
		return filter( tail(ls), predicate );
	}	
}

void* reduce( List ls, 
			void* (*reducer)(void*,void*)) {
						
	return foldLeft( ls, NULL, reducer);
}

void* foldLeft( List ls, void* zero,
				void* (*reducer)(void*,void*) ) {
	if( ls == NULL ) {
		return zero; //base case
	
	} else { //recursively accumulate values
		return foldLeft( tail(ls), reducer(zero, head(ls)), reducer);
	}
}


void destroy_list( List ls, void (*free_fn)(void*) ) {
	List nxt = ls;
	while( nxt ) {
		if(free_fn) { //free elem only if we have to
			free_fn(nxt->elem);
		}
		
		List q = nxt->next;
		free(nxt);
		nxt = q;
	}
}


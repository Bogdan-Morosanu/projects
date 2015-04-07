#ifndef CONS_NIL_LIST
#define CONS_NIL_LIST

struct list {
	const void* const elem;
	const struct list* const next;
};

typedef struct list* List;

List cons( void* elem, List ls );
List merge_lists( List first, List second );

/** Very important, elems is a null terminated pointer array */
List toList( void** elems );

void iterate( List ls, void (*to_do)(const void* elem) );			
int count_list( List ls );

void* head( List ls );
List tail( List ls );

List map( List ls, void* (*fn)(const void* elem) );
List filter( List ls, int (*predicate)(const void* elem));
void* reduce( List ls, void* (*reducer)(void* zero,const void* elem));
void* foldLeft( List ls, void* zero,
			void* (*reducer)(void* zr,const void* elem) );

			
void destroy_list( List ls, void (*free_fn)(void*)  );

#endif

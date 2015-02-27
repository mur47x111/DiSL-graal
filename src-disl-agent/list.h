/***
 * An implementation of a doubly-linked list. This implementation requires
 * that the link pointers be embedded in the list item structures themselves.
 * This idea comes from the Linux kernel implementation of a list.
 *
 * @author Lubomir Bulej
 */

#ifndef _LIST_H_
#define _LIST_H_

#include <assert.h>
#include <stdio.h>
#include <stdbool.h>

/****************************************************************************\
| PUBLIC DEFS                                                                |
\****************************************************************************/

/*
 * List head/item structure.
 */
struct list {
	struct list * prev;
	struct list * next;
};


/*
 * Various types of visitor/observer functions.
 */
typedef void (* list_destroy_fn) (struct list * item, void * data);
typedef void (* list_visit_fn) (struct list * item, void * data);
typedef int (* list_match_fn) (struct list * item, void * data);


/*
 * Static list head/item initializer.
 */
#define LIST_INIT(name) \
{ \
	.prev = &(name), \
	.next = &(name) \
}


/****************************************************************************\
| PRIVATE INLINE FUNCTIONS AND MACROS                                        |
\****************************************************************************/

/**
 * Calculates an offset of a member in a struct.
 *
 * @param type
 *	the type of the container struct a member is embedded in
 * @param member
 *	the name of the member within the struct
 */
#define __offset_of(type, member) \
	((size_t) &((type *) 0)->member)


/**
 * Calculates the pointer to the containing structure based on the offset
 * of the given member in that structure. Casts the resulting pointer to
 * the type of the containing structure.
 *
 * @param ptr
 *	the pointer to the member
 * @param type
 *	the type of the container struct the member is embedded in
 * @param member
 *	the name of the member within the containing struct
 */
#define __container_of(ptr, type, member) \
({ \
	const typeof (((type *) 0)->member) * __mptr = (ptr); \
	(type *) ((char *) __mptr - __offset_of (type, member)); \
})




/**
 * Initializes a list head/item. An empty list head or an unlinked list item
 * points back to itself, which simplifies runtime checks. Returns the
 * itialized item back to the caller.
 *
 * @param head
 *	the list head/item to initialize
 */
static inline struct list *
__list_init (struct list * head) {
	head->prev = head;
	head->next = head;

	return head;
}


/**
 * Inserts an item into a list by linking it between its immediate predecessor
 * and successor items, and returns back the inserted item. The pointer from
 * the successor is linked first, so there can be a moment when the new item
 * is visible in the list during backward (but not forward) traversal, but this
 * should be handled by the caller.
 *
 * @param item
 *	the item to insert
 * @param pred
 *	the predecessor of the item
 * @param succ
 *	the successor of the item
 */
static inline struct list *
__list_insert_between (struct list * item, struct list * pred, struct list * succ) {
	item->next = succ;
	item->prev = pred;

	succ->prev = item;
	pred->next = item;

	return item;
}


/**
 * Removes item(s) from a list by linking together the items immediately
 * before and after the item(s) being removed. The backward pointer is
 * linked first, because lists are less often traversed backwards. The
 * forward pointer is linked last and when that happens, the list is
 * completely linked. The caller should make sure this is not a problem.
 *
 * @param pred
 *	the item preceding the item(s) to be removed
 * @param succ
 *	the item succeeding the item(s) to be removed
 */
static inline void
__list_remove_between (struct list * pred, struct list * succ) {
	succ->prev = pred;
	pred->next = succ;
}


/****************************************************************************\
| INLINE FUNCTIONS                                                           |
\****************************************************************************/

/**
 * Initializes the given list and returns it back to the caller.
 *
 * @param head
 *	the list head/item to initialize
 */
static inline struct list *
list_init (struct list * head) {
	assert (head != NULL);

	//

	return __list_init (head);
}


/**
 * Returns true if the given list is empty, false otherwise.
 *
 * @param head
 * 	the list to test
 */
static inline bool
list_is_empty (struct list * head) {
	assert (head != NULL);

	//

	return head->next == head;
}


/**
 * Returns a typed structure from the given list item.
 *
 * @param ptr
 *	pointer to struct list
 * @param type
 *	the type of the struct the struct list is embedded in
 * @param member
 *	the name of the struct list field within the containing struct
 */
#define list_item(ptr, type, member) \
	__container_of (ptr, type, member)


/**
 * Inserts a new item to the list after the specified item and
 * returns back the new item.
 *
 * @param item
 *	the item to insert
 * @param pred
 *	the item after which to add the new item
 */
static inline struct list *
list_insert_after (struct list * item, struct list * pred) {
	assert (item != NULL && pred != NULL);

	//

	return __list_insert_between (item, pred, pred->next);
}


/**
 * Inserts a new item to the list before the specified item and
 * returns back the new item.
 *
 * @param item
 *	the item to insert
 * @param succ
 *	the item before which to add the new item
 */
static inline struct list *
list_insert_before (struct list * item, struct list * succ) {
	assert (item != NULL && succ != NULL);

	//

	return __list_insert_between (item, succ->prev, succ);
}


/**
 * Removes the given item from the list and reinitializes it to
 * make it look like an empty list. Then it returns back the
 * removed item.
 *
 * @param item
 *	the item to remove
 */
static inline struct list *
list_remove (struct list * item) {
	assert (item != NULL);

	//

	__list_remove_between (item->prev, item->next);
	__list_init (item);

	return item;
}


/**
 * Removes the successor of the given list item from the list and
 * returns it to the caller. The item is assumed to have a valid
 * successor.
 *
 * @param item
 *	the list item whose successor to remove
 */
static inline struct list *
list_remove_after (struct list * item) {
	assert (item != NULL);

	//

	return list_remove (item->next);
}


/**
 * Removes the predecessor of the given list item from the list and
 * returns it to the caller. The item is assumed to have a valid
 * predecessor.
 *
 * @param item
 *	the list item whose predecessor to remove
 */
static inline struct list *
list_remove_before (struct list * item) {
	assert (item != NULL);

	//

	return list_remove (item->prev);
}


/**
 * Iterates over the given list.
 *
 * @param curr
 *	pointer to struct list to use as a loop counter
 * @param head
 *	the head of the list to iterate over
 */
#define list_for_each(curr, head) \
	assert ((head) != NULL); \
	for (curr = (head)->next; curr != (head); curr = curr->next)


/**
 * Iterates over the given list. This version is safe with respect
 * to removal of a list item.
 *
 * @param curr
 *	pointer to struct list to use as a loop counter
 * @param next
 *	pinter to struct list to hold the pointer to next item
 * @param head
 *	the head of the list to iterate over
 */
#define list_for_each_safe(curr, next, head) \
	assert ((head) != NULL); \
	for (curr = (head)->next, next = curr->next; \
		curr != (head); curr = next, next = curr->next)


/**
 * Iterates over the given list in reverse.
 *
 * @param curr
 *	pointer to struct list to use as a loop counter
 * @param head
 *	the head of the list to iterate over
 */
#define list_for_each_reverse(curr, head) \
	assert ((head) != NULL); \
	for (curr = (head)->prev; curr != (head); curr = curr->prev)


/**
 * Iterates over the given list of typed entries.
 *
 * @param curr
 *	pointer to (type) to use for loop control
 * @param head
 *	the head of the list to iterate over
 * @param member
 *	the name of the struct list member within the containing type
 */
#define list_for_each_item(curr, head, member) \
	assert ((head) != NULL); \
	for ( \
		curr = list_item ((head)->next, typeof (* curr), member); \
		(&curr->member) != (head); \
		curr = list_item (curr->member.next, typeof (* curr), member) \
	)


/**
 * Iterates over the given list of typed entries in reverse.
 *
 * @param curr
 *	pointer to (type) to use for loop control
 * @param head
 *	the head of the list to iterate over
 * @param member
 *	the name of the struct list member within the containing type
 */
#define list_for_each_item_reverse(curr, head, member) \
	assert ((head) != NULL); \
	for ( \
		curr = list_item ((head)->prev, typeof (* curr), member); \
		(&curr->member) != (head); \
		curr = list_item (curr->member.prev, typeof (* curr), member) \
	)


/**
 * Destroys the given list by removing all items from the list
 * and calling a destroy function on each item. The head of the
 * list is not considered to be a valid item, so it is not
 * destroyed.
 *
 * @param head
 *	the list to destroy
 * @param destroy
 *	the destructor function to call on each item
 * @param data
 *	additional data for the destroy callback
 */
static inline void
list_destroy (struct list * head, list_destroy_fn destroy, void * data) {
	assert (head != NULL);

	//

	while (! list_is_empty (head)) {
		// unlink head successor from the list and destroy it
		destroy (list_remove_after (head), data);
	}
}


/**
 * Walks through the given list and calls a visitor function
 * on each list item.
 *
 * @param head
 *	the list to walk
 * @param visit
 *	the function to call with each item
 * @param data
 *	additional data for the visit callback
 */
static inline void
list_walk (struct list * head, list_visit_fn visit, void * data) {
	assert (head != NULL);

	//

	struct list * item;
	list_for_each (item, head) {
		visit (item, data);
	}
}


/**
 * Walks through the given list and calls a match function on each item.
 * If the match function returns true, the matching item is returned to the
 * caller and the traversal stops. Returns NULL when the traversal reaches
 * the end of the list without finding a match.
 *
 * @param head
 *	the list to search
 * @param match
 *	the function to call with each item
 * @param data:
 *	additional data for the match callback
 */
static inline struct list *
list_find (struct list * head, list_match_fn match, void * data) {
	assert (head != NULL);

	//

	struct list * item;
	list_for_each (item, head) {
		if (match (item, data))
			return item;
	}

	return NULL;
}

#endif /* _LIST_H_ */


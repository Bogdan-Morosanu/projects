/*
 * blockstore.hpp
 *
 *  Created on: Feb 9, 2016
 *      Author: moro
 */

#ifndef INCLUDE_MEMORY_BLOCKSTORE_HPP_
#define INCLUDE_MEMORY_BLOCKSTORE_HPP_

namespace afl {
    /// @brief holds custom memory allocators
    namespace mem {
        // TODO implement
        template <typename ... Contents>
        class CacheLine;

        /// @brief returns the size in bytes needed
        /// to hold an integer that can count up to capacity.
        constexpr std::size_t tag_size(const std::size_t capacity);

        /// @brief models a free store allocating
        /// fixed size blocks.
        template <std::size_t BlockSizeHint,
                  std::size_t Capacity,
                  typename AlignType>
        class BlockStore final {
        public:
            BlockStore (): used(0), available(0), top(data) {}

            ~BlockStore () {} // TODO maybe check used > 0, throw leak ?

            // TODO decide on failure policy,
            // maybe make throwing one as well ?
            void *malloc(void) noexcept;

            void free(void *vp) noexcept;

        private:
            /// @brief currently allocated block numbers
            std::size_t used;

            /// @brief currently initialized, but free blocks in store.
            std::size_t available;

            /// @brief next block address
            void *top;

            static const std::size_t BLOCK_SIZE = (BlockSizeHint > sizeof(void**)) ?
                                                                     BlockSizeHint : sizeof(void**);

            static const std::size_t MAX_BLOCKS = Capacity;

            alignas(alignof(AlignType)) char data[BLOCK_SIZE * Capacity];
        };


    } /* namespace mem */
} /* namespace afl */

#include "blockstore-inl.hpp"

#endif /* INCLUDE_MEMORY_BLOCKSTORE_HPP_ */

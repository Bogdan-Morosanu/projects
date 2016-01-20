/*
 * magic_range.hpp
 *
 *  Created on: Jan 16, 2016
 *      Author: moro
 */

#include <array>
#include <functional>
#include <type_traits>

#ifndef INCLUDE_TMP_MAGIC_RANGE_HPP_
#define INCLUDE_TMP_MAGIC_RANGE_HPP_

namespace afl {
	namespace tmp {
		/// @brief hepler function used by mapped_range,
		///	use this as second template argument to create
		/// a range filled with [0... Size-1] in increasing
		/// order.
		int constexpr ident(std::size_t x) { return x; }

		/// @brief instantiates a compile time std::array
		/// of size N with content of index x determined
		/// by Mapper(x).
		/// this is the base case where the recursion terminates
		/// after some assembly output inspection from g++
		/// with the -O3 flag turned on, the diference between
		/// this solution and the plain old for loop with low
		/// values of size is usually just that this solution
		/// generates Size instructions of load immediate into ram
		/// e.g. 	mov	DWORD PTR [rsp], 0
		/// whereas the for loop one generates a static buffer
		/// and then executes Size / 4 moves into a mxx register
		/// from the buffer and then another write from the mxx
		/// into ram.
		/// e.g.	movdqa	xmm0, XMMWORD PTR .LC1[rip]
		///     	movaps	XMMWORD PTR [rsp+48], xmm0
		/// with .LC1 being the label of the static buffer.
		/// TODO test which is more effective, though probably the
		/// MMX one is better...
		template <typename ValType,
				  ValType(*Mapper)(std::size_t),
				  std::size_t Size, std::size_t... Arr>
		constexpr typename
		std::enable_if<Size == sizeof...(Arr),
					   std::array<ValType, Size>>::type
		mapped_range() {
			return std::array<ValType, Size>{{ Mapper(Arr)... }};
		}

		/// @brief instantiates a compile time std::array
		/// of size N with content of index x determined
		/// by Mapper(x).
		/// this is the recursive case where we call another
		/// template instantiation.
		template <typename ValType,
				  ValType(*Mapper)(std::size_t),
				  std::size_t Size, std::size_t... Arr>
		constexpr typename
		std::enable_if<Size != sizeof...(Arr),
					   std::array<ValType, Size>>::type
		mapped_range() {
			return mapped_range<ValType, Mapper, Size, Arr..., sizeof...(Arr)>();
		}

	}
}


#endif /* INCLUDE_TMP_MAGIC_RANGE_HPP_ */

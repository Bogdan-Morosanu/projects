/*
 * matiter.hpp
 *
 *  Created on: Feb 10, 2016
 *      Author: moro
 */

#ifndef INCLUDE_MATITER_HPP_
#define INCLUDE_MATITER_HPP_

#include "mat.hpp"

namespace afl {
  namespace mem {
    /// @brief implements a linear sequence of
    /// elements in a buffer
    template<typename ElType>
    class Seq {
    public:
      Seq(ElType *begin, ElType *end)
	: b(begin), e(end) {}

      ElType& operator * () { return *b; }

      ElType operator * () const { return *b; }

      Seq end() const { return Seq(e, e); }

      bool operator == (const Seq& rhs) const
      { return b == rhs.b;  }

      Seq& operator ++ ()
      {
	++b;
	return *this;
      }

      Seq operator ++ (int)
      {
	auto tmp(*this);
	++(*this);
	return tmp;
      }

      ElType* operator -> () const { return b; }

    private:
      Seq() = delete;

      ElType *b;
      ElType *e;
    };

    /// @brief forward iterator class for indexing
    /// elements in a matrix.
    template <typename ElType,
	      IterationDir Dir = IterationDir::COLS>
    class MatIter {
      /// @brief Mat is a friend class since only
      /// matrices can create matrix iterators.
      template<int rows, int cols>
      friend class Mat;
    public:

      ~MatIter() {}
    private:
      MatIter() = delete;
      MatIter(std::size_t row_size, std::size_t col_size,
	      std::size_t row_start, std::size_t col_start);

      std::size_t max_rows; //< maximum rows in buffer
      std::size_t max_cols; //< maximum cols in buffer
      std::size_t r; //< current row
      std::size_t c; //< current column

      /// @brief underlying matrix buffer of values.
      /// is a pointer to a ElType[max_rows][max_cols]
      /// object.
      ElType **buf;
    };

  } /* namespace mem */
} /* namespace afl */

#endif /* INCLUDE_MATITER_HPP_ */

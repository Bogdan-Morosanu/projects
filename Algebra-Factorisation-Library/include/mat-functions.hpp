/*
 * mat-functions.hpp
 *
 *  Created on: Feb 7, 2016
 *      Author: moro
 */

#ifndef INCLUDE_MAT_FUNCTIONS_HPP_
#define INCLUDE_MAT_FUNCTIONS_HPP_

#include "mat.hpp"
#include <iostream>

namespace afl {

  template<typename ElType, int rows, int cols>
  std::ostream&
  operator << (std::ostream& out, const Mat<ElType, rows, cols>& mat)
  {
    out << "[" << std::endl;
    for (int r = 0; r < rows; ++r) {
      out << " ";
      for (int c = 0; c < cols; ++c) {
	out << mat(r, c) << " ";
      }
      out << std::endl;
    }
    out << "]" << std::endl;
    return out;
  }

  /// @brief factory function, returns Identity matrix
  template <typename ElType, int dim>
  Mat<ElType, dim, dim>
  eye()
  {   // dim * dim assignments + dim * dim comparisons
    // are probably more expensive than
    // dim * dim + dim assignments, so we don't bother
    // checking. init all to zero, make diag 1.
    Mat<ElType, dim, dim> retval(0);
    for (int d = 0; d < dim; ++d) {
      retval(d, d) = 1;
    }
    return retval;
  }

  /// @brief factory function, returns permutation matrix
  /// that will permute the rows of another matrix when
  /// multiplied.
  /// trick: visualise afl::permutation as the permuted eye()
  /// and generating one will become very easy.
  /// (because permutation * eye == permutation).
  template <typename ElType, int dim>
  Mat<ElType, dim, dim>
  permutation(const std::array<int, dim>& p)
  {   // dim * dim assignments + dim * dim comparisons
    // are probably more expensive than
    // dim * dim + dim assignments, so we don't bother
    // checking. init all to zero, make diag 1.
    Mat<ElType, dim, dim> retval(0);
    for (int d = 0; d < dim; ++d) {
      retval(d, p[d]) = 1;
    }
    return retval;
  }

  template <typename SeedType,
	    typename ElType,
	    int rows, int cols>
  inline SeedType
  reduce_with(std::function<SeedType(SeedType, ElType)> reducer,
	      const Mat<ElType, rows, cols>& mat,
	      SeedType seed,
	      IterationDir dir)
  {
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	seed = reducer(seed, mat(r, c));
      }
    }
    return seed;
  }

  template <typename SeedType,
	    typename ElType,
	    int rows, int cols,
	    SeedType(*reducer)(SeedType, ElType)>
  inline SeedType
  reduce_with(const Mat<ElType, rows, cols>& mat,
	      SeedType seed)
  {
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	seed = reducer(seed, mat(r, c));
      }
    }
    return seed;
  }

  /// @brief friend function to multiply two matrices.
  /// a friend function is needed if we need type-conversions
  /// on both lhs and rhs arguments.
  template <typename ElType, int rows, int cols, int n>
  inline Mat<ElType, rows, cols>
  operator * (const Mat<ElType, rows, n>& lhs,
	      const Mat<ElType, n, cols>& rhs)
  {
    Mat<ElType, rows, cols> retval;

    // we've overloaded some function calls
    // instead of using the C dgemm, sgemm and so on.
    // this has the benefit that afl::Mat's with ElTypes that
    // do not have gemm ops defined will not compile.
    intern::overload_resolver_gemm(
				   CBLAS_LAYOUT::CblasColMajor, CBLAS_TRANSPOSE::CblasNoTrans,
				   CBLAS_TRANSPOSE::CblasNoTrans, rows, cols, n, 1.0,
				   lhs.buf[0], rows, rhs.buf[0], n, 0.0, retval.buf[0], rows);

    return retval;
  }

  template <typename ElType, int rows, int cols, int n>
  inline Mat<ElType, rows, cols>
  operator + (const Mat<ElType, rows, n>& lhs,
	      const Mat<ElType, n, cols>& rhs)
  {
    Mat<ElType, rows, cols> retval(lhs);
    return (retval += rhs);
  }

  template <typename ElType, int rows, int cols, int n>
  inline Mat<ElType, rows, cols>
  operator - (const Mat<ElType, rows, n>& lhs,
	      const Mat<ElType, n, cols>& rhs)
  {
    Mat<ElType, rows, cols> retval(lhs);
    return (retval -= rhs);
  }

}




#endif /* INCLUDE_MAT_FUNCTIONS_HPP_ */

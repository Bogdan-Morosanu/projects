/*
 * mat-inl.hpp
 *
 *  Created on: Jan 16, 2016
 *      Author: moro
 */
#include "mat.hpp"
#include "intern/overloads.hpp"
#include "tmp/mapped_range.hpp"

#include <iostream>
#include <array>
#include <limits>
#include <cmath>
#include <cassert>

#ifndef MAT_INL_HPP
#define MAT_INL_HPP

namespace afl {


  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>::
  Mat(const ElType& val)
  {
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	this->buf[c][r] = val;
      }
    }
  }

  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>::
  Mat(const std::initializer_list<ElType>& ls, IterationDir dir)
  {
    assert(ls.size() == rows * cols && "initializer list to matrix size mismatch!");
    auto it = ls.begin();
    if (dir == IterationDir::COLS) {
      for (int r = 0; r < rows; ++r) {
	for (int c = 0; c < cols; ++c) {
	  (*this)(r, c) = *it++;
	}
      }

    } else {
      for (int c = 0; c < cols; ++c) {
	for (int r = 0; r < rows; ++r) {
	  (*this)(r, c) = *it++;
	}
      }
    }
  }

  template<typename ElType, int rows, int cols>
  inline ElType
  Mat<ElType, rows, cols>::
  operator()(int r, int c) const
  {
    return buf[c][r];
  }

  template<typename ElType, int rows, int cols>
  inline ElType&
  Mat<ElType, rows, cols>::
  operator () (int r, int c)
  {
    return buf[c][r];
  }

  template<typename ElType, int rows, int cols>
  inline ElType
  Mat<ElType, rows, cols>::
  operator () (int idx, IterationDir dir) const
  {
    if (dir == IterationDir::COLS) {
      return (&buf[0][0]) + idx;

    } else {
      int ridx = idx % cols;
      int cidx = idx / cols;
      return buf[cidx][ridx];
    }
  }

  template<typename ElType, int rows, int cols>
  inline ElType&
  Mat<ElType, rows, cols>::
  operator () (int idx, IterationDir dir)
  {   // TODO test if this works or returns a ref to temporary object
    // return const_cast<const Mat*>(this)->operator()(idx, dir);
    if (dir == IterationDir::COLS) {
      return (&buf[0][0]) + idx;

    } else {
      int ridx = idx % cols;
      int cidx = idx / cols;
      return buf[cidx][ridx];
    }
  }


  template<typename ElType, int rows, int cols>
  inline Mat<ElType, cols, rows>
  Mat<ElType, rows, cols>::
  transp() const
  {
    Mat<ElType, cols, rows> retval;
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	retval(c, r) = (*this)(r, c);
      }
    }
    return retval;
  }

  /// @brief performs element-wise multiplication of matrices.
  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>
  Mat<ElType, rows, cols>::
  elem_mul(const Mat& rhs) const
  {
    Mat retval = (*this);
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	retval(r, c) *= rhs(r, c);
      }
    }
    return retval;
  }

  /// @brief performs element-wise division of matrices
  /// (elems in this are the numertators).
  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>
  Mat<ElType, rows, cols>::
  elem_div(const Mat& rhs) const
  {
    Mat retval = (*this);
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	retval(r, c) /= rhs(r, c);
      }
    }
    return retval;
  }


  template<typename ElType, int rows, int cols>
  inline std::pair<ElType, int>
  Mat<ElType, rows, cols>::
  max_abs(IterationDir d, int idx) const
  {	// denorm_min() is the min non-zero value, but we want
    // to accomodate 0 as well, so we use lowest as well.
    std::pair<ElType, int> retval =
      { std::numeric_limits<ElType>::lowest(), -1};

    if(d == IterationDir::ROWS) {
      for (int r = 0; r < rows; ++r) {
	if(retval.first < std::abs((*this)(r, idx))) {
	  retval = { (*this)(r, idx), idx };
	}
      }
    } else {
      for (int c = 0; c < cols; ++c) {
	if(retval.first < std::abs((*this)(idx, c))) {
	  retval = { (*this)(idx, c), idx };
	}
      }
    }

    return retval;
  }

  template<typename ElType, int rows, int cols>
  inline std::pair<ElType, int>
  Mat<ElType, rows, cols>::
  max(IterationDir d, int idx) const
  {
    std::pair<ElType, int> retval =
      { std::numeric_limits<ElType>::lowest(), -1};

    if(d == IterationDir::ROWS) {
      for (int r = 0; r < rows; ++r) {
	if(retval.first < (*this)(r, idx)) {
	  retval = { (*this)(r, idx), idx };
	}
      }
    } else {
      for (int c = 0; c < cols; ++c) {
	if(retval.first < (*this)(idx, c)) {
	  retval = { (*this)(idx, c), idx };
	}
      }
    }

    return retval;
  }

  template<typename ElType, int rows, int cols>
  inline std::pair<ElType, int>
  Mat<ElType, rows, cols>::
  min_abs(IterationDir d, int idx) const
  {
    std::pair<ElType, int> retval =
      { std::numeric_limits<ElType>::max(), -1};

    if(d == IterationDir::ROWS) {
      for (int r = 0; r < rows; ++r) {
	if(retval.first > std::abs((*this)(r, idx))) {
	  retval = { (*this)(r, idx), idx };
	}
      }
    } else {
      for (int c = 0; c < cols; ++c) {
	if(retval.first > std::abs((*this)(idx, c))) {
	  retval = { (*this)(idx, c), idx };
	}
      }
    }

    return retval;
  }

  template<typename ElType, int rows, int cols>
  inline std::pair<ElType, int>
  Mat<ElType, rows, cols>::
  min(IterationDir d, int idx) const
  {
    std::pair<ElType, int> retval =
      { std::numeric_limits<ElType>::max(), -1};

    if(d == IterationDir::ROWS) {
      for (int r = 0; r < rows; ++r) {
	if(retval.first > (*this)(r, idx)) {
	  retval = { (*this)(r, idx), idx };
	}
      }
    } else {
      for (int c = 0; c < cols; ++c) {
	if(retval.first > (*this)(idx, c)) {
	  retval = { (*this)(idx, c), idx };
	}
      }
    }

    return retval;
  }

  template <typename ElType>
  constexpr inline ElType
  l2_reducer(ElType seed, ElType cnt)
  {
    return seed + cnt * cnt;
  }

  /// @brief returns sum of squares of elements.
  template <typename ElType, int rows, int cols>
  inline ElType Mat<ElType, rows, cols>::
  l2_norm() const
  {
    return reduce_with<ElType, ElType, rows, cols, l2_reducer>(*this);
  }

  template <typename ElType>
  constexpr inline ElType
  l1_reducer(ElType seed, ElType cnt)
  {
    return seed + cnt;
  }

  /// @brief return sum of elements.
  template <typename ElType, int rows, int cols>
  inline ElType Mat<ElType, rows, cols>::
  l1_norm() const
  {
    return reduce_with<ElType, ElType, rows, cols, l1_reducer>(*this);
  }


  template <typename ElType>
  constexpr inline ElType
  linf_reducer(ElType seed, ElType cnt)
  {
    return std::max(seed, cnt);
  }

  /// @brief returns max element.
  template <typename ElType, int rows, int cols>
  inline ElType Mat<ElType, rows, cols>::
  linf_norm() const
  {
    return reduce_with<ElType, ElType, rows, cols, linf_reducer>(*this,
								 std::numeric_limits<ElType>::lowest());
  }


  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>&
  Mat<ElType, rows, cols>::
  operator *= (const Mat<ElType, cols, rows>& rhs)
  {	// TODO implement more efficient version of this
    return (*this) = (*this) * rhs;
  }

  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>&
  Mat<ElType, rows, cols>::
  operator += (const Mat& rhs)
  {
    for (int r = 0; r < rows; ++r) {
      for (int c = 0; c < cols; ++c) {
	this->buf[c][r] += rhs->buf[c][r];
      }
    }
  }

  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>&
  Mat<ElType, rows, cols>::
  operator -= (const Mat& rhs)
  {
    for (int r = 0; r < rows; ++r) {
      for (int c = 0; c < cols; ++c) {
	this->buf[c][r] -= rhs->buf[c][r];
      }
    }
  }

  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>
  operator * (ElType scalar, const Mat<ElType, rows, cols>& mat)
  {
    Mat<ElType, rows, cols> retval;
    for (int c = 0; c < cols; ++c) {
      for (int r = 0; r < rows; ++r) {
	retval(r, c) = scalar * mat(r, c);
      }
    }
    return retval;
  }

  template<typename ElType, int rows, int cols>
  inline Mat<ElType, rows, cols>
  operator * (const Mat<ElType, rows, cols>& mat, ElType scalar)
  {
    return scalar *  mat;
  }
}


#endif

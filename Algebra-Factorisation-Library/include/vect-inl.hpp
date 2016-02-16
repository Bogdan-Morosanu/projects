/*
 * vect-inl.hpp
 *
 *  Created on: Feb 7, 2016
 *      Author: moro
 */

#ifndef INCLUDE_VECT_INL_HPP_
#define INCLUDE_VECT_INL_HPP_

#include "vect.hpp"

namespace afl {
  template <typename ElType, int dim>
  inline
  Vect<ElType, dim> Vect<ElType, dim>::
  normalized() const
  {
    return ElType(1) / this->l2_norm() * (*this);
  }

  template <typename ElType, int dim>
  inline
  Vect<ElType, dim> Vect<ElType, dim>::
  projected(const Vect& rhs) const
  {   // we can't just do rhs * rhs since that's not
    // a well formed matrix expression. and we
    // don't want to do rhs * rhs.transp() for
    // performance reasons.
    auto rhs_d_rhs = rhs.elem_mul(rhs).l1_norm();
    auto rhs_d_this = this->elem_mul(rhs).l1_norm();
    return (rhs_d_this / rhs_d_rhs) * rhs;
  }

  template <typename ElType, int dim>
  inline
  Vect<ElType, dim> Vect<ElType, dim>::
  orthogonalized(const Vect& axis) const
  {
    return (*this) - this->projected(axis);
  }
}



#endif /* INCLUDE_VECT_INL_HPP_ */

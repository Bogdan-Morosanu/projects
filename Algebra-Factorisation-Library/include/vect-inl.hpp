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
    Vect<ElType, dim> Vect<ElType, dim>::
    normalized() const
    {
        return ElType(1) / this->l2_norm() * (*this);
    }

    template <typename ElType, int dim>
    Vect<ElType, dim> Vect<ElType, dim>::
    project(const Vect& rhs) const
    {
        auto proj_dir = rhs.normalized();
        auto proj_len = this->elem_mul(proj_dir);
        return proj_len * proj_dir;
    }
}



#endif /* INCLUDE_VECT_INL_HPP_ */

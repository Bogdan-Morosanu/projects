#ifndef EXPR_HPP_INCLUDED
#define EXPR_HPP_INCLUDED

#include "kern.hpp"
#include <memory>
#include <vector>

template<int K>
class Expression;

template<int K>
class AddExpression;

template<int K>
class MulExpression;

/// @brief Base class modeling expressions 
/// over K-dimensional feature vectors.
template<int K>
class Expression {
public:
  // pure virtual application
  virtual double operator()(const FeatVec<K>& x) const = 0;
  
  using Ptr = std::shared_ptr<Expression>;
  
  const Expression<K>& operator + (const Expression<K>& that) {
    Ptr expr(new AddExpression<K>(*this, that));
    // pass to vector for memory management
    Expression<K>::mem_pool().push_back(expr); 

    return *expr;
  }
  
  const Expression<K>& operator * (const Expression<K>& that) {
    Ptr expr(new MulExpression<K>(*this, that));
    // pass to vector for memory management
    Expression<K>::mem_pool().push_back(expr); 

    return *expr;
  }
  
  static const Expression<K>& get(int i) { return *(mem_pool()[i]); }

private:
  static std::vector<Ptr>& mem_pool(){
    static std::vector<Ptr> memory;
    return memory;
  };
};


/// @brief class modeling a multiplication of two subexpressions 
/// over K-dimensional vectors.
template<int K>
class MulExpression : public Expression<K> {
public:
  MulExpression(const Expression<K>& lhs, const Expression<K>& rhs)
    : l(lhs), r(rhs) {}

  double operator()(const FeatVec<K>& x) const {
    return l(x) * r(x);
  }

private:
  // we're using references right now,
  // instead of pointers because we don't need rebinding.
  // (should we need that, we should switch).
  const Expression<K>& l;
  const Expression<K>& r;
};


/// @brief class modeling an addition of two subexpressions 
/// over K-dimensional vectors.
template<int K>
class AddExpression : public Expression<K> {
public:
  AddExpression(const Expression<K>& lhs, const Expression<K>& rhs)
    : l(lhs), r(rhs) {}

  double operator()(const FeatVec<K>& x) const {
    return l(x) + r(x);
  }

private:
  // we're using references right now,
  // instead of pointers because we don't need rebinding.
  // (should we need that, we should switch).
  const Expression<K>& l;
  const Expression<K>& r;
};


/// @brief class modelling a base node in our expression
/// tree: it only evaluates a kernel.
/// \todo modify Kernel class to encapsulate mean vector as well.
template<int K>
class KernExpression : public Expression<K> {
public:
  KernExpression(const KernFunction<K>& kfn,
		 const FeatVec<K>& mean,
		 const CovMat<K>& covar):
    kern(kfn, mean, covar) {}

  // avoid implicit conversions from kernels to expressions
  explicit KernExpression(const Kernel<K>& krn):
    kern(krn) {}

  double operator()(const FeatVec<K>& x) const {
    return kern(x);
  }

private:
  const Kernel<K> kern;
};

// explicit instantiation
template class MulExpression<2>;
template class AddExpression<2>;
template class KernExpression<2>;

#endif

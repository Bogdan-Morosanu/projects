#ifndef KERN_HPP_INCLUDED
#define KERN_HPP_INCLUDED 

#include <iostream>
#include <functional>
#include <Eigen/Core>
#include <Eigen/Dense>


/// @brief Feature Vector, K-dimensional.
template<int K>
using FeatVec = Eigen::Matrix<double, K, 1>;

/// @brief Covariance matrix over a K-dimensional field
/// (K rows, K columns).
template<int K>
using CovMat = Eigen::Matrix<double, K, K>;

// @briefn Kernel Function mapping pairs of feature vector
// values to similarity values, using a covariance matrix.
template<int K>
using KernFunction =
  std::function< double (const FeatVec<K>&, 
			 const FeatVec<K>&, 
			 const CovMat<K>& ) >;

/// @brief class encapsulating a kernel function,
/// offering a simple binary function for applying diverse kernels.
/// @param K dimiensionality of vector space
template<int K>	 
class Kernel {
public:
  Kernel(const KernFunction<K>& kfn, //< kernel function
	 const FeatVec<K>& mean, //<mean vector 
         const CovMat<K>& cv): //< covariance matrix
    krn(kfn),
    m(mean),
    sigma(cv) { }
  
  /// apply kernel to get similarity metric.
  double operator()(const FeatVec<K>& x) const {
    return krn(x, m, sigma);
  }

private:
  const KernFunction<K> krn; //< kernel function
  const CovMat<K> sigma; //< covariance matrix
  const FeatVec<K> m;
};


template<int K>
KernFunction<K> se_generator(const std::vector<double>& params);

template<int K>
KernFunction<K> per_generator(const std::vector<double>& params);

template<int K>
KernFunction<K> lin_generator(const std::vector<double>& params);

template<int K>
KernFunction<K> rq_generator(const std::vector<double>& params);

template<int K>
double mh_dist(const FeatVec<K>& a,
	       const FeatVec<K>& b,
	       const CovMat<K>& covar); 

#endif

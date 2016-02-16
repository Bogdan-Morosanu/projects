#include <vector>

#include "kern.hpp"
#include "expr.hpp"

/// class modelling supervised learning data set.
template<int K>
class SpDataSet {
public:
  // currently only L2 available
  // \todo implement Mahalanobis, L1, and Linf
  enum class FitMetric { L2 };
  
  // currently only regression data points are provided
  // \todo implement enumerated labels
  struct DataPoint {
    double val;
    FeatVec<K> features;
  };
  
  using Container = std::vector<DataPoint>;
  using iterator = typename Container::iterator;

  void push_back(const DataPoint& x) { data.push_back(x); }
  
  iterator begin() { return data.begin(); }
  
  iterator end() { return data.end(); }
  
  double fit_score(const Expression<K>& expr, FitMetric mtr);

private:
  double fit_score_l2(const Expression<K>& expr);

  Container data;
};


template<int K>
double SpDataSet<K>::fit_score(const Expression<K>& expr,
			       SpDataSet<K>::FitMetric mtr) {
  switch(mtr) {
  case FitMetric::L2:
    return this->fit_score_l2(expr);
  default:
    assert(false && "Fit Metric Not Implemented");
  }
}

template<int K>
double SpDataSet<K>::fit_score_l2(const Expression<K>& expr) {
  double accum = 0.0;
  for(const DataPoint& pt : this->data) {
    // compute estimated value out of features and accumulate
    double est_val =  expr(pt.features);
    accum += (pt.val - est_val) * (pt.val - est_val);
  }
  return accum;
}

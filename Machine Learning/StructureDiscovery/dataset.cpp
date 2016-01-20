#include "dataset.hpp"

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

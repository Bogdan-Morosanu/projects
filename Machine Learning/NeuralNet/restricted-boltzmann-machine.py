import pandas as pd
import numpy as np
from copy import copy

def sigmoid(Weights, bias, x):
    "Get sigmoid activation column vector sgm(W*x + bias)"
    lin_comb = Weights*x + bias
    numer = np.exp(lin_comb)
    denom = 1 + np.exp(lin_comb)
    return np.divide(numer, denom)
    
class RBM:
    def __init__(self, inputs, hidden):
        "Create RBM with given inputs and hidden units, init with random weights"
        self.weights = np.mat([np.random.normal(0, 1, hidden) for i in range(0, inputs)])
        self.hd_bias = np.mat(np.random.normal(0, 1, hidden)).T
        self.in_bias = np.mat(np.random.normal(0, 1, inputs)).T
        
    # learning rate
    lrate = 0.5
    
    def activate_hidden(self, x):
        "Get P(h = 1|x), column vector of probabilities"
        return sigmoid(self.weights.T, self.hd_bias, x)
    
    def activate_visible(self, h):
        "Get P(x = 1|h), column vector of probabilities"
        return sigmoid(self.weights, self.in_bias, h)
    
    def sample_hidden(self, x):
        "Get a sample hidden layer column vector from visible column vector x"
        bool_act = self.activate_hidden(x) > np.mat(np.random.uniform(0, 1, len(self.hd_bias))).T
        return np.mat([0 if x else 1 for x in bool_act]).T
        
    def sample_visible(self, h):
        "Get a sample visible column vector from given hidden column vector h"
        bool_act = self.activate_visible(h) > np.mat(np.random.uniform(0, 1, len(self.in_bias))).T
        return np.mat([0 if x else 1 for x in bool_act]).T

    def update(self, x):
        "Implements constrastive divergence from training sample x"
        #sample distributions
        h1 = self.sample_hidden(x)
        x_reconstr = self.sample_visible(h1)
        h2_probs = self.activate_hidden(x_reconstr)
        
        #update rbm params
        self.weights += self.lrate * (x * h1.T - x_reconstr * h2_probs.T)
        self.in_bias += self.lrate * (x - x_reconstr)
        self.hd_bias += self.lrate * (h1 - h2_probs)
        
    def energy(self, x, h):
        "Computes the joint P(x, h), x visible and h hidden both column vectors"
        lin_comb = -x.T * self.in_bias -h.T * self.hd_bias -x.T * self.weights * h
        return np.exp(lin_comb)
    
    def reconstruct(self, x):
        "Computes reconstruction of input x"
        return self.sample_visible(self.sample_hidden(x))
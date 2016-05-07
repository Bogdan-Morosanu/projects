import random

states = ('Healthy', 'Fever')

observations = ('normal', 'cold', 'dizzy')

start_probability = {'Healthy': 0.6, 'Fever': 0.4}

transition_probability = {

   'Healthy' : {'Healthy': 0.7, 'Fever': 0.3},

   'Fever' : {'Healthy': 0.4, 'Fever': 0.6}

   }

emission_probability = {

   'Healthy' : {'normal': 0.5, 'cold': 0.4, 'dizzy': 0.1},

   'Fever' : {'normal': 0.1, 'cold': 0.3, 'dizzy': 0.6}

   }


class HMM:
    def __init__(self, states, start_prob, trans_prob, ems_prob):
        self.states = states
        self.start_prob = start_prob
        self.trans_prob = trans_prob
        self.ems_prob = ems_prob
    
    def viterbi(self, obs):
        # first init DP table
        V = [{}]
        for z in self.states:
            V[0][z] = self.start_prob[z] * self.ems_prob[z][obs[0]]
            
        # now propagate fwd in time
        for t in range(1, len(obs)):
            # notice we fan in and pin on current observation
            # also, notice we never use the full emission probability matrix
            # we always select a row from it, and thus, should we want to
            # maintain well defined probabilities, we should normalise at each step
            
            # can also apply log to avoid underflow
            V.append({})
            for z in self.states: 
                V[t][z] = max(V[t-1][z0] * 
                              self.trans_prob[z0][z] * 
                              self.ems_prob[z][obs[t]] for z0 in self.states)
                
        path = []
        for snapshot in V:
            mx = max(snapshot.values())
            for z, p in snapshot.items():
                if p == mx:
                    path.append(z) # cumulate max state at each time snapshot
        
        return path
                
    def fwd_bwd(self, obs, time = None):
        """
        :return: if time is None, returns FullTable, Forward Transition and Backward
                 Transition tables, else returns probability snapshot for given 
                 time as a list of (state, prob)
        """
        # Forward Probability Run
        Fwd = [{}]
        for z in self.states:
            Fwd[0][z] = self.start_prob[z]
        
        for t in range(0, len(obs)):
            Fwd.append({})
            for z in states: # fan in and pin on current observation
                Fwd[t+1][z] = sum(Fwd[t][z0] * 
                                  self.trans_prob[z0][z] * 
                                  self.ems_prob[z][obs[t]] for z0 in states)
            
            # now normalise since we conditioned on the observation and this
            # changed our universe
            norm = sum(Fwd[t+1][z] for z in states)
            for z in states:
                Fwd[t+1][z] /= norm
            
        # Backward Probability Run
        Bwd = [{} for t in range(0, len(obs) + 1)] # init all sequence
        for z in self.states:
            Bwd[len(obs)][z] = 1 # init with one
        
        # now we go backwards in time
        for t in range(len(obs)-1, -1, -1):
            for z in self.states:
                Bwd[t][z] = sum(Bwd[t+1][z0] *
                                self.trans_prob[z][z0] *
                                self.ems_prob[z0][obs[t]]
                                for z0 in self.states)
            
            # normalise this also
            norm = sum(Bwd[t][z] for z in self.states)
            for z in self.states:
                Bwd[t][z] /= norm
        
        if time is None:
            FullTable = [{} for t in range(0, len(obs)+1)]
            for t in range(0, len(FullTable)):
                for z in self.states:
                    FullTable[t][z] = Fwd[t][z] * Bwd[t][z]
                
                norm = sum(FullTable[t][z] for z in self.states)
                for z in self.states:
                    FullTable[t][z] /= norm
                    
            return FullTable, Fwd, Bwd
            
        else:
            snapshot = [(z, Fwd[time][z] * Bwd[time][z]) for z in self.states]
            norm = sum(p for (z, p) in snapshot)
            return [(z, p / norm) for (z, p) in snapshot]
        
    @staticmethod
    def baum_welch(states, obs_values, obs_seq):
        """
        :param: states all possible states
        :param: obs_values all possible unique values that can be observed
        :param: obs_seq observation sequence
        :return: HMM model with params estimated from observation sequence
        """
        random.seed() # sys time seed

        # first generate random params
        start_p = { z: random.random() for z in states }
        transition_p = { z0: 
                            { z1: random.random() for z1 in states }
                         for z0 in states
                       }

        emission_p = { z0: 
                         { x: random.random() for x in obs_values }
                       for z0 in states
                     }

        #do 100 iterations
        for i in range(0, 1000):
            prob_single_state, fwds, bwds = HMM(states, start_p, transition_p, emission_p).fwd_bwd(obs_seq)

            if i % 10 == 0:
                print("in iteration {}".format(i))

            prob_state_pairs = []
            for t in range(0, len(fwds)-2):
                prob_state_pairs.append({})
                for z0 in states:
                    prob_state_pairs[t][z0] = { z1: fwds[t][z0] * 
                                                    transition_p[z0][z1] *
                                                    bwds[t+1][z1] *
                                                    emission_p[z1][obs_seq[t+1]]
                                                 for z1 in states
                                               }

                # normalise transitions
                norm = 0
                for z0 in states:
                    for z1 in states:
                        norm += prob_state_pairs[t][z0][z1] 
                
                for z0 in states:
                    for z1 in states:
                        prob_state_pairs[t][z0][z1] /= norm


            # update params
            # Initial Prob Distribution
            start_p = fwds[1]

            # Transition Probability Matrix
            for z0 in states:
                # sum up all transitions from z0 to z1
                for z1 in states:
                    transition_p[z0][z1] = sum(prob_state_pairs[t][z0][z1] 
                                                   for t in range(0, len(prob_state_pairs)))


                # normalise with transitions away from state z0
                norm = sum(prob_single_state[t][z0] for t in range(0, len(prob_state_pairs)))
                for z1 in states:
                    transition_p[z0][z1] /= norm

            # Emission Probability Matrix
            for z in states:
                norm = sum(prob_single_state[t][z0] for t in range(0, len(prob_state_pairs)))
                for obs_v in obs_values:
                    emission_p[z][obs_v] = sum(prob_single_state[t][z0] if obs_seq[t] == obs_v else 0
                                                 for t in range(0, len(prob_state_pairs)))
                    emission_p[z][obs_v] /= norm

        return start_p, transition_p, emission_p
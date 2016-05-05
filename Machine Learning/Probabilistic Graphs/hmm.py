
class HMM:
    def __init__(self, states, start_prob, trans_prob, ems_prob):
        self.states = states
        self.start_prob = start_prob
        self.trans_prob = trans_prob
        self.ems_prob = ems_prob
    
    def viterbi(self, obs):
        # first init DP table
        V = [{}]
        for z in states:
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
                    
            return FullTable
            
        else:
            snapshot = [(z, Fwd[time][z] * Bwd[time][z]) for z in self.states]
            norm = sum(p for (z, p) in snapshot)
            return [(z, p / norm) for (z, p) in snapshot]
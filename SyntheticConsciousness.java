import java.util.*;
import java.util.stream.*;

/**
 * A demonstration implementation of Igor Aleksander's Five Axioms of Consciousness
 * This is a simplified educational model showing the key concepts.
 */
public class SyntheticConsciousness {
    
    // Neural State Machine - represents the current state of consciousness
    private NeuralStateSpace stateSpace;
    private AttentionMechanism attention;
    private ImaginationEngine imagination;
    private PlanningModule planner;
    private EmotionalSystem emotions;
    private Environment environment;
    
    public SyntheticConsciousness(int stateSpaceSize) {
        this.stateSpace = new NeuralStateSpace(stateSpaceSize);
        this.attention = new AttentionMechanism();
        this.imagination = new ImaginationEngine(stateSpace);
        this.planner = new PlanningModule(stateSpace);
        this.emotions = new EmotionalSystem();
        this.environment = new Environment();
    }
    
    /**
     * Main consciousness loop - integrates all five axioms
     */
    public void consciousCycle() {
        System.out.println("\n=== Consciousness Cycle ===");
        
        // AXIOM 1: PRESENCE - sense the environment
        double[] sensorInput = environment.getSensorData();
        NeuralState currentState = stateSpace.perceive(sensorInput);
        System.out.println("1. PRESENCE: Current state - " + currentState);
        
        // AXIOM 2: ATTENTION - focus on salient features
        double[] focusedInput = attention.focus(sensorInput, emotions.getCurrentMood());
        System.out.println("2. ATTENTION: Focused on features - " + Arrays.toString(focusedInput));
        
        // AXIOM 3: IMAGINATION - create internal representations
        NeuralState[] imaginedStates = imagination.simulate(currentState, 3);
        System.out.println("3. IMAGINATION: Simulated " + imaginedStates.length + " possible futures");
        for (int i = 0; i < imaginedStates.length; i++) {
            System.out.println("   Scenario " + (i+1) + ": " + imaginedStates[i]);
        }
        
        // AXIOM 4: PLANNING - decide on actions based on imagined outcomes
        Action plannedAction = planner.selectAction(currentState, imaginedStates, emotions);
        System.out.println("4. PLANNING: Selected action - " + plannedAction);
        
        // AXIOM 5: EMOTION - evaluate and update emotional state
        double reward = environment.executeAction(plannedAction);
        emotions.update(reward, currentState);
        System.out.println("5. EMOTION: Reward=" + reward + ", Mood=" + emotions.getCurrentMood());
        
        // Learn from experience
        stateSpace.learn(sensorInput, plannedAction, reward);
    }
    
    public void run(int cycles) {
        System.out.println("Starting Synthetic Consciousness Demonstration");
        System.out.println("Based on Igor Aleksander's Five Axioms\n");
        
        for (int i = 0; i < cycles; i++) {
            System.out.println("\n--- Cycle " + (i+1) + " ---");
            consciousCycle();
            
            try {
                Thread.sleep(1000); // Slow down for readability
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        SyntheticConsciousness consciousness = new SyntheticConsciousness(100);
        consciousness.run(5);
    }
}

/**
 * Neural State Space - represents possible states of consciousness
 * Similar to Aleksander's MAGNUS architecture concept
 */
class NeuralStateSpace {
    private int size;
    private List<NeuralState> states;
    private Map<NeuralState, Double> stateValues; // Learned values
    private Random random;
    
    public NeuralStateSpace(int size) {
        this.size = size;
        this.states = new ArrayList<>();
        this.stateValues = new HashMap<>();
        this.random = new Random();
    }
    
    public NeuralState perceive(double[] sensorInput) {
        // Create or retrieve a neural state based on input pattern
        NeuralState state = new NeuralState(sensorInput);
        
        if (!states.contains(state)) {
            states.add(state);
            stateValues.put(state, 0.0);
        }
        
        return state;
    }
    
    public void learn(double[] input, Action action, double reward) {
        NeuralState state = perceive(input);
        double currentValue = stateValues.getOrDefault(state, 0.0);
        // Simple reinforcement learning update
        double learningRate = 0.1;
        stateValues.put(state, currentValue + learningRate * reward);
    }
    
    public double getValue(NeuralState state) {
        return stateValues.getOrDefault(state, 0.0);
    }
}

/**
 * Represents a discrete state in the neural state space
 */
class NeuralState {
    private double[] pattern;
    private int hash;
    
    public NeuralState(double[] pattern) {
        this.pattern = pattern.clone();
        // Discretize pattern for state identification
        this.hash = Arrays.hashCode(discretize(pattern));
    }
    
    private int[] discretize(double[] pattern) {
        // Simple discretization - could be more sophisticated
        int[] discrete = new int[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            discrete[i] = (int)(pattern[i] * 10);
        }
        return discrete;
    }
    
    public double[] getPattern() {
        return pattern.clone();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeuralState)) return false;
        return this.hash == ((NeuralState)o).hash;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
    @Override
    public String toString() {
        return String.format("State[%s]", 
            Arrays.stream(pattern).limit(3)
                .mapToObj(d -> String.format("%.2f", d))
                .collect(Collectors.joining(", ")) + "...");
    }
}

/**
 * AXIOM 2: ATTENTION - selective focus mechanism
 */
class AttentionMechanism {
    private Random random = new Random();
    
    public double[] focus(double[] input, double emotionalBias) {
        // Attention modulated by emotional state
        double[] focused = new double[input.length];
        
        for (int i = 0; i < input.length; i++) {
            // Attention weights influenced by salience and emotion
            double salience = Math.abs(input[i]);
            double attentionWeight = salience * (1.0 + emotionalBias * 0.5);
            focused[i] = input[i] * Math.min(1.0, attentionWeight);
        }
        
        return focused;
    }
}

/**
 * AXIOM 3: IMAGINATION - internal simulation capability
 */
class ImaginationEngine {
    private NeuralStateSpace stateSpace;
    private Random random = new Random();
    
    public ImaginationEngine(NeuralStateSpace stateSpace) {
        this.stateSpace = stateSpace;
    }
    
    public NeuralState[] simulate(NeuralState currentState, int numScenarios) {
        // Generate possible future states through internal simulation
        NeuralState[] imagined = new NeuralState[numScenarios];
        
        for (int i = 0; i < numScenarios; i++) {
            // Perturb current state to imagine alternatives
            double[] pattern = currentState.getPattern();
            double[] imagined_pattern = new double[pattern.length];
            
            for (int j = 0; j < pattern.length; j++) {
                imagined_pattern[j] = pattern[j] + (random.nextGaussian() * 0.1);
            }
            
            imagined[i] = new NeuralState(imagined_pattern);
        }
        
        return imagined;
    }
}

/**
 * AXIOM 4: PLANNING - future-oriented action selection
 */
class PlanningModule {
    private NeuralStateSpace stateSpace;
    private Random random = new Random();
    
    public PlanningModule(NeuralStateSpace stateSpace) {
        this.stateSpace = stateSpace;
    }
    
    public Action selectAction(NeuralState current, NeuralState[] imagined, EmotionalSystem emotions) {
        // Evaluate imagined futures and select best action
        Action bestAction = Action.EXPLORE;
        double bestValue = Double.NEGATIVE_INFINITY;
        
        Action[] possibleActions = Action.values();
        
        for (Action action : possibleActions) {
            // Estimate value of each action based on imagined outcomes
            double estimatedValue = 0;
            for (NeuralState state : imagined) {
                estimatedValue += stateSpace.getValue(state);
            }
            estimatedValue /= imagined.length;
            
            // Add emotional bias
            estimatedValue += emotions.getCurrentMood() * 0.3;
            
            if (estimatedValue > bestValue) {
                bestValue = estimatedValue;
                bestAction = action;
            }
        }
        
        return bestAction;
    }
}

/**
 * AXIOM 5: EMOTION - affective evaluation system
 */
class EmotionalSystem {
    private double mood; // Current emotional state (-1 to +1)
    private double baseline = 0.0;
    
    public EmotionalSystem() {
        this.mood = baseline;
    }
    
    public void update(double reward, NeuralState state) {
        // Update mood based on reward
        double delta = reward - baseline;
        double emotionalLearningRate = 0.2;
        
        mood = mood * (1 - emotionalLearningRate) + delta * emotionalLearningRate;
        
        // Keep mood bounded
        mood = Math.max(-1.0, Math.min(1.0, mood));
    }
    
    public double getCurrentMood() {
        return mood;
    }
}

/**
 * Simple environment for the conscious agent
 */
class Environment {
    private Random random = new Random();
    private double[] state;
    private int timeStep = 0;
    
    public Environment() {
        this.state = new double[5];
        randomizeState();
    }
    
    public double[] getSensorData() {
        // Return current environmental state
        timeStep++;
        
        // Environment changes over time
        for (int i = 0; i < state.length; i++) {
            state[i] += (random.nextGaussian() * 0.1);
            state[i] = Math.max(-1, Math.min(1, state[i]));
        }
        
        return state.clone();
    }
    
    public double executeAction(Action action) {
        // Execute action and return reward
        double reward = 0;
        
        switch (action) {
            case APPROACH:
                reward = state[0] > 0 ? 1.0 : -0.5;
                break;
            case AVOID:
                reward = state[0] < 0 ? 1.0 : -0.5;
                break;
            case EXPLORE:
                reward = random.nextDouble() * 0.5;
                break;
            case REST:
                reward = 0.2;
                break;
        }
        
        return reward;
    }
    
    private void randomizeState() {
        for (int i = 0; i < state.length; i++) {
            state[i] = random.nextDouble() * 2 - 1;
        }
    }
}

/**
 * Possible actions the conscious agent can take
 */
enum Action {
    APPROACH,
    AVOID,
    EXPLORE,
    REST
}

# Synthetic Consciousness - Igor Aleksander's Five Axioms Implementation

## Overview

This Java program demonstrates Igor Aleksander's five axioms of consciousness:

1. **PRESENCE** - The system has awareness of being in an environment
2. **IMAGINATION** - Ability to create internal representations and simulate futures
3. **ATTENTION** - Selective focus on salient environmental features
4. **PLANNING** - Future-oriented action selection based on imagined outcomes
5. **EMOTION** - Affective evaluation that influences behavior

## Architecture

The implementation includes:

- **NeuralStateSpace**: Represents the space of possible conscious states (inspired by MAGNUS)
- **AttentionMechanism**: Selective filtering of sensory input
- **ImaginationEngine**: Internal simulation of possible future states
- **PlanningModule**: Action selection based on imagined outcomes
- **EmotionalSystem**: Affective evaluation and mood tracking
- **Environment**: Simple world for the agent to interact with

## How It Works

Each consciousness cycle:
1. Perceives the environment (PRESENCE)
2. Focuses attention on salient features (ATTENTION)
3. Imagines possible futures (IMAGINATION)
4. Plans actions based on predicted outcomes (PLANNING)
5. Updates emotional state based on rewards (EMOTION)
6. Learns from experience

## Compilation and Execution

```bash
# Compile
javac SyntheticConsciousness.java

# Run
java SyntheticConsciousness
```

## Example Output

```
Starting Synthetic Consciousness Demonstration
Based on Igor Aleksander's Five Axioms

--- Cycle 1 ---

=== Consciousness Cycle ===
1. PRESENCE: Current state - State[0.23, -0.45, 0.67...]
2. ATTENTION: Focused on features - [0.34, -0.23, 0.89, ...]
3. IMAGINATION: Simulated 3 possible futures
   Scenario 1: State[0.21, -0.43, 0.71...]
   Scenario 2: State[0.25, -0.47, 0.65...]
   Scenario 3: State[0.19, -0.41, 0.69...]
4. PLANNING: Selected action - APPROACH
5. EMOTION: Reward=1.0, Mood=0.2
```

## Key Concepts

### Neural State Space
- Discrete states representing different patterns of neural activity
- Similar to Aleksander's concept of depictive (pattern-based) representation
- States are learned and valued through experience

### Imagination vs Perception
- Perception: Direct sensory input creates states
- Imagination: Internal generation of states without external input
- Both use the same neural state representation

### Emotional Modulation
- Emotions influence attention (what we focus on)
- Emotions bias planning (risk-seeking vs risk-avoidance)
- Emotions are updated by reward signals

### Learning
- Simple reinforcement learning updates state values
- States associated with positive outcomes become more valuable
- Planning favors actions leading to high-value states

## Limitations

This is a simplified educational demonstration. A full implementation of Aleksander's ideas would require:

- More sophisticated neural architectures (actual neural networks)
- Hierarchical state representations
- More complex imagination mechanisms
- Richer environmental interactions
- Self-modeling capabilities

## Extensions You Could Try

1. Add visual pattern recognition (implement WISARD-like components)
2. Implement hierarchical state spaces
3. Add episodic memory for past experiences
4. Create more complex environments
5. Implement multiple interacting conscious agents
6. Add language processing capabilities

In a standard network (like a modern Transformer or LLM), learning happens by adjusting continuous 
floating-point weights (w). In Aleksander's discrete networks, there are no weights. Instead, the
 neurons are essentially lookup tables (RAM).

The Input: The input is a binary string (0s and 1s).

The Address: This string acts as a memory address.

The Output: The neuron looks at that address and outputs whatever bit is stored there.

Learning: Learning is not done via backpropagation or calculus. Instead, it is done by directly
 writing the desired output into the memory location.

In Igor Aleksander’s discrete (weightless) neural networks, the process of obtaining and 
writing the desired output is significantly simpler than the calculus-based methods used in 
standard AI. It is essentially a memory-addressing operation.

1. Obtaining the Address

To train a RAM-based neuron, you first present it with an input pattern (e.g., a specific 
set of pixels from an image).

This binary input acts as a lookup address for the neuron's internal RAM.

For a neuron with n inputs, there are 2 n possible memory locations. The current input 
string points to exactly one of these locations.

2. The Learning Signal (Writing the Output)

Once the input has selected a specific memory address, the system provides a desired output 
(usually a 0 or a 1) based on the training data.

Initialization: At the start, all memory locations are typically set to a neutral or undefined 
state (often represented as u or 0.5).

The Write Action: During the Learning State, the desired output bit is written directly into the 
selected address.

One-Shot Learning: Unlike standard networks that require thousands of iterations to nudge weights, 
a weightless neuron learns a pattern instantly. If it sees that specific binary input again, it 
will immediately output the bit you just stored.

3. Generalization (G-RAM)

In Aleksander’s more advanced G-RAM (Generalizing RAM) models, a spreading phase occurs after 
the initial write:

The desired output is not just written to the exact address, but also spread to neighboring 
addresses (those that have a small Hamming distance, meaning they only differ by one or two bits).

This allows the neuron to recognize patterns that are similar to the one it was taught, even 
if they aren't an exact match.

4. Goal-Seeking Neurons (GSN)

In his Goal-Seeking Neuron models, the learning process involves a Validation State. The neuron 
first checks if it can learn the new pattern without corrupting or overwriting important 
information previously stored in that memory location. If the current content is undefined (u), 
it proceeds to write the desired output.

## References

- Aleksander, I. (2001). "How to Build a Mind"
- Aleksander, I. & Dunmall, B. (2003). "Axioms and Tests for the Presence of Minimal Consciousness in Agents"
- Aleksander, I. (1996). "Impossible Minds: My Neurons, My Consciousness"

## Author Notes

This implementation prioritizes clarity and educational value over computational efficiency. The core idea is to show how Aleksander's five axioms can be computationally realized in a simple but functional system.

The program demonstrates that consciousness-like behavior can emerge from the interaction of these five fundamental capabilities operating on a neural state space.

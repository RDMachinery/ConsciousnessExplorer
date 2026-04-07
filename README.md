# Synthetic Consciousness Explorer

A Java Swing application that visualises Igor Aleksander's **Five Axioms of Synthetic Consciousness** operating in a live geometric world. The system perceives, attends to, imagines, plans, and emotionally evaluates a field of moving shapes — making the abstract machinery of a conscious architecture directly observable.

---

## Background

[Igor Aleksander](https://en.wikipedia.org/wiki/Igor_Aleksander) (1937–2024) was Professor of Neural Systems Engineering at Imperial College London. Over several decades he developed a coherent theory of machine consciousness grounded in neural state machines, most fully realised in his **Magnus** architecture and articulated in works including *Impossible Minds* (1996) and *My Neurons, My Consciousness* (2021).

Central to his theory are five functional axioms that he argued are both necessary and sufficient for a system to be considered conscious:

| Axiom | Name | Description |
|-------|------|-------------|
| 1 | **Presence** | The system forms depictive internal representations of the world from sensory input |
| 2 | **Attention** | The system selectively focuses on salient features, suppressing irrelevance |
| 3 | **Imagination** | The system can simulate possible future states without acting on them |
| 4 | **Planning** | The system selects actions by evaluating imagined outcomes |
| 5 | **Emotion** | The system maintains motivational states that orient it toward goals |

This application implements a simplified but structurally faithful version of that architecture, grounded in a visual world of geometric shapes so that each axiom's operation can be observed directly.

---

## What It Does

The application creates a field of **12 geometric shapes** — circles, triangles, squares, pentagons, and hexagons in seven colours — that drift autonomously around the canvas, bouncing off boundaries. On each cycle the consciousness engine:

1. **Perceives** the full scene by encoding every shape as a 5-dimensional sensor vector covering type, colour, x position, y position, and size
2. **Attends** to the most salient shape, with salience modulated by the current emotional state
3. **Imagines** three perturbed future world states and evaluates them against learned values
4. **Plans** by selecting the action with the best estimated outcome across imagined futures
5. **Updates its emotional state** based on the reward received from attending to a high-salience object

The attended shape is highlighted with a pulsing golden halo. All other shapes are dimmed in proportion to their salience score, giving a direct visual representation of what the system is ignoring and what it is focusing on.

---

## Architecture

```
ConsciousnessExplorer          (Swing JFrame — main entry point)
│
├── GeometricWorld             (the environment)
│   └── GeometricShape × 12   (each encodes to a 5-dim sensor vector)
│
├── ConsciousnessEngine        (the five-axiom loop)
│   ├── NeuralState2           (discretised world-state representation)
│   ├── Attention              (salience computation, emotion-modulated)
│   ├── Imagination            (Gaussian perturbation of current state)
│   ├── Planning               (value-based action selection)
│   └── EmotionalSystem        (mood as a bounded running average of reward)
│
└── GUI Components
    ├── WorldPanel             (animated canvas — shapes + attended halo)
    ├── SaliencePanel          (live bar chart of per-shape attention weights)
    ├── MoodBar                (bipolar emotional valence indicator)
    └── LogPanel               (scrolling consciousness cycle log)
```

Each shape is encoded as a normalised vector in `[-1, 1]^5`:

```
[ shape_type, colour, x_position, y_position, size ]
```

The full scene sensor array is the concatenation of all 12 shape vectors, giving a 60-dimensional input to the consciousness engine on each cycle.

---

## Requirements

- Java 17 or later (the code uses switch expressions introduced in Java 14)
- No external dependencies — standard library only

---

## Building and Running

```bash
# Compile
javac ConsciousnessExplorer.java

# Run
java ConsciousnessExplorer
```

The entire application is a single self-contained source file. No build tool, no classpath configuration, no dependencies to resolve.

---

## Controls

| Control | Action |
|---------|--------|
| **START / PAUSE** | Begin or suspend the consciousness cycle (80ms per tick) |
| **STEP** | Advance exactly one cycle manually |
| **RESET** | Reinitialise the world and engine with a new random configuration |

---

## User Interface

The interface is divided into four areas:

**World Canvas** (centre) — the live geometric environment. The attended shape carries a pulsing amber halo labelled ATTENDED. Non-attended shapes are rendered at reduced opacity proportional to their salience score, making the attention gradient directly visible.

**Sidebar** (right) — displays current cycle count, the identity of the attended shape, and a bipolar mood bar running from negative (red, left) through neutral (centre) to positive (green, right). An axiom legend indicates which of the five axioms are active. Below this, a salience bar chart shows the relative attention weight of every shape in the scene, updated each cycle.

**Log Panel** (bottom) — a scrolling record of each consciousness cycle showing the attended shape index, its salience score, and the current mood value.

---

## Design Notes

**Attention and irrelevancy suppression** — the visual dimming of non-attended shapes is not merely decorative. It is a direct rendering of the attention weight array computed by the engine. Shapes with very low salience become nearly invisible, reflecting Aleksander's argument that a conscious system does not merely process all inputs equally but actively constructs a focused representation of the world by suppressing what is currently irrelevant.

**Emotion modulating attention** — the salience computation is scaled by `(1 + mood * 0.5)`. A system in a positive emotional state attends more readily; a system in a negative state is more conservative. This is a simplified implementation of Aleksander's fifth axiom, where emotional valence is not a separate module but is woven into the perceptual and attentional processes.

**State learning** — the engine maintains a map from discretised world states to learned values, updated by simple reinforcement on each cycle. Over time the system builds a value landscape over the state space it has encountered, which the imagination and planning modules use to evaluate simulated futures.

**Determinism** — the engine itself is deterministic given a fixed sequence of inputs. The world introduces stochasticity through shape movement, but the consciousness cycle is a pure function of its inputs and accumulated state. This design choice reflects the broader argument that deterministic, inspectable neural architectures are preferable to stochastic ones for systems where verification matters.

---

## Relationship to Magnus

Aleksander's Magnus architecture used RAM-based neurons — lookup tables rather than weighted connections — to achieve full transparency at the individual neuron level. Each neuron's complete input-output behaviour was explicitly stored and therefore directly inspectable.

This implementation abstracts over the neuron-level implementation and models the architecture at the functional level, representing world states as discretised hash keys into a value table. A full Magnus implementation would replace the `NeuralState2` hash map with a network of RAM neurons whose lookup tables are updated by the learning rule, preserving the property that every state transition is traceable and auditable.

---

## Further Reading

- Aleksander, I. (1996). *Impossible Minds: My Neurons, My Consciousness*. Imperial College Press.
- Aleksander, I. (2005). *The World in My Mind, My Mind in the World*. Imprint Academic.
- Aleksander, I. & Morton, H. (1990). *An Introduction to Neural Computing*. Chapman and Hall.
- Aleksander, I. (2021). *My Neurons, My Consciousness*. World Scientific.

---

## Author

Mario Gianota

---

## Licence

MIT

# Canopy
Canopy is a generic tool that enables experimenting with sampling-based symbolic analyses. It builds on Symbolic PathFinder.

The sampling of paths is governed by a *sampling strategy* that can be used for implementing heuristics for exploring the state space of the program. Currently, Canopy provides three different strategies:

* Monte Carlo Tree Search
* Reinforcement Learning (modified version of the algorithm in the ASE 2014 paper: (Exact and approximate probabilistic symbolic execution for nondeterministic programs)[http://dl.acm.org/citation.cfm?id=2643011])
* Pure Monte Carlo

In addition, there is also support for exhaustive analysis, which reports results in a similar format as the sampling-based approaches.

An important aspect of Canopy is that it supports *path pruning*: When a path has been sampled, Canopy prevents it from being sampled again, i.e., Canopy guarantees progress which converges to an exhaustive analysis. It also supports incremental solving and constraints caching, which improves performance by several orders of magnitude.

In addition, Canopy provides notions of reward function, reporting functionalities, real-time visualization of results, etc. The latter gives snapshots of rewards over time, reward histograms, and live views of the search tree.

In summary, Canopy provides a unified platform that lets experimenters accurately evaluate and compare different heuristics for path exploration.

## Installation
There are two ways to install Canopy:

* Installation on local machine by setting up manually Java PathFinder and Symbolic PathFinder
* Virtual machine with Docker 

## Local Machine
Before continuing, make sure that `jpf-core` and `jpf-symbc` are installed.

To install Canopy, update your `site.properties` file (usually `~/.jpf/site.properties`) and set the `canopy` variable to point to the directory of your Canopy installation. 
```
canopy=/path/to/canopy
```

**Don't** add `canopy` to the `extensions` variable.

Make sure you have `Ivy` installed on your system. To bootstrap the Ivy ant task, you can run:
```
$ ant bootstrap
```

Then, obtain all the dependencies by running:
```
$ ant resolve
```
The dependencies will be downloaded to `lib/`.

Now canopy can be built by simply running:
```
$ ant build
```
### Docker
Assuming you have [Docker](https://www.docker.com/) installed, simply run:

```bash
$ docker build -t canopy .
# Will take some time to build the image...
$ docker run -it canopy
```

**Note** that, because there is no X11 available, the live views that can optionally be enabled for Canopy do not work. The results are still produced for the user to inspect.

## Usage 
The analysis can be performed by executing the JPF config file that specifies the parameters of the analysis, the constraint solver, the entry point of the system under analysis etc:

```
$ ./jpf-core/bin/jpf <path-to-jpf-file>
```

### Example
This section shows a couple of examples of using the Monte Carlo Tree Search strategy; the Reinforcement Learning strategy; and the Pure Monte Carlo strategy.
For all analyses, a window should open showing the live statistics of the results.

To use the Monte Carlo Tree Search algorithm for analyzing Quicksort, execute:
```
$ ./jpf-core/bin/jpf canopy/src/examples/sampling/mcts/QuickSort.jpf
```

To use the Reinforcement Learning algorithm for analyzing Quicksort, execute:
```
$ ./jpf-core/bin/jpf canopy/src/examples/sampling/rl/QuickSort.jpf
```

To use the Pure Monte Carlo algorithm for analyzing Quicksort, execute:
```
$ ./jpf-core/bin/jpf canopy/src/examples/sampling/montecarlo/QuickSort.jpf
```

To run the exhaustive analysis for Quicksort, execute
```
$ ./jpf-core/bin/jpf canopy/src/examples/sampling/exhaustive/QuickSort.jpf
```

## Configuration
Canopy has global and analysis local configuration options. Configuration of Canopy happen through the jpf file.

To enable Canopy, the JPF file **must** contain the `@using` directive:
```
@using canopy
```

Canopy relies on the configuration options available in Java PathFinder and Symbolic PathFinder and can use the incremental solver in Symbolic PathFinder. Please consult these projects regarding configuration.

Most importantly is that the JPF file contains values for `target` (and possibly `classpath` if the SUT is not part of Canopy), `symbolic.method`, and `symbolic.dp`.

### Global Options
All options are either optional or have default values.

* **symbolic.security.sampling.rewardfunc** An implementation of `edu.cmu.sv.isstac.sampling.reward.RewardFunction` that provides rewards for paths. Default: `edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction`, i.e. reward is based on depth (number of decisions) of paths
* **symbolic.security.sampling.termination** An implementation of `edu.cmu.sv.isstac.sampling.termination.TerminationStrategy` that specifies when to stop sampling paths. Note that when pruning is used, analysis will terminate after all paths have been explored. Default: `edu.cmu.sv.isstac.sampling.termination.NeverTerminateStrategy`. See option `symbolic.security.sampling.termination.samplingsize` which provides a shortcut for sampling a specific number of paths
* **symbolic.security.sampling.termination.samplingsize** If `symbolic.security.sampling.termination` is not set, this option can be used to easily specify a termination strategy that samples the specified number of paths
* **symbolic.security.sampling.livestats** Boolean that controls whether the live view will be shown to the user. If true, this can impact performance slightly. Default: true
* **symbolic.security.sampling.stats** Boolean that controls whether to output results to std output when the analysis is done. Default: True
* **symbolic.security.sampling.eventobservers** List of implementations of `edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver`, observers that can be used to monitor the analysis. Optional
* **symbolic.security.sampling.choicesstrategy** An implementation of `edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy` that provides eligible choices for each state and controls whether a path has been seen before. Is used for implementing path pruning but can be changed by the user through this option. Default: `edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy`. There is also a `edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy` which effectively disables pruning.
* **symbolic.security.sampling.backtrackingsearch** A boolean that controls whether Canopy should use backtracking whenever it samples an ignored state (happens when Symbolic PathFinder explores updated PC of an infeasible choice). For programs with many infeasible choices, this option has significant performance improvements. Default: True
* **symbolic.security.sampling.seed** Specify the seed for the random number generators. **Note** If this option is not set, a default seed will be used
* **symbolic.security.sampling.random** A boolean that controls whether the random number generators are initialized with random seeds. Default: False

There are also options for amplifying rewards with model counting---since this is experimental, we leave out the details here.

### Analysis Local Options
The analyses are enabled by using the corresponding JPF Shell. Please consult each section below for how to use and configure the analyses.

#### Monte Carlo Tree Search
To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.sampling.mcts.MCTSShell
```

The Monte Carlo Tree Search strategy can be configured with the following options:

* **symbolic.security.sampling.mcts.selectionpol** An implementation of `edu.cmu.sv.isstac.sampling.mcts.SelectionPolicy` that specifies the policy for selecting a new leaf in the search tree maintained by MCTS. Default is `edu.cmu.sv.isstac.sampling.mcts.UCBPolicy` which uses the UCB criterion for selecting child nodes
* **symbolic.security.sampling.mcts.simulationpol** An implementation of `edu.cmu.sv.isstac.sampling.policies.SimulationPolicy` that controls how children are selected during the simulation run. Default `edu.cmu.sv.isstac.sampling.mcts.UniformSimulationPolicy` which uniformly, at random, selects children
* **symbolic.security.sampling.mcts.uct.bias** Controls the UCT bias for the `UCBPolicy` (if used). Default is `Math.sqrt(2)`
* **symbolic.security.sampling.mcts.treevisualizer** Boolean that controls whether to create a window that shows how nodes are expanded in real time thus visualizing the tree maintained by the MCTS algorithm. Slows down performance significantly. Default: False

This analysis can use model counting for amplifying rewards. This feature is currently experimental and is thus not covered here.

#### Reinforcement Learning
To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningShell
```
Please consult the following paper that describes the analysis and parameters in detail:
> Kasper Luckow, Corina Pasareanu, Matthew Dwyer, Antonio Filieri, Willem Visser, **Exact and approximate probabilistic symbolic execution for nondeterministic programs**, Proceedings of the 29th ACM/IEEE international conference on Automated software engineering (ASE 2014), \[[pdf](https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/20150000116.pdf)\] \[[bibtex](https://scholar.googleusercontent.com/citations?view_op=export_citations&user=RnfTeq8AAAAJ&s=RnfTeq8AAAAJ:Tyk-4Ss8FVUC&citsig=AMstHGQAAAAAWN2bHZRaRn27c88nNLU7InV3I6kibiju&hl=en&cit_fmt=0)\].


The Reinforcement Learning strategy can be configured with the following options:

* **symbolic.security.sampling.rl.samplesperoptimization** Controls how many paths will be sampled before performing the optimization step. Default 100
* **symbolic.security.sampling.rl.epsilon** Sets the value of the epsilon parameter. Default: 0.5
* **symbolic.security.sampling.rl.history** Sets the value of the history parameter. Default: 0.5

This analysis can also use model counting for amplifying rewards. This feature is currently experimental and is thus not covered here.

#### Pure Monte Carlo
To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloShell
```

The Pure Monte Carlo strategy can be configured with the following options:

* **symbolic.security.sampling.montecarlo.simulationpol** An implementation of `edu.cmu.sv.isstac.sampling.policies.SimulationPolicy` that controls how children are selected during the simulation run. Default `edu.cmu.sv.isstac.sampling.mcts.UniformSimulationPolicy` which uniformly, at random, selects children


#### Exhaustive
To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.sampling.exhaustive.ExhaustiveShell
```

This analysis does not have additional configuration options.

## Extensions
Some extensions are built on top of Canopy. We outline them here.

### Complexity Analysis
Canopy includes an extension for performing complexity analysis. It works by increasing the input size (e.g., the length of a list to be sorted), and then using any of the analyses described above, to find the *longest* paths for each input size. 

Canopy will live update with a chart showing these data points. The data points are also output to CSV which can be used with regression analysis to estimate the complexity of the analyzed component.

To use it, simply set:
```
shell = edu.cmu.sv.isstac.sampling.complexity.ComplexityAnalysisShell
```

It can be configured using the following options:

* **symbolic.security.sampling.complexity.type** The analysis used for finding the longest path. **MUST** be set to one of `mcts`, `rl`, `mc`, or `exhaustive`
* **symbolic.security.sampling.complexity.inputrange** The range with which to obtain data points. **MUST** be set to, e.g., `1,10` 
* **symbolic.security.sampling.complexity.inputincrement** The increment with which to pick inputsizes in the `inputrange`. Default: 1
* **symbolic.security.sampling.complexity.outputdir** The output dir used for storing results data. Default: ./
* **symbolic.security.sampling.complexity.visualize** Visualize the data points as they are obtained by the underlying analysis. Default: True


## License
TBD

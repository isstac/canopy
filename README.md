# Canopy

Canopy is a generic tool that enables experimenting with sampling-based symbolic analyses. It builds on Symbolic PathFinder.

To cite Canopy, please use the most recent paper that was accepted at SEFM 2018:
> Kasper Luckow, Corina Pasareanu, Villem Wisser, **Monte Carlo Tree Search for Finding Costly Paths in Programs**, to appear in Proceedings of Software Engineering and Formal Methods (SEFM 2018).

Please visit [https://bitbucket.org/anonresearcher/sefm2018] for supplementary material to the paper including a Canopy distribution for reproducing the results and a paper appendix containing the full results tables etc.

## Overview
The sampling of paths is governed by a *sampling strategy* that can be used for implementing heuristics for exploring the state space of the program. Currently, Canopy provides three different strategies:

* Monte Carlo Tree Search
* Reinforcement Learning (modified version of the algorithm in the ASE 2014 paper: [Exact and approximate probabilistic symbolic execution for nondeterministic programs](http://dl.acm.org/citation.cfm?id=2643011).
* Pure Monte Carlo

In addition, there is also support for exhaustive analysis, which reports results in a similar format as the sampling-based approaches.

An important aspect of Canopy is that it supports *path pruning*: When a path has been sampled, Canopy prevents it from being sampled again, i.e., Canopy guarantees progress which converges to an exhaustive analysis. It also supports incremental solving and constraints caching, which improves performance by several orders of magnitude.

In addition, Canopy provides notions of reward function, reporting functionalities, real-time visualization of results, etc. The latter gives snapshots of rewards over time, reward histograms, and live views of the search tree.

In summary, Canopy provides a unified platform that lets experimenters accurately evaluate and compare different heuristics for path exploration.

## Installation
There are two ways to install Canopy:

* Installation on local machine by setting up manually Java PathFinder and Symbolic PathFinder
* Virtual machine with Docker 

### Local Machine
Before continuing, make sure that `jpf-core` and `jpf-symbc` are installed.

To install Canopy, update your `site.properties` file (usually `~/.jpf/site.properties`) and set the `canopy` variable to point to the directory of your Canopy installation. 
```
canopy=/path/to/canopy
```

Do **not** add `canopy` to the `extensions` variable.

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

* **canopy.rewardfunc** An implementation of `edu.cmu.sv.isstac.canopy.reward.RewardFunction` that provides rewards for paths. Default: `edu.cmu.sv.isstac.canopy.reward.DepthRewardFunction`, i.e. reward is based on depth (number of decisions) of paths
* **canopy.termination** An implementation of `edu.cmu.sv.isstac.canopy.termination.TerminationStrategy` that specifies when to stop sampling paths. Note that when pruning is used, analysis will terminate after all paths have been explored. Default: `edu.cmu.sv.isstac.canopy.termination.NeverTerminateStrategy`. See option `canopy.termination.samplingsize` which provides a shortcut for sampling a specific number of paths
* **canopy.termination.samplingsize** If `canopy.termination` is not set, this option can be used to easily specify a termination strategy that samples the specified number of paths
* **canopy.livestats** Boolean that controls whether the live view will be shown to the user. If true, this can impact performance slightly. Default: true
* **canopy.stats** Boolean that controls whether to output results to std output when the analysis is done. Default: True
* **canopy.eventobservers** List of implementations of `edu.cmu.sv.isstac.canopy.analysis.AnalysisEventObserver`, observers that can be used to monitor the analysis. Optional
* **canopy.choicesstrategy** An implementation of `edu.cmu.sv.isstac.canopy.exploration.ChoicesStrategy` that provides eligible choices for each state and controls whether a path has been seen before. Is used for implementing path pruning but can be changed by the user through this option. Default: `edu.cmu.sv.isstac.canopy.exploration.PruningChoicesStrategy`. There is also a `edu.cmu.sv.isstac.canopy.exploration.AllChoicesStrategy` which effectively disables pruning.
* **canopy.backtrackingsearch** A boolean that controls whether Canopy should use backtracking whenever it samples an ignored state (happens when Symbolic PathFinder explores updated PC of an infeasible choice). For programs with many infeasible choices, this option has significant performance improvements. Default: True
* **canopy.seed** Specify the seed for the random number generators. **Note** If this option is not set, a default seed will be used
* **canopy.random** A boolean that controls whether the random number generators are initialized with random seeds. Default: False

There are also options for amplifying rewards with model counting---since this is experimental, we leave out the details here.


### Analysis Local Options

The analyses are enabled by using the corresponding JPF Shell. Please consult each section below for how to use and configure the analyses.


#### Monte Carlo Tree Search

To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.canopy.mcts.MCTSShell
```

The Monte Carlo Tree Search strategy can be configured with the following options:

* **canopy.mcts.selectionpol** An implementation of `edu.cmu.sv.isstac.canopy.mcts.SelectionPolicy` that specifies the policy for selecting a new leaf in the search tree maintained by MCTS. Default is `edu.cmu.sv.isstac.canopy.mcts.UCBPolicy` which uses the UCB criterion for selecting child nodes
* **canopy.mcts.simulationpol** An implementation of `edu.cmu.sv.isstac.canopy.policies.SimulationPolicy` that controls how children are selected during the simulation run. Default `edu.cmu.sv.isstac.canopy.mcts.UniformSimulationPolicy` which uniformly, at random, selects children
* **canopy.mcts.uct.bias** Controls the UCT bias for the `UCBPolicy` (if used). Default is `Math.sqrt(2)`
* **canopy.mcts.treevisualizer** Boolean that controls whether to create a window that shows how nodes are expanded in real time thus visualizing the tree maintained by the MCTS algorithm. Slows down performance significantly. Default: False

This analysis can use model counting for amplifying rewards. This feature is currently experimental and is thus not covered here.


#### Reinforcement Learning

To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.canopy.reinforcement.ReinforcementLearningShell
```
Please consult the following paper that describes the analysis and parameters in detail:
> Kasper Luckow, Corina Pasareanu, Matthew Dwyer, Antonio Filieri, Willem Visser, **Exact and approximate probabilistic symbolic execution for nondeterministic programs**, Proceedings of the 29th ACM/IEEE international conference on Automated software engineering (ASE 2014), \[[pdf](https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/20150000116.pdf)\] \[[bibtex](https://scholar.googleusercontent.com/citations?view_op=export_citations&user=RnfTeq8AAAAJ&s=RnfTeq8AAAAJ:Tyk-4Ss8FVUC&citsig=AMstHGQAAAAAWN2bHZRaRn27c88nNLU7InV3I6kibiju&hl=en&cit_fmt=0)\].


The Reinforcement Learning strategy can be configured with the following options:

* **canopy.rl.samplesperoptimization** Controls how many paths will be sampled before performing the optimization step. Default 100
* **canopy.rl.epsilon** Sets the value of the epsilon parameter. Default: 0.5
* **canopy.rl.history** Sets the value of the history parameter. Default: 0.5

This analysis can also use model counting for amplifying rewards. This feature is currently experimental and is thus not covered here.


#### Pure Monte Carlo

To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.canopy.montecarlo.MonteCarloShell
```

The Pure Monte Carlo strategy can be configured with the following options:

* **canopy.montecarlo.simulationpol** An implementation of `edu.cmu.sv.isstac.canopy.policies.SimulationPolicy` that controls how children are selected during the simulation run. Default `edu.cmu.sv.isstac.canopy.mcts.UniformSimulationPolicy` which uniformly, at random, selects children


#### Exhaustive

To enable this analysis, put in the JPF file:
```
shell = edu.cmu.sv.isstac.canopy.exhaustive.ExhaustiveShell
```

This analysis does not have additional configuration options.


## Extensions

Some extensions are built on top of Canopy. We outline them here.


### Complexity Analysis

Canopy includes an extension for performing complexity analysis. It works by increasing the input size (e.g., the length of a list to be sorted), and then using any of the analyses described above, to find the *longest* paths for each input size. 

Canopy will live update with a chart showing these data points. The data points are also output to CSV which can be used with regression analysis to estimate the complexity of the analyzed component.

To use it, simply set:
```
shell = edu.cmu.sv.isstac.canopy.complexity.ComplexityAnalysisShell
```

It can be configured using the following options:

* **canopy.complexity.type** The analysis used for finding the longest path. **MUST** be set to one of `mcts`, `rl`, `mc`, or `exhaustive`
* **canopy.complexity.inputrange** The range with which to obtain data points. **MUST** be set to, e.g., `1,10`
* **canopy.complexity.inputincrement** The increment with which to pick inputsizes in the `inputrange`. Default: 1
* **canopy.complexity.outputdir** The output dir used for storing results data. Default: ./
* **canopy.complexity.visualize** Visualize the data points as they are obtained by the underlying analysis. Default: True


## LICENSE

Canopy is Copyright (c) 2017, Carnegie Mellon University and is released under the MIT License. See the `LICENSE` file in the root of this project and the headers of the individual files in the `src/` folder for the details.

Canopy uses benchmarks from the WISE project by Jacob Burnim, Sudeep Juvekar, Koushik Sen. 
The benchmarks are available here [WISE-1.0.tar.gz](https://www.burn.im/pubs/WISE-1.0.tar.gz).

WISE is Copyright (c) 2011, Regents of the University of California,
and is released under an open-source BSD-style license.  See the
individual source files under `src/examples/wise` for details. A copy of the `README` file of 
WISE that includes license details can be found in the file `licenses/README.WISE`.

Benchmark code in `src/examples/sampling/wise/*/*.java` is based on the code obtained from the WISE 
project. It is Copyright (c) 2011, Regents of the University of California, and is
released under an open-source BSD-style license.

We repeat here the license details from the `README` file (with file paths adjusted) in the WISE 
distribution from above:

>The code in `src/examples/sampling/wise/rbtree/` for
>red-black trees is by Tuomo Saarni, obtained from:
>
>    http://users.utu.fi/~tuiisa/Java/index.html
>
>
>under the following license:
>
>    Here's some java sources I've made. Most codes are free to
>    download. If you use some of my sources just remember give me the
>    credits.
>
>The code in src/examples/sampling/wise/java15/{util,lang}/ is
>originally from the Oracle Java (TM) 2 Platform Standard Edition
>Development Kit 5.0 Update 22, obtained and redistributed under the
>Java Research License v1.5 -- please see `licenses/JavaResearchLicense.txt` for
>details. Use and distribution of this technology is subject to the
>Java Research License included herein.

In addition, Canopy relies on several other libraries:

* Google Guava, which is distributed under the Apache License, Version 2.0. The license for Google Guava can be found in the file `licenses/COPYING.GUAVA`.
* Google Guice, which is distributed under the Apache License, Version 2.0. The license for Google Guava can be found in the file `licenses/COPYING.GUICE`.
* JFreeChart, which is distributed under the GNU Lesser General Public License (LGPL) version 2.1 or later. A copy of the license can be found in the file `licenses/licence-LGPL.JFREECHART`.
* Apache Commons Math3, which is distributed under the Apache License, Version 2.0. A copy of the license can be found in the file `licenses/LICENSE.COMMONS_MATH3`. The `NOTICE` file of Apache Commons Math3 can be found in the file `licenses/NOTICE.COMMONS_MATH3`.
* Apache Commons JCS, which is distributed under the Apache License, Version 2.0. A copy of the license can be found in the file `licenses/LICENSE.COMMONS_JCS`. The `NOTICE` file of Apache Commons JCS can be found in the file `licenses/NOTICE.COMMONS_JCS`.
* Apache Commons IO, which is distributed under the Apache License, Version 2.0. A copy of the license can be found in the file `licenses/LICENSE.COMMONS_IO`. The `NOTICE` file of Apache Commons IO can be found in the file `licenses/NOTICE.COMMONS_IO`.
* Javax-inject, which is distributed under the Apache License, Version 2.0. A copy of the license can be found in the file `licenses/LICENSE.JAVAXINJECT`. can be found in the file `licenses/NOTICE.JAVAXINJECT`.
* JGraphX, which is distributed under the BSD 3-Clause License. A copy of the license can be found in the file `licenses/LICENSE.JGRAPHX`.
* Antlr4, which is distributed under the BSD 3-Clause License. A copy of the license can be found in the file `licenses/LICENSE.ANTLR4`. 
* Jung2, which is distributed under the BSD 2-Clause License. A copy of the license can be found in the file `licenses/LICENSE.JUNG2`. 
